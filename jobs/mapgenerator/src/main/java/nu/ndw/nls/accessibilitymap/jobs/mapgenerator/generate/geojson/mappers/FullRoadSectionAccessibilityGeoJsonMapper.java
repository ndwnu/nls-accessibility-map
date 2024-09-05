package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.AccessibilityGeoJsonFeature;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.AccessibilityGeoJsonFeatureCollection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSectionAndTrafficSignGroupedById;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.suppliers.GeoJsonIdSequenceSupplier;
import org.springframework.stereotype.Component;

/**
 * This mapper outputs the full NWB geometry in driving direction, along with the accessibility
 */
@Component
@RequiredArgsConstructor
public class FullRoadSectionAccessibilityGeoJsonMapper {

    private final AccessibilityGeoJsonFeatureCollectionMapper accessibilityGeoJsonFeatureCollectionMapper;

    private final RoadSectionAccessibilityGeoJsonFeatureMapper roadSectionAccessibilityGeoJsonFeatureMapper;

    public AccessibilityGeoJsonFeatureCollection map(GeoJsonIdSequenceSupplier geoJsonIdSequenceSupplier,
            List<DirectionalRoadSectionAndTrafficSignGroupedById> directionalRoadSectionAndTrafficSignGroupedByIds,
            int nwbVersion) {

        List<AccessibilityGeoJsonFeature> features = directionalRoadSectionAndTrafficSignGroupedByIds.stream()
                .flatMap(trafficSignEnrichedRoadSection -> Stream.of( trafficSignEnrichedRoadSection.getBackward(),
                        trafficSignEnrichedRoadSection.getForward()))
                .filter(Objects::nonNull)
                .map(directionalRoadSectionAndTrafficSign ->
                        roadSectionAccessibilityGeoJsonFeatureMapper.map(directionalRoadSectionAndTrafficSign,
                                nwbVersion, geoJsonIdSequenceSupplier.next()))
                .toList();

        return accessibilityGeoJsonFeatureCollectionMapper.map(features);
    }

}
