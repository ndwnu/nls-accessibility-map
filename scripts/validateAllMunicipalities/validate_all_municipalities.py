#!/usr/bin/env python3
import argparse
import json
import shlex
import shutil
import sys
import time
import urllib.error
import urllib.request
from dataclasses import dataclass
from pathlib import Path
from typing import Optional

import yaml

SCRIPT_DIRECTORY = Path(__file__).resolve().parent
DEFAULT_MUNICIPALITIES_FILE = SCRIPT_DIRECTORY.parent.parent / "backend" / "src" / "main" / "resources" / "data" / "municipalities.yml"
DEFAULT_BASE_URL = "https://data.ndw.nu/api/rest/static-road-data/accessibility-map"
ACCESSIBILITY_PATH = "/v2/accessibility.geojson"
VALID_LOG_FILE = SCRIPT_DIRECTORY / "valid.log"
INVALID_MUNICIPALITIES_DIRECTORY = SCRIPT_DIRECTORY / "invalidMunicipalities"
MUNICIPALITIES_GEOJSON_FILE = SCRIPT_DIRECTORY / "municipalities.geojson"
REQUEST_TIMEOUT_SECONDS = 120
MINIMUM_ACCESSIBLE_PERCENTAGE = 80.0
MAXIMUM_REQUESTS_PER_SECOND = 2

STATUS_VALID = "VALID"
STATUS_INVALID = "INVALID"
STATUS_FAILED = "FAILED"


@dataclass
class Municipality:
    name: str
    municipality_id: str
    destination_latitude: float
    destination_longitude: float


@dataclass
class ValidationResult:
    status: str
    finding: str
    http_status: Optional[int] = None
    response_body: Optional[bytes] = None
    road_section_segment_count: Optional[int] = None
    accessible_count: Optional[int] = None
    accessible_percentage: Optional[float] = None


def main() -> int:
    arguments = parse_arguments()
    municipality_entries = load_municipality_entries(Path(arguments.municipalities_file))
    write_municipalities_geojson(municipality_entries)
    municipalities = create_municipalities(municipality_entries)
    if arguments.municipality:
        municipalities = [municipality for municipality in municipalities if municipality.municipality_id == arguments.municipality]
        if not municipalities:
            print(f"municipality {arguments.municipality} not found in {arguments.municipalities_file}", file=sys.stderr)
            return 1

    if arguments.retry_invalid:
        municipalities = select_invalid_municipalities(municipalities)
        if not municipalities:
            print("nothing to retry")
            return 0
    else:
        clean_output()

    url = arguments.base_url.rstrip("/") + ACCESSIBILITY_PATH
    name_width = max(len(municipality.name) for municipality in municipalities)
    counter_width = len(str(len(municipalities)))
    status_counts = {STATUS_VALID: 0, STATUS_INVALID: 0, STATUS_FAILED: 0}
    minimum_seconds_between_requests = 1 / MAXIMUM_REQUESTS_PER_SECOND
    last_request_started_at = None

    for index, municipality in enumerate(municipalities, start=1):
        if last_request_started_at is not None:
            time.sleep(max(0.0, minimum_seconds_between_requests - (time.monotonic() - last_request_started_at)))
        last_request_started_at = time.monotonic()

        request_body = create_request_body(municipality)
        result = validate(url, request_body)
        shutil.rmtree(INVALID_MUNICIPALITIES_DIRECTORY / municipality.municipality_id, ignore_errors=True)
        status_counts[result.status] += 1

        if result.status == STATUS_VALID:
            with VALID_LOG_FILE.open("a") as valid_log:
                valid_log.write(municipality.municipality_id + "\n")
        else:
            write_invalid_municipality(municipality, url, request_body, result)

        print(f"[{index:>{counter_width}}/{len(municipalities)}] {municipality.municipality_id} {municipality.name:<{name_width}}  "
              f"{result.status:<8} {format_progress_detail(result)}", flush=True)

    print()
    print(f"valid: {status_counts[STATUS_VALID]}, invalid: {status_counts[STATUS_INVALID]}, failed: {status_counts[STATUS_FAILED]}")
    print(f"valid municipalities: {VALID_LOG_FILE}")
    print(f"invalid municipalities: {INVALID_MUNICIPALITIES_DIRECTORY}")
    return 0


def parse_arguments() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Validates the accessibility map response for every municipality")
    parser.add_argument("--municipalities-file", default=str(DEFAULT_MUNICIPALITIES_FILE))
    parser.add_argument("--base-url", default=DEFAULT_BASE_URL)
    selection_group = parser.add_mutually_exclusive_group()
    selection_group.add_argument("--municipality", help="only validate this municipality id, for example GM0363")
    selection_group.add_argument("--retry-invalid", action="store_true",
                                 help="only validate the municipalities in invalidMunicipalities without cleaning the previous output")
    return parser.parse_args()


def load_municipality_entries(municipalities_file: Path) -> list[dict]:
    with municipalities_file.open() as file:
        return yaml.safe_load(file)


def write_municipalities_geojson(municipality_entries: list[dict]) -> None:
    feature_collection = {
        "type": "FeatureCollection",
        "features": [
            {
                "type": "Feature",
                "geometry": {
                    "type": "Point",
                    "coordinates": [entry["start-coordinate-longitude"], entry["start-coordinate-latitude"]],
                },
                "properties": {
                    "name": entry["name"],
                    "municipality_id": entry["municipality-id"],
                    "date_last_check": entry["date-last-check"],
                },
            }
            for entry in municipality_entries
        ],
    }
    MUNICIPALITIES_GEOJSON_FILE.write_text(json.dumps(feature_collection, indent=2) + "\n")


def create_municipalities(municipality_entries: list[dict]) -> list[Municipality]:
    return [
        Municipality(
            name=entry["name"],
            municipality_id=entry["municipality-id"],
            destination_latitude=entry["start-coordinate-latitude"],
            destination_longitude=entry["start-coordinate-longitude"],
        )
        for entry in municipality_entries
    ]


def select_invalid_municipalities(municipalities: list[Municipality]) -> list[Municipality]:
    if not INVALID_MUNICIPALITIES_DIRECTORY.exists():
        return []

    invalid_municipality_ids = {directory.name for directory in INVALID_MUNICIPALITIES_DIRECTORY.iterdir() if directory.is_dir()}
    known_municipality_ids = {municipality.municipality_id for municipality in municipalities}
    for unknown_municipality_id in sorted(invalid_municipality_ids - known_municipality_ids):
        print(f"warning: {unknown_municipality_id} not found in municipalities file, skipping", file=sys.stderr)

    return [municipality for municipality in municipalities if municipality.municipality_id in invalid_municipality_ids]


def clean_output() -> None:
    VALID_LOG_FILE.write_text("")
    if INVALID_MUNICIPALITIES_DIRECTORY.exists():
        shutil.rmtree(INVALID_MUNICIPALITIES_DIRECTORY)
    INVALID_MUNICIPALITIES_DIRECTORY.mkdir()


def create_request_body(municipality: Municipality) -> dict:
    return {
        "area": {
            "type": "municipality",
            "id": municipality.municipality_id,
        },
        "vehicle": {
            "type": "car",
            "width": 0,
            "height": 0,
            "weight": 0,
            "length": 0,
            "axleLoad": 0,
            "hasTrailer": False,
            "emissionClass": "euro_1",
            "fuelTypes": ["hydrogen"],
        },
        "includeAccessibleRoadSections": True,
        "destination": {
            "latitude": municipality.destination_latitude,
            "longitude": municipality.destination_longitude,
        },
    }


def validate(url: str, request_body: dict) -> ValidationResult:
    request = urllib.request.Request(
        url,
        data=json.dumps(request_body).encode("utf-8"),
        headers={"Accept": "application/geo+json", "Content-Type": "application/json"},
        method="POST",
    )
    try:
        with urllib.request.urlopen(request, timeout=REQUEST_TIMEOUT_SECONDS) as response:
            http_status = response.status
            response_body = response.read()
    except urllib.error.HTTPError as error:
        return ValidationResult(status=STATUS_FAILED, finding=f"unexpected http status {error.code}", http_status=error.code,
                                response_body=error.read())
    except Exception as error:
        return ValidationResult(status=STATUS_FAILED, finding=f"request failed: {error}")

    if http_status != 200:
        return ValidationResult(status=STATUS_FAILED, finding=f"unexpected http status {http_status}", http_status=http_status,
                                response_body=response_body)

    try:
        feature_collection = json.loads(response_body)
    except ValueError:
        return ValidationResult(status=STATUS_FAILED, finding="response is not valid json", http_status=http_status,
                                response_body=response_body)

    road_section_segments = [feature for feature in feature_collection.get("features", [])
                             if feature.get("properties", {}).get("type") == "roadSectionSegment"]
    if not road_section_segments:
        return ValidationResult(status=STATUS_INVALID, finding="no roadSectionSegment features in response", http_status=http_status,
                                response_body=response_body, road_section_segment_count=0, accessible_count=0)

    accessible_count = sum(1 for feature in road_section_segments if feature["properties"].get("accessible") is True)
    accessible_percentage = accessible_count / len(road_section_segments) * 100
    is_valid = accessible_percentage > MINIMUM_ACCESSIBLE_PERCENTAGE
    return ValidationResult(
        status=STATUS_VALID if is_valid else STATUS_INVALID,
        finding=f"accessible percentage {accessible_percentage:.2f}% is {'' if is_valid else 'not '}above {MINIMUM_ACCESSIBLE_PERCENTAGE:.0f}%",
        http_status=http_status,
        response_body=response_body,
        road_section_segment_count=len(road_section_segments),
        accessible_count=accessible_count,
        accessible_percentage=accessible_percentage,
    )


def write_invalid_municipality(municipality: Municipality, url: str, request_body: dict, result: ValidationResult) -> None:
    municipality_directory = INVALID_MUNICIPALITIES_DIRECTORY / municipality.municipality_id
    municipality_directory.mkdir()

    request_script = municipality_directory / "request.sh"
    request_script.write_text(create_curl_command(url, request_body))
    request_script.chmod(0o755)

    if result.response_body is not None:
        try:
            pretty_response = json.dumps(json.loads(result.response_body), indent=2)
            (municipality_directory / "response.geojson").write_text(pretty_response + "\n")
        except ValueError:
            (municipality_directory / "response.txt").write_bytes(result.response_body)

    findings = [
        f"municipality: {municipality.name} ({municipality.municipality_id})",
        f"destination: latitude={municipality.destination_latitude}, longitude={municipality.destination_longitude}",
        f"httpStatus: {result.http_status if result.http_status is not None else 'n/a'}",
    ]
    if result.road_section_segment_count is not None:
        findings.append(f"roadSectionSegments: {result.road_section_segment_count}")
        findings.append(f"accessible: {result.accessible_count}")
    if result.accessible_percentage is not None:
        findings.append(f"accessiblePercentage: {result.accessible_percentage:.2f}%")
    findings.append(f"finding: {result.finding}")
    (municipality_directory / "findings.log").write_text("\n".join(findings) + "\n")


def create_curl_command(url: str, request_body: dict) -> str:
    return (
        "#!/usr/bin/env bash\n"
        f"curl {shlex.quote(url)} \\\n"
        "  -H 'Accept: application/geo+json' \\\n"
        "  -H 'Content-Type: application/json' \\\n"
        f"  -d {shlex.quote(json.dumps(request_body, indent=2))} \\\n"
        "  | jq > response.geojson\n"
    )


def format_progress_detail(result: ValidationResult) -> str:
    if result.status == STATUS_VALID:
        return f"{result.accessible_percentage:6.2f}%"
    if result.accessible_percentage is not None:
        return f"{result.accessible_percentage:6.2f}% (not above {MINIMUM_ACCESSIBLE_PERCENTAGE:.0f}%)"
    return result.finding


if __name__ == "__main__":
    sys.exit(main())
