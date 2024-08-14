package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;

import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.AccessibilityGeoJsonFeature;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.AccessibilityGeoJsonFeatureCollection;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessibilityGeoJsonFeatureCollectionMapper {

    public AccessibilityGeoJsonFeatureCollection map(List<AccessibilityGeoJsonFeature> accessibilityGeoJsonFeatures) {
        return AccessibilityGeoJsonFeatureCollection.builder()
                .features(accessibilityGeoJsonFeatures)
                .build();
    }

}
