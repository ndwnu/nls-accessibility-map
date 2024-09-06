package nu.ndw.nls.accessibilitymap.backend.mappers;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.model.MunicipalityBoundingBox;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.GeometryJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.MunicipalityFeatureCollectionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.MunicipalityFeatureCollectionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.MunicipalityFeatureJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.MunicipalityPropertiesJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.PointJson;
import nu.ndw.nls.accessibilitymap.backend.municipality.model.Municipality;
import nu.ndw.nls.geometry.rounding.dto.RoundDoubleConfiguration;
import nu.ndw.nls.geometry.rounding.mappers.RoundDoubleMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MunicipalityFeatureMapper {

    private final RoundDoubleMapper doubleMapper;

    private static final String EMPTY_STRING = "";

    public MunicipalityFeatureCollectionJson mapToMunicipalitiesToGeoJson(Collection<Municipality> municipalities) {
        return new MunicipalityFeatureCollectionJson(TypeEnum.FEATURE_COLLECTION,
                municipalities.stream().map(this::mapMunicipality).toList());
    }

    private MunicipalityFeatureJson mapMunicipality(Municipality m) {
        List<List<Double>> bounds = mapMunicipalityBounds(m.getBounds());

        String requestExemptionUrlString = Optional.ofNullable(m.getRequestExemptionUrl())
                .map(URL::toString)
                .orElse(EMPTY_STRING);

        return new MunicipalityFeatureJson(MunicipalityFeatureJson.TypeEnum.FEATURE, m.getMunicipalityId(),
                mapStartPoint(m))
                .properties(new MunicipalityPropertiesJson(
                        m.getName(),
                        (int) m.getSearchDistanceInMetres(),
                        bounds,
                        requestExemptionUrlString,
                        m.getDateLastCheck()));
    }

    private PointJson mapStartPoint(Municipality m) {
        double roundedX = roundCoordinate(m.getStartPoint().getX());
        double roundedY = roundCoordinate(m.getStartPoint().getY());
        return mapPointJson(roundedX, roundedY);
    }

    private static PointJson mapPointJson(double x, double y) {
        return new PointJson(List.of(x, y), GeometryJson.TypeEnum.POINT);
    }

    private List<List<Double>> mapMunicipalityBounds(MunicipalityBoundingBox boundingBox) {
        List<Double> from = List.of(roundCoordinate(boundingBox.longitudeFrom()),
                roundCoordinate(boundingBox.latitudeFrom()));
        List<Double> to = List.of(roundCoordinate(boundingBox.longitudeTo()),
                roundCoordinate(boundingBox.latitudeTo()));
        return List.of(from, to);
    }

    private Double roundCoordinate(double unRounded) {
        return doubleMapper.round(unRounded, RoundDoubleConfiguration.ROUND_7_HALF_UP);
    }
}
