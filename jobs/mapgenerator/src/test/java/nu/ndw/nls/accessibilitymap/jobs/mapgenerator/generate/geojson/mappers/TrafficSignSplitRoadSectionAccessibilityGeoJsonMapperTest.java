package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.AccessibilityGeoJsonFeature;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.AccessibilityGeoJsonFeatureCollection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSectionAndTrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSectionAndTrafficSignGroupedById;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.services.DirectionalRoadSectionSplitAtTrafficSignService;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.util.LongSequenceSupplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrafficSignSplitRoadSectionAccessibilityGeoJsonMapperTest {

    private static final int NWB_VERSION = 20240101;
    private static final long GEOJSON_ID = 123;

    @Mock
    private AccessibilityGeoJsonFeatureCollectionMapper accessibilityGeoJsonFeatureCollectionMapper;

    @Mock
    private RoadSectionAccessibilityGeoJsonFeatureMapper roadSectionAccessibilityGeoJsonFeatureMapper;

    @Mock
    private DirectionalRoadSectionSplitAtTrafficSignService directionalRoadSectionSplitAtTrafficSignService;

    @InjectMocks
    private TrafficSignSplitRoadSectionAccessibilityGeoJsonMapper mapper;

    @Mock
    private DirectionalRoadSectionAndTrafficSign roadSectionAndTrafficSignForward;
    @Mock
    private DirectionalRoadSectionAndTrafficSign roadSectionAndTrafficSignBackward;

    @Mock
    private DirectionalRoadSectionAndTrafficSign roadSectionAndTrafficSignForwardAccessible;
    @Mock
    private DirectionalRoadSectionAndTrafficSign roadSectionAndTrafficSignForwardInaccessible;
    @Mock
    private DirectionalRoadSectionAndTrafficSign roadSectionAndTrafficSignBackwardAccessible;
    @Mock
    private DirectionalRoadSectionAndTrafficSign roadSectionAndTrafficSignBackwardInaccessible;


    @Mock
    private DirectionalRoadSectionAndTrafficSignGroupedById directionalRoadSectionAndTrafficSignGroupedById;
    @Mock
    private AccessibilityGeoJsonFeature accessibilityGeoJsonFeatureForwardsAccessible;
    @Mock
    private AccessibilityGeoJsonFeature accessibilityGeoJsonFeatureForwardsInaccessible;
    @Mock
    private AccessibilityGeoJsonFeature accessibilityGeoJsonFeatureBackwardsAccessible;
    @Mock
    private AccessibilityGeoJsonFeature accessibilityGeoJsonFeatureBackwardsInaccessible;

    @Mock
    private AccessibilityGeoJsonFeatureCollection accessibilityGeoJsonFeatureCollection;

    @Mock
    private LongSequenceSupplier geoJsonIdSequenceSupplier;


    @Test
    void map_ok() {
        when(directionalRoadSectionAndTrafficSignGroupedById.getForward()).thenReturn(roadSectionAndTrafficSignForward);
        when(directionalRoadSectionAndTrafficSignGroupedById.getBackward()).thenReturn(roadSectionAndTrafficSignBackward);

        // Split the directions
        when(directionalRoadSectionSplitAtTrafficSignService.split(roadSectionAndTrafficSignForward))
                .thenReturn(List.of(roadSectionAndTrafficSignForwardAccessible,
                                            roadSectionAndTrafficSignForwardInaccessible));
        when(directionalRoadSectionSplitAtTrafficSignService.split(roadSectionAndTrafficSignBackward))
                .thenReturn(List.of(roadSectionAndTrafficSignBackwardAccessible,
                        roadSectionAndTrafficSignBackwardInaccessible));

        when(geoJsonIdSequenceSupplier.next()).thenReturn(GEOJSON_ID);

        // Convert the accessible and inaccessible parts
        when(roadSectionAccessibilityGeoJsonFeatureMapper.map(roadSectionAndTrafficSignForwardAccessible, NWB_VERSION,
                GEOJSON_ID))
                .thenReturn(accessibilityGeoJsonFeatureForwardsAccessible);
        when(roadSectionAccessibilityGeoJsonFeatureMapper.map(roadSectionAndTrafficSignForwardInaccessible, NWB_VERSION,
                GEOJSON_ID))
                .thenReturn(accessibilityGeoJsonFeatureForwardsInaccessible);
        when(roadSectionAccessibilityGeoJsonFeatureMapper.map(roadSectionAndTrafficSignBackwardAccessible, NWB_VERSION,
                GEOJSON_ID))
                .thenReturn(accessibilityGeoJsonFeatureBackwardsAccessible);
        when(roadSectionAccessibilityGeoJsonFeatureMapper.map(roadSectionAndTrafficSignBackwardInaccessible, NWB_VERSION,
                GEOJSON_ID))
                .thenReturn(accessibilityGeoJsonFeatureBackwardsInaccessible);

        when(accessibilityGeoJsonFeatureCollectionMapper.map(List.of(
                accessibilityGeoJsonFeatureBackwardsAccessible,
                accessibilityGeoJsonFeatureBackwardsInaccessible,
                accessibilityGeoJsonFeatureForwardsAccessible,
                accessibilityGeoJsonFeatureForwardsInaccessible)
        )).thenReturn(accessibilityGeoJsonFeatureCollection);

        assertEquals(accessibilityGeoJsonFeatureCollection,
                mapper.map(geoJsonIdSequenceSupplier, List.of(directionalRoadSectionAndTrafficSignGroupedById),
                        NWB_VERSION));
    }
}