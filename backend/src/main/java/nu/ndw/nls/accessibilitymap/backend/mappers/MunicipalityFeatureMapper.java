package nu.ndw.nls.accessibilitymap.backend.mappers;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.FeatureCollectionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.FeatureCollectionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.FeatureJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.GeometryJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.MunicipalityPropertiesJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.PointJson;
import nu.ndw.nls.accessibilitymap.backend.model.Municipality;
import nu.ndw.nls.accessibilitymap.backend.model.MunicipalityBoundingBox;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class MunicipalityFeatureMapper {




    public FeatureCollectionJson mapToMunicipalitiesToGeoJSON(Collection<Municipality> municipalities) {
        return new FeatureCollectionJson(TypeEnum.FEATURECOLLECTION,
                municipalities.stream().map(this::mapMunicipality).toList());
    }

    private FeatureJson mapMunicipality(Municipality m) {
        List<List<Double>> bounds = mapMunicipalityBounds(m.getBounds());

        URL requestExemptionUrl = m.getRequestExemptionUrl();
        String requestExemptionUrlString = "";
        if (requestExemptionUrl != null) {
            requestExemptionUrlString = requestExemptionUrl.toString();
        }

        return new FeatureJson(FeatureJson.TypeEnum.FEATURE, m.getMunicipalityId(), mapStartPoint(m))
                .properties(new MunicipalityPropertiesJson(
                        m.getName(),
                        (int) m.getSearchDistanceInMetres(),
                        bounds,
                        requestExemptionUrlString));
    }

    private PointJson mapStartPoint(Municipality m) {
        return mapPointJson(m.getStartPoint().getX(), m.getStartPoint().getY());
    }

    private static PointJson mapPointJson(double x, double y) {
        return new PointJson(GeometryJson.TypeEnum.POINT)
                .coordinates(List.of(x, y));
    }

    private List<List<Double>> mapMunicipalityBounds(MunicipalityBoundingBox boundingBox) {
        List<Double> from = List.of(
                boundingBox.longitudeFrom(), boundingBox.latitudeFrom());
        List<Double> to = List.of(
                boundingBox.longitudeTo(), boundingBox.latitudeTo());
        return List.of(from, to);
    }
}
