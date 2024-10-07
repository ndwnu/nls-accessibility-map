package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.AccessibilityGeoJsonFeature;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.AccessibilityGeoJsonFeatureCollection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSectionAndTrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.util.LongSequenceSupplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EffectivelyAccessibleGeoJsonMapperTest {

    private static final long ID = 123L;

    private static final int NWB_VERSION = 2024;

    @Mock
    private AccessibilityGeoJsonFeatureCollectionMapper accessibilityGeoJsonFeatureCollectionMapper;

    @Mock
    private RoadSectionAccessibilityGeoJsonFeatureMapper roadSectionAccessibilityGeoJsonFeatureMapper;

    @InjectMocks
    private EffectivelyAccessibleGeoJsonMapper effectivelyAccessibleGeoJsonMapper;

    @Mock
    private LongSequenceSupplier geoJsonIdSequenceSupplier;

    @Mock
    private DirectionalRoadSectionAndTrafficSign directionalRoadSectionAndTrafficSignA;

    @Mock
    private DirectionalRoadSectionAndTrafficSign directionalRoadSectionAndTrafficSignB;

    @Mock
    private AccessibilityGeoJsonFeature accessibilityGeoJsonFeatureA;

    @Mock
    private AccessibilityGeoJsonFeature accessibilityGeoJsonFeatureB;

    @Mock
    private AccessibilityGeoJsonFeatureCollection accessibilityGeoJsonFeatureCollection;

    @Test
    void map_ok() {
        when(geoJsonIdSequenceSupplier.next()).thenReturn(ID);

        when(roadSectionAccessibilityGeoJsonFeatureMapper.map(directionalRoadSectionAndTrafficSignA, NWB_VERSION, ID))
                .thenReturn(accessibilityGeoJsonFeatureA);
        when(roadSectionAccessibilityGeoJsonFeatureMapper.map(directionalRoadSectionAndTrafficSignB, NWB_VERSION, ID))
                .thenReturn(accessibilityGeoJsonFeatureB);

        when(accessibilityGeoJsonFeatureCollectionMapper.map(List.of(accessibilityGeoJsonFeatureA,
                accessibilityGeoJsonFeatureB))).thenReturn(accessibilityGeoJsonFeatureCollection);

        assertThat(effectivelyAccessibleGeoJsonMapper.map(geoJsonIdSequenceSupplier, List.of(
                directionalRoadSectionAndTrafficSignA, directionalRoadSectionAndTrafficSignB), NWB_VERSION))
                    .isEqualTo(accessibilityGeoJsonFeatureCollection);
    }
}