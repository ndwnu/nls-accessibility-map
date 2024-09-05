package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.AccessibilityGeoJsonFeature;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.AccessibilityGeoJsonFeatureCollection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSectionAndTrafficSignGroupedById;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.services.DirectionalRoadSectionSplitAtTrafficSignService;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.suppliers.GeoJsonIdSequenceSupplier;
import org.springframework.stereotype.Component;

/**
 * This mapper outputs the NWB geometry in driving direction and has the geometry split at the traffic sign and marks
 * the area prior the traffic sign as accessible and the area behind the traffic sign as not accessible
 */
@Component
@RequiredArgsConstructor
public class TrafficSignSplitRoadSectionAccessibilityGeoJsonMapper {

    private final AccessibilityGeoJsonFeatureCollectionMapper accessibilityGeoJsonFeatureCollectionMapper;

    private final RoadSectionAccessibilityGeoJsonFeatureMapper roadSectionAccessibilityGeoJsonFeatureMapper;

    private final DirectionalRoadSectionSplitAtTrafficSignService directionalRoadSectionSplitAtTrafficSignService;

    public AccessibilityGeoJsonFeatureCollection map(GeoJsonIdSequenceSupplier geoJsonIdSequenceSupplier,
            List<DirectionalRoadSectionAndTrafficSignGroupedById> directionalRoadSectionAndTrafficSignGroupedByIds,
            int nwbVersion) {

        List<AccessibilityGeoJsonFeature> features = directionalRoadSectionAndTrafficSignGroupedByIds.stream()
                .flatMap(trafficSignEnrichedRoadSection -> Stream.of( trafficSignEnrichedRoadSection.getBackward(),
                                                                      trafficSignEnrichedRoadSection.getForward()))
                .filter(Objects::nonNull)
                .map(directionalRoadSectionSplitAtTrafficSignService::split)
                .flatMap(Collection::stream)
                .map(directionalRoadSectionAndTrafficSign ->
                        roadSectionAccessibilityGeoJsonFeatureMapper.map(directionalRoadSectionAndTrafficSign,
                                nwbVersion, geoJsonIdSequenceSupplier.next()))
                .toList();

        return accessibilityGeoJsonFeatureCollectionMapper.map(features);
    }

}
