package nu.ndw.nls.accessibilitymap.backend.mappers;

import java.util.Collection;
import java.util.List;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.FeatureCollectionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.FeatureCollectionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.FeatureJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.GeometryJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.MunicipalityPropertiesJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.PointJson;
import nu.ndw.nls.accessibilitymap.backend.model.Municipality;
import org.springframework.stereotype.Component;

@Component
public class MunicipalityMapper {

    public FeatureCollectionJson mapToMunicipalitiesToGeoJSON(Collection<Municipality> municipalities) {
        return new FeatureCollectionJson(TypeEnum.FEATURECOLLECTION,
                municipalities.stream().map(this::mapMunicipality).toList());
    }

    private FeatureJson mapMunicipality(Municipality m) {
        return new FeatureJson(FeatureJson.TypeEnum.FEATURE, m.getMunicipalityId(), mapStartPoint(m))
                .properties(new MunicipalityPropertiesJson(m.getName(), (int) m.getSearchDistanceInMetres()));
    }

    private PointJson mapStartPoint(Municipality m) {
        return new PointJson(GeometryJson.TypeEnum.POINT)
                .coordinates(List.of(m.getStartPoint().getX(), m.getStartPoint().getY()));
    }
}
