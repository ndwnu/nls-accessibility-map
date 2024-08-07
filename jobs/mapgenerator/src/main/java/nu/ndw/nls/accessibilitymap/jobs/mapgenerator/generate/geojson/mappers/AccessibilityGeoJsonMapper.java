package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;

import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.model.RoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.AccessibilityGeoJsonFeature;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.AccessibilityGeoJsonFeatureCollection;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessibilityGeoJsonMapper {

    private final DirectionalRoadSectionMapper directionalRoadSectionMapper;

    private final AccessibilityGeoJsonFeatureCollectionMapper accessibilityGeoJsonFeatureCollectionMapper;

    private final AccessibilityGeoJsonFeatureMapper accessibilityGeoJsonFeatureMapper;

    public AccessibilityGeoJsonFeatureCollection map(SortedMap<Integer, RoadSection> accessiblityResult,
            int nwbVersion) {

        List<AccessibilityGeoJsonFeature> features = accessiblityResult.values().stream()
                .map(directionalRoadSectionMapper::map)
                .flatMap(Collection::stream)
                .map(directionalRoadSection ->
                        accessibilityGeoJsonFeatureMapper.map(directionalRoadSection, nwbVersion))
                .toList();

        return accessibilityGeoJsonFeatureCollectionMapper.map(features);
    }

}
