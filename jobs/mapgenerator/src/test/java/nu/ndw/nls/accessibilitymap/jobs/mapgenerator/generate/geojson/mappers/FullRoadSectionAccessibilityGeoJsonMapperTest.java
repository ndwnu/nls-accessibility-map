package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.AccessibilityGeoJsonFeature;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.AccessibilityGeoJsonFeatureCollection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSectionAndTrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSectionAndTrafficSignGroupedById;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.suppliers.GeoJsonIdSequenceSupplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FullRoadSectionAccessibilityGeoJsonMapperTest {

    private static final int NWB_VERSION = 20240101;
    private static final long GEOJSON_ID = 123;

    @Mock
    private AccessibilityGeoJsonFeatureCollectionMapper accessibilityGeoJsonFeatureCollectionMapper;

    @Mock
    private RoadSectionAccessibilityGeoJsonFeatureMapper roadSectionAccessibilityGeoJsonFeatureMapper;

    @InjectMocks
    private FullRoadSectionAccessibilityGeoJsonMapper fullRoadSectionAccessibilityGeoJsonMapper;

    @Mock
    private DirectionalRoadSectionAndTrafficSign roadSectionAndTrafficSignForward;
    @Mock
    private DirectionalRoadSectionAndTrafficSign roadSectionAndTrafficSignBackward;


    @Mock
    private DirectionalRoadSectionAndTrafficSignGroupedById directionalRoadSectionAndTrafficSignGroupedById;
    @Mock
    private AccessibilityGeoJsonFeature accessibilityGeoJsonFeatureForwards;
    @Mock
    private AccessibilityGeoJsonFeature accessibilityGeoJsonFeatureBackwards;

    @Mock
    private AccessibilityGeoJsonFeatureCollection accessibilityGeoJsonFeatureCollection;

    @Mock
    private GeoJsonIdSequenceSupplier geoJsonIdSequenceSupplier;


    @Test
    void map_ok() {
        when(directionalRoadSectionAndTrafficSignGroupedById.getForward()).thenReturn(roadSectionAndTrafficSignForward);
        when(directionalRoadSectionAndTrafficSignGroupedById.getBackward()).thenReturn(roadSectionAndTrafficSignBackward);

        when(geoJsonIdSequenceSupplier.next()).thenReturn(GEOJSON_ID);

        when(roadSectionAccessibilityGeoJsonFeatureMapper.map(roadSectionAndTrafficSignBackward, NWB_VERSION,GEOJSON_ID))
                .thenReturn(accessibilityGeoJsonFeatureBackwards);
        when(roadSectionAccessibilityGeoJsonFeatureMapper.map(roadSectionAndTrafficSignForward, NWB_VERSION,GEOJSON_ID))
                .thenReturn(accessibilityGeoJsonFeatureForwards);

        when(accessibilityGeoJsonFeatureCollectionMapper.map(List.of(accessibilityGeoJsonFeatureBackwards,
                accessibilityGeoJsonFeatureForwards))).thenReturn(accessibilityGeoJsonFeatureCollection);

        assertEquals(accessibilityGeoJsonFeatureCollection,
                fullRoadSectionAccessibilityGeoJsonMapper.map(geoJsonIdSequenceSupplier, List.of(
                        directionalRoadSectionAndTrafficSignGroupedById), NWB_VERSION));
    }
}