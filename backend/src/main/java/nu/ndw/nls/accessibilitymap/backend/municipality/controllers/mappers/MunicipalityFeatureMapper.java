package nu.ndw.nls.accessibilitymap.backend.municipality.controllers.mappers;

import static nu.ndw.nls.accessibilitymap.backend.generated.model.v1.MunicipalityFeatureCollectionJson.TypeEnum.FEATURE_COLLECTION;
import static nu.ndw.nls.accessibilitymap.backend.generated.model.v1.MunicipalityFeatureJson.TypeEnum.FEATURE;
import static nu.ndw.nls.geojson.geometry.model.GeometryJson.TypeEnum.POINT;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.MunicipalityFeatureCollectionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.MunicipalityFeatureJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.MunicipalityPropertiesJson;
import nu.ndw.nls.accessibilitymap.backend.municipality.repository.dto.Municipality;
import nu.ndw.nls.accessibilitymap.backend.municipality.repository.dto.MunicipalityBoundingBox;
import nu.ndw.nls.geojson.geometry.model.PointJson;
import nu.ndw.nls.geometry.rounding.dto.RoundDoubleConfiguration;
import nu.ndw.nls.geometry.rounding.mappers.RoundDoubleMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MunicipalityFeatureMapper {

    private static final String EMPTY_STRING = "";

    private final RoundDoubleMapper doubleMapper;

    public MunicipalityFeatureCollectionJson mapToMunicipalitiesToGeoJson(Collection<Municipality> municipalities) {

        return new MunicipalityFeatureCollectionJson(FEATURE_COLLECTION,
                municipalities.stream().map(this::mapMunicipality).toList());
    }

    private MunicipalityFeatureJson mapMunicipality(Municipality municipality) {

        List<List<Double>> bounds = mapMunicipalityBounds(municipality.bounds());

        String requestExemptionUrlString = Optional.ofNullable(municipality.requestExemptionUrl())
                .map(URL::toString)
                .orElse(EMPTY_STRING);

        return new MunicipalityFeatureJson(
                FEATURE,
                municipality.municipalityId(),
                mapStartPoint(municipality),
                new MunicipalityPropertiesJson(
                        municipality.name(),
                        municipality.searchDistanceInMetres().intValue(),
                        bounds,
                        requestExemptionUrlString,
                        municipality.dateLastCheck()));
    }

    private PointJson mapStartPoint(Municipality municipality) {

        double roundedX = roundCoordinate(municipality.startCoordinateLongitude());
        double roundedY = roundCoordinate(municipality.startCoordinateLatitude());

        return mapPointJson(roundedX, roundedY);
    }

    private static PointJson mapPointJson(double x, double y) {
        return new PointJson(List.of(x, y), POINT);
    }

    private List<List<Double>> mapMunicipalityBounds(MunicipalityBoundingBox boundingBox) {

        List<Double> from = List.of(roundCoordinate(boundingBox.longitudeFrom()), roundCoordinate(boundingBox.latitudeFrom()));
        List<Double> to = List.of(roundCoordinate(boundingBox.longitudeTo()), roundCoordinate(boundingBox.latitudeTo()));

        return List.of(from, to);
    }

    private Double roundCoordinate(double unRounded) {

        return doubleMapper.round(unRounded, RoundDoubleConfiguration.ROUND_7_HALF_UP);
    }
}
