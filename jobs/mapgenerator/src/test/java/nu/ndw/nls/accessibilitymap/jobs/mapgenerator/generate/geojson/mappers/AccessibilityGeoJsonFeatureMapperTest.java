package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.AccessibilityGeoJsonFeature;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.AccessibilityGeoJsonProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.LineStringGeojson;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.RoadSectionAndTrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.TrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.TrafficSignType;
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
    private static final int ROAD_SECTION_ID_INT = 123;
    private static final long ROAD_SECTION_ID_LONG = 123;
    private static final String WINDOW_TIMES = "Mo 08:00-16:00; Jan-Mar Mo 08:00-12:00";

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
                .id(ROAD_SECTION_ID_INT)
                .geometry(LineStringGeojson.builder()
                        .coordinates(geojsonLineString)
                        .build())
                .properties(AccessibilityGeoJsonProperties.builder()
                        .id(ROAD_SECTION_ID_INT)
                        .versionId(NWB_VERSION)
                        .accessible(true)
                        .trafficSignType(TrafficSignType.C7)
                        .windowTimes(WINDOW_TIMES)
                        .build())
                .build(), accessibilityGeoJsonFeatureMapper.map(
                RoadSectionAndTrafficSign.<DirectionalRoadSection, TrafficSign>builder()
                        .roadSection(DirectionalRoadSection.builder()
                                .roadSectionId(ROAD_SECTION_ID_INT)
                                .geometry(geometry)
                                .accessible(true)
                                .build())
                        .trafficSign(TrafficSign.builder()
                                .roadSectionId(ROAD_SECTION_ID_LONG)
                                .trafficSignType(TrafficSignType.C7)
                                .windowTimes(WINDOW_TIMES)
                                .build())
                        .build(), NWB_VERSION));
    }

    @Test
    void map_ok_inaccessible() {
        when(geoJsonLineStringCoordinateMapper.map(geometry)).thenReturn(geojsonLineString);

        assertEquals(AccessibilityGeoJsonFeature.builder()
                .id(ROAD_SECTION_ID_INT)
                .geometry(LineStringGeojson.builder()
                        .coordinates(geojsonLineString)
                        .build())
                .properties(AccessibilityGeoJsonProperties.builder()
                        .id(ROAD_SECTION_ID_INT)
                        .versionId(NWB_VERSION)
                        .accessible(false)
                        .trafficSignType(TrafficSignType.C7)
                        .windowTimes(WINDOW_TIMES)
                        .build())
                .build(), accessibilityGeoJsonFeatureMapper.map(
                RoadSectionAndTrafficSign.<DirectionalRoadSection, TrafficSign>builder()
                        .roadSection(DirectionalRoadSection.builder()
                                .roadSectionId(ROAD_SECTION_ID_INT)
                                .geometry(geometry)
                                .accessible(false)
                                .build())
                        .trafficSign(TrafficSign.builder()
                                .roadSectionId(ROAD_SECTION_ID_LONG)
                                .trafficSignType(TrafficSignType.C7)
                                .windowTimes(WINDOW_TIMES)
                                .build())
                        .build(), NWB_VERSION));
    }

    @Test
    void map_ok_inaccessibleWithoutTrafficSign() {
        when(geoJsonLineStringCoordinateMapper.map(geometry)).thenReturn(geojsonLineString);

        assertEquals(AccessibilityGeoJsonFeature.builder()
                .id(ROAD_SECTION_ID_INT)
                .geometry(LineStringGeojson.builder()
                        .coordinates(geojsonLineString)
                        .build())
                .properties(AccessibilityGeoJsonProperties.builder()
                        .id(ROAD_SECTION_ID_INT)
                        .versionId(NWB_VERSION)
                        .accessible(false)
                        .trafficSignType(null)
                        .windowTimes(null)
                        .build())
                .build(), accessibilityGeoJsonFeatureMapper.map(
                RoadSectionAndTrafficSign.<DirectionalRoadSection, TrafficSign>builder()
                        .roadSection(DirectionalRoadSection.builder()
                                .roadSectionId(ROAD_SECTION_ID_INT)
                                .geometry(geometry)
                                .accessible(false)
                                .build())
                        .trafficSign(null)
                        .build(), NWB_VERSION));
    }


}