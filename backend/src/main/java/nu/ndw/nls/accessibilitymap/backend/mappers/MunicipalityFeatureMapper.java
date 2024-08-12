package nu.ndw.nls.accessibilitymap.backend.mappers;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.GeometryJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.MunicipalityFeatureCollectionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.MunicipalityFeatureCollectionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.MunicipalityFeatureJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.MunicipalityPropertiesJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.PointJson;
import nu.ndw.nls.accessibilitymap.backend.model.Municipality;
import nu.ndw.nls.accessibilitymap.backend.model.MunicipalityBoundingBox;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class MunicipalityFeatureMapper {

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
        return mapPointJson(m.getStartPoint().getX(), m.getStartPoint().getY());
    }

    private static PointJson mapPointJson(double x, double y) {
        return new PointJson(List.of(x, y), GeometryJson.TypeEnum.POINT);
    }

    private List<List<Double>> mapMunicipalityBounds(MunicipalityBoundingBox boundingBox) {
        List<Double> from = List.of(boundingBox.longitudeFrom(), boundingBox.latitudeFrom());
        List<Double> to = List.of(boundingBox.longitudeTo(), boundingBox.latitudeTo());
        return List.of(from, to);
    }
}
