package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;

import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.AccessibilityGeoJsonFeature;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.AccessibilityGeoJsonProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSectionAndTrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.LineStringGeojson;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.directional.DirectionalRoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.directional.DirectionalTrafficSign;
import nu.ndw.nls.geometry.geojson.mappers.GeoJsonLineStringCoordinateMapper;
import org.springframework.stereotype.Component;

/**
 * Output geometry in driving direction entirely as accessible or not accessible
 */
@Component
@RequiredArgsConstructor
public class RoadSectionAccessibilityGeoJsonFeatureMapper {

    private final GeoJsonLineStringCoordinateMapper geoJsonLineStringCoordinateMapper;

    public AccessibilityGeoJsonFeature map(DirectionalRoadSectionAndTrafficSign roadSectionAndTrafficSign,
            int version, long id) {

        DirectionalRoadSection directionalRoadSection = roadSectionAndTrafficSign.getRoadSection();
        DirectionalTrafficSign directionalTrafficSign = roadSectionAndTrafficSign.getTrafficSign();

        return AccessibilityGeoJsonFeature
                .builder()
                .id(id)
                .geometry(LineStringGeojson.builder()
                        .coordinates(geoJsonLineStringCoordinateMapper.map(directionalRoadSection.getGeometry()))
                        .build())
                .properties(AccessibilityGeoJsonProperties.builder()
                        .id(directionalRoadSection.getNwbRoadSectionId())
                        .versionId(version)
                        .accessible(directionalRoadSection.getAccessible())
                        .trafficSignType(directionalTrafficSign != null ?
                                directionalTrafficSign.getTrafficSignType() : null)
                        .windowTimes(directionalTrafficSign != null ?
                                directionalTrafficSign.getWindowTimes() : null)
                        .build())
                .build();
    }



}
