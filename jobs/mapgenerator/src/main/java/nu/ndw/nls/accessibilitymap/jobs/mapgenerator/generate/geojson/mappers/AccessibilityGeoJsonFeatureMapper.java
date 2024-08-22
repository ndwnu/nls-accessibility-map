package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;

import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.AccessibilityGeoJsonFeature;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.AccessibilityGeoJsonProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.LineStringGeojson;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.RoadSectionAndTrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.TrafficSign;
import nu.ndw.nls.geometry.geojson.mappers.GeoJsonLineStringCoordinateMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessibilityGeoJsonFeatureMapper {

    private final GeoJsonLineStringCoordinateMapper geoJsonLineStringCoordinateMapper;

    public AccessibilityGeoJsonFeature map(
            RoadSectionAndTrafficSign<DirectionalRoadSection, TrafficSign> roadSectionAndTrafficSign, int version) {

        DirectionalRoadSection directionalRoadSection = roadSectionAndTrafficSign.getRoadSection();
        TrafficSign trafficSign = roadSectionAndTrafficSign.getTrafficSign();

        return AccessibilityGeoJsonFeature
                .builder()
                .id(directionalRoadSection.getRoadSectionId())
                .geometry(LineStringGeojson.builder()
                        .coordinates(geoJsonLineStringCoordinateMapper.map(directionalRoadSection.getGeometry()))
                        .build())
                .properties(AccessibilityGeoJsonProperties.builder()
                        .id(directionalRoadSection.getRoadSectionId())
                        .versionId(version)
                        .accessible(directionalRoadSection.isAccessible())
                        .trafficSignType(trafficSign != null ? trafficSign.getTrafficSignType() : null)
                        .windowTimes(trafficSign != null ? trafficSign.getWindowTimes() : null)
                        .build())
                .build();
    }


}
