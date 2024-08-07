package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;

import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.AccessibilityGeoJsonFeature;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.AccessibilityProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.LineStringGeojson;
import nu.ndw.nls.geometry.geojson.mappers.GeoJsonLineStringCoordinateMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessibilityGeoJsonFeatureMapper {

    private final GeoJsonLineStringCoordinateMapper geoJsonLineStringCoordinateMapper;

    public AccessibilityGeoJsonFeature map(DirectionalRoadSection directionalRoadSection, int version) {

        return AccessibilityGeoJsonFeature
                .builder()
                .id(directionalRoadSection.getRoadSectionId())
                .geometry(LineStringGeojson.builder()
                        .coordinates(geoJsonLineStringCoordinateMapper.map(directionalRoadSection.getGeometry()))
                        .build())
                .properties(AccessibilityProperties.builder()
                        .id(directionalRoadSection.getRoadSectionId())
                        .versionId(version)
                        .accessible(directionalRoadSection.isAccessible())
                        //.validFrom() dump?
                        .build())
                .build();
    }

}
