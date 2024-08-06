package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.AccessibilityGeoJsonFeature;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.AccessibilityProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSection;
import nu.ndw.nls.geometry.geojson.mappers.GeoJsonLineStringCoordinateMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.LineString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityGeoJsonFeatureMapperTest {

    private static final int NWB_VERSION = 20240101;
    private static final int ROAD_SECTION_ID = 123;

    @Mock
    private GeoJsonLineStringCoordinateMapper geoJsonLineStringCoordinateMapper;

    @InjectMocks
    private AccessibilityGeoJsonFeatureMapper accessibilityGeoJsonFeatureMapper;

    @Mock
    private LineString geometry;

    @Mock
    private List<List<Double>> geojsonLineString;

    @Test
    void map_ok_accessible() {
        when(geoJsonLineStringCoordinateMapper.map(geometry)).thenReturn(geojsonLineString);

        assertEquals(AccessibilityGeoJsonFeature.builder()
                .id((long) ROAD_SECTION_ID)
                .geometry(geojsonLineString)
                .properties(AccessibilityProperties.builder()
                        .id((long) ROAD_SECTION_ID)
                        .versionId(NWB_VERSION)
                        .accessible(true)
                        .build())
                .build(), accessibilityGeoJsonFeatureMapper.map(DirectionalRoadSection.builder()
                        .roadSectionId(ROAD_SECTION_ID)
                        .geometry(geometry)
                        .accessible(true)
                .build(), NWB_VERSION));
    }

    @Test
    void map_ok_inaccessible() {
        when(geoJsonLineStringCoordinateMapper.map(geometry)).thenReturn(geojsonLineString);

        assertEquals(AccessibilityGeoJsonFeature.builder()
                .id((long) ROAD_SECTION_ID)
                .geometry(geojsonLineString)
                .properties(AccessibilityProperties.builder()
                        .id((long) ROAD_SECTION_ID)
                        .versionId(NWB_VERSION)
                        .accessible(false)
                        .build())
                .build(), accessibilityGeoJsonFeatureMapper.map(DirectionalRoadSection.builder()
                .roadSectionId(ROAD_SECTION_ID)
                .geometry(geometry)
                .accessible(false)
                .build(), NWB_VERSION));
    }

}