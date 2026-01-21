package nu.ndw.nls.accessibilitymap.backend.municipality.controller.mapper;

import static nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.MunicipalityFeatureCollectionJson.TypeEnum.FEATURE_COLLECTION;
import static nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.MunicipalityFeatureJson.TypeEnum.FEATURE;
import static nu.ndw.nls.geojson.geometry.model.GeometryJson.TypeEnum.POINT;

import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.backend.municipality.repository.dto.Municipality;
import nu.ndw.nls.accessibilitymap.backend.municipality.repository.dto.MunicipalityBoundingBox;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.MunicipalityFeatureCollectionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.MunicipalityFeatureJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.MunicipalityPropertiesJson;
import nu.ndw.nls.geojson.geometry.model.PointJson;
import nu.ndw.nls.geometry.rounding.dto.RoundDoubleConfiguration;
import nu.ndw.nls.geometry.rounding.mappers.RoundDoubleMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MunicipalityFeatureMapperV2 {

    private final RoundDoubleMapper doubleMapper;

    public MunicipalityFeatureCollectionJson mapToMunicipalitiesToGeoJson(Collection<Municipality> municipalities) {
        return new MunicipalityFeatureCollectionJson(FEATURE_COLLECTION, municipalities.stream().map(this::mapMunicipality).toList());
    }

    private MunicipalityFeatureJson mapMunicipality(Municipality municipality) {
        List<List<Double>> bounds = mapMunicipalityBounds(municipality.bounds());

        return new MunicipalityFeatureJson(
                FEATURE,
                municipality.id(),
                mapStartPoint(municipality),
                new MunicipalityPropertiesJson(
                        municipality.name(),
                        municipality.searchDistanceInMetres(),
                        bounds,
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
