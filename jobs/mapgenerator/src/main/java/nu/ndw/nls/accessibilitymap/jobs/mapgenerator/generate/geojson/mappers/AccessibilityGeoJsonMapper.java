package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;

import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.AccessibilityGeoJsonFeature;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.AccessibilityGeoJsonFeatureCollection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.RoadSectionAndTrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.TrafficSign;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessibilityGeoJsonMapper {

    private final AccessibilityGeoJsonFeatureCollectionMapper accessibilityGeoJsonFeatureCollectionMapper;

    private final AccessibilityGeoJsonFeatureMapper accessibilityGeoJsonFeatureMapper;

    public AccessibilityGeoJsonFeatureCollection map(List<RoadSectionAndTrafficSign<DirectionalRoadSection,
            TrafficSign>>  idToRoadSectionAndTrafficSign, int nwbVersion) {

        List<AccessibilityGeoJsonFeature> features = idToRoadSectionAndTrafficSign.stream()
                .map(directionalRoadSectionAndTrafficSign ->
                        accessibilityGeoJsonFeatureMapper.map(directionalRoadSectionAndTrafficSign, nwbVersion))
                .toList();

        return accessibilityGeoJsonFeatureCollectionMapper.map(features);
    }

}
