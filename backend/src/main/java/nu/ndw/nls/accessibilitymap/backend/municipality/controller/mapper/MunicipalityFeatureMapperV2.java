package nu.ndw.nls.accessibilitymap.backend.municipality.controller.mapper;

import static nu.ndw.nls.accessibilitymap.backend.generated.model.v2.Geometry.TypeEnum.POINT;
import static nu.ndw.nls.accessibilitymap.backend.generated.model.v2.MunicipalityFeature.TypeEnum.FEATURE;
import static nu.ndw.nls.accessibilitymap.backend.generated.model.v2.MunicipalityFeatureCollection.TypeEnum.FEATURE_COLLECTION;

import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v2.MunicipalityFeature;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v2.MunicipalityFeatureCollection;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v2.MunicipalityProperties;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v2.Point;
import nu.ndw.nls.accessibilitymap.backend.municipality.repository.dto.Municipality;
import nu.ndw.nls.accessibilitymap.backend.municipality.repository.dto.MunicipalityBoundingBox;
import nu.ndw.nls.geometry.rounding.dto.RoundDoubleConfiguration;
import nu.ndw.nls.geometry.rounding.mappers.RoundDoubleMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MunicipalityFeatureMapperV2 {

    private final RoundDoubleMapper doubleMapper;

    public MunicipalityFeatureCollection mapToMunicipalitiesToGeoJson(Collection<Municipality> municipalities) {
        return new MunicipalityFeatureCollection(FEATURE_COLLECTION, municipalities.stream().map(this::mapMunicipality).toList());
    }

    private MunicipalityFeature mapMunicipality(Municipality municipality) {
        List<List<Double>> bounds = mapMunicipalityBounds(municipality.bounds());

        return new MunicipalityFeature(
                FEATURE,
                municipality.municipalityId(),
                mapStartPoint(municipality),
                new MunicipalityProperties(
                        municipality.name(),
                        municipality.searchDistanceInMetres(),
                        bounds,
                        municipality.dateLastCheck()));
    }

    private Point mapStartPoint(Municipality municipality) {
        double roundedX = roundCoordinate(municipality.startCoordinateLongitude());
        double roundedY = roundCoordinate(municipality.startCoordinateLatitude());

        return mapPointJson(roundedX, roundedY);
    }

    private static Point mapPointJson(double x, double y) {
        return Point.builder()
                .type(POINT)
                .coordinates(List.of(x, y))
                .build();
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
