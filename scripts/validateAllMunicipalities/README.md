# Validate all municipalities

Validates the start location (destination) of every municipality in
`backend/src/main/resources/data/municipalities.yml` by requesting the accessibility map for each municipality and checking that the
response is plausible.

For every municipality a `POST /v2/accessibility.geojson` request is sent with an unrestricted vehicle (car, all dimensions `0`,
`euro_1`, `hydrogen`) and `includeAccessibleRoadSections: true`. Since such a vehicle should be able to reach nearly every road in the
municipality, a low accessible percentage indicates a badly placed start location.

A municipality is:

| Status    | Meaning                                                                                                          |
|-----------|------------------------------------------------------------------------------------------------------------------|
| `VALID`   | More than 80% of the `roadSectionSegment` features in the response are `accessible`                              |
| `INVALID` | 80% or less of the `roadSectionSegment` features are `accessible`, or the response contains no road section segments |
| `FAILED`  | The request failed: non `200` http status, timeout (120 seconds), connection error or a response that is not valid json |

Requests are throttled to a maximum of 2 requests per second.

## Requirements

- Python 3.9+
- `jq` (only used by the generated `request.sh` scripts)
- `curl` (only used by `downloadTrafficSigns.sh` and the generated `request.sh` scripts)

```bash
pip install -r requirements.txt
```

## Usage

```bash
./validate_all_municipalities.py [--municipalities-file FILE] [--base-url URL] [--municipality ID | --retry-invalid]
```

### Options

| Option                       | Default                                                               | Description                                                                                                                                                     |
|------------------------------|-----------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `--municipalities-file FILE` | `../../backend/src/main/resources/data/municipalities.yml`            | Municipalities yaml file to validate                                                                                                                            |
| `--base-url URL`             | `https://data.ndw.nu/api/rest/static-road-data/accessibility-map`     | Base url of the accessibility map api. `/v2/accessibility.geojson` is appended                                                                                  |
| `--municipality ID`          |                                                                       | Only validate this municipality id, for example `GM0363`. Previous output is cleaned                                                                            |
| `--retry-invalid`            |                                                                       | Only validate the municipalities that have a directory in `invalidMunicipalities`. Previous output is kept, valid ones are appended to `valid.log` and their directory is removed |
| `-h`, `--help`               |                                                                       | Show help                                                                                                                                                       |

`--municipality` and `--retry-invalid` cannot be combined.

### Examples

Validate all municipalities against production:

```bash
./validate_all_municipalities.py
```

Validate a single municipality against a locally running backend:

```bash
./validate_all_municipalities.py --base-url http://localhost:8080/api/rest/static-road-data/accessibility-map --municipality GM0363
```

After fixing start locations in `municipalities.yml`, only re-validate the municipalities that were invalid or failed:

```bash
./validate_all_municipalities.py --retry-invalid
```

## Output

Progress is printed per municipality, followed by a summary:

```
[  1/342] GM0014 Groningen       VALID     97.12%
[  2/342] GM0034 Almere          INVALID   12.30% (not above 80%)
...

valid: 330, invalid: 10, failed: 2
```

All output is written next to the script and is ignored by git:

| File / directory                     | Description                                                                                       |
|--------------------------------------|---------------------------------------------------------------------------------------------------|
| `municipalities.geojson`             | All municipality start locations as points, regenerated on every run                              |
| `valid.log`                          | Ids of all valid municipalities, one per line                                                     |
| `invalidMunicipalities/<id>/`        | One directory per invalid or failed municipality                                                  |
| `invalidMunicipalities/<id>/findings.log`     | Municipality name, destination, http status, segment counts, accessible percentage and finding |
| `invalidMunicipalities/<id>/request.sh`       | Executable curl script that reproduces the request and writes `response.geojson`        |
| `invalidMunicipalities/<id>/response.geojson` | The pretty printed response, or `response.txt` when the response is not valid json      |

Without `--retry-invalid`, `valid.log` and `invalidMunicipalities` are cleaned at the start of every run.

## Analysing in QGIS

`debug.qgs` is a QGIS project with the following layers:

- `municipalities.geojson` — the municipality start locations
- `test/response.geojson` — copy the `response.geojson` of an invalid municipality here to inspect it
- `trafficSigns.geojson` — the current traffic signs
- NWB wegvakken (PDOK WFS) and an OpenBasisKaart background map

Download the current traffic signs with:

```bash
./downloadTrafficSigns.sh
```
