package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.AccessibilityGeoJsonFeature;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.AccessibilityGeoJsonFeatureCollection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.RoadSectionAndTrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.TrafficSign;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityGeoJsonMapperTest {

    private static final int NWB_VERSION = 20240101;

    @Mock
    private AccessibilityGeoJsonFeatureCollectionMapper accessibilityGeoJsonFeatureCollectionMapper;

    @Mock
    private AccessibilityGeoJsonFeatureMapper accessibilityGeoJsonFeatureMapper;

    @InjectMocks
    private AccessibilityGeoJsonMapper accessibilityGeoJsonMapper;

    @Mock
    private RoadSectionAndTrafficSign<DirectionalRoadSection, TrafficSign> roadSectionAndTrafficSignA;
    @Mock
    private RoadSectionAndTrafficSign<DirectionalRoadSection, TrafficSign> roadSectionAndTrafficSignB;

    @Mock
    private AccessibilityGeoJsonFeature accessibilityGeoJsonFeatureA;
    @Mock
    private AccessibilityGeoJsonFeature accessibilityGeoJsonFeatureB;

    @Mock
    private AccessibilityGeoJsonFeatureCollection accessibilityGeoJsonFeatureCollection;

    @Test
    void map_ok() {
        when(accessibilityGeoJsonFeatureMapper.map(roadSectionAndTrafficSignA, NWB_VERSION))
                .thenReturn(accessibilityGeoJsonFeatureA);
        when(accessibilityGeoJsonFeatureMapper.map(roadSectionAndTrafficSignB, NWB_VERSION))
                .thenReturn(accessibilityGeoJsonFeatureB);

        when(accessibilityGeoJsonFeatureCollectionMapper.map(List.of(accessibilityGeoJsonFeatureA,
                accessibilityGeoJsonFeatureB))).thenReturn(accessibilityGeoJsonFeatureCollection);

        assertEquals(accessibilityGeoJsonFeatureCollection,
                accessibilityGeoJsonMapper.map(List.of(roadSectionAndTrafficSignA, roadSectionAndTrafficSignB)
                        , NWB_VERSION));
    }
}