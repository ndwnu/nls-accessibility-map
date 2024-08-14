package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import nu.ndw.nls.accessibilitymap.accessibility.model.RoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.AccessibilityGeoJsonFeature;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.AccessibilityGeoJsonFeatureCollection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityGeoJsonMapperTest {

    private static final int NWB_VERSION = 20240101;
    @Mock
    private DirectionalRoadSectionMapper directionalRoadSectionMapper;

    @Mock
    private AccessibilityGeoJsonFeatureCollectionMapper accessibilityGeoJsonFeatureCollectionMapper;

    @Mock
    private AccessibilityGeoJsonFeatureMapper accessibilityGeoJsonFeatureMapper;

    @InjectMocks
    private AccessibilityGeoJsonMapper accessibilityGeoJsonMapper;

    @Mock
    private SortedMap<Integer, RoadSection> idRoadSectionMap;

    @Mock
    private RoadSection roadSectionA;
    @Mock
    private RoadSection roadSectionB;
    @Mock
    private RoadSection roadSectionC;
    @Mock
    private DirectionalRoadSection directionalRoadSectionAForward;
    @Mock
    private DirectionalRoadSection directionalRoadSectionABackward;
    @Mock
    private DirectionalRoadSection directionalRoadSectionBBackward;
    @Mock
    private AccessibilityGeoJsonFeature accessibilityGeoJsonFeatureAForward;
    @Mock
    private AccessibilityGeoJsonFeature accessibilityGeoJsonFeatureABackward;
    @Mock
    private AccessibilityGeoJsonFeature accessibilityGeoJsonFeatureBBackward;
    @Mock
    private AccessibilityGeoJsonFeatureCollection accessibilityGeoJsonFeatureCollection;

    @Test
    void map_ok() {
        when(idRoadSectionMap.values()).thenReturn(List.of(roadSectionA,roadSectionB,roadSectionC));

        when(directionalRoadSectionMapper.map(roadSectionA)).thenReturn(List.of(directionalRoadSectionAForward,
                directionalRoadSectionABackward));
        when(directionalRoadSectionMapper.map(roadSectionB)).thenReturn(List.of(directionalRoadSectionBBackward));
        when(directionalRoadSectionMapper.map(roadSectionC)).thenReturn(Collections.emptyList());
        when(accessibilityGeoJsonFeatureMapper.map(directionalRoadSectionAForward, NWB_VERSION))
                .thenReturn(accessibilityGeoJsonFeatureAForward);
        when(accessibilityGeoJsonFeatureMapper.map(directionalRoadSectionABackward, NWB_VERSION))
                .thenReturn(accessibilityGeoJsonFeatureABackward);
        when(accessibilityGeoJsonFeatureMapper.map(directionalRoadSectionBBackward, NWB_VERSION))
                .thenReturn(accessibilityGeoJsonFeatureBBackward);


        when(accessibilityGeoJsonFeatureCollectionMapper.map(List.of(accessibilityGeoJsonFeatureAForward,
                accessibilityGeoJsonFeatureABackward, accessibilityGeoJsonFeatureBBackward)))
                .thenReturn(accessibilityGeoJsonFeatureCollection);

        assertEquals(accessibilityGeoJsonFeatureCollection,
                accessibilityGeoJsonMapper.map(idRoadSectionMap, NWB_VERSION));
    }
}