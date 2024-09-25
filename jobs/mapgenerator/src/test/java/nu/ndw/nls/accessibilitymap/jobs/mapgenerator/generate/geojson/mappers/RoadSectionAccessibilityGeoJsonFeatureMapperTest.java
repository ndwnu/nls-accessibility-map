package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.AccessibilityGeoJsonFeature;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.AccessibilityGeoJsonProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSectionAndTrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.LineStringGeojson;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.TrafficSignType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.Direction;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.directional.DirectionalRoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.directional.DirectionalTrafficSign;
import nu.ndw.nls.geometry.geojson.mappers.GeoJsonLineStringCoordinateMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.LineString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoadSectionAccessibilityGeoJsonFeatureMapperTest {

    private static final int NWB_VERSION = 20240101;
    private static final int ROAD_SECTION_ID_INT = 123;
    private static final long GEOJSON_ID = 123;
    private static final String WINDOW_TIMES = "Mo 08:00-16:00; Jan-Mar Mo 08:00-12:00";

    @Mock
    private GeoJsonLineStringCoordinateMapper geoJsonLineStringCoordinateMapper;

    @InjectMocks
    private RoadSectionAccessibilityGeoJsonFeatureMapper roadSectionAccessibilityGeoJsonFeatureMapper;

    @Mock
    private LineString geometry;

    @Mock
    private LineString backwardGeometry;

    @Mock
    private List<List<Double>> geojsonLineString;

    @Test
    void map_ok_accessible_forward() {
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
                .build(), roadSectionAccessibilityGeoJsonFeatureMapper.map(
                DirectionalRoadSectionAndTrafficSign.builder()
                        .roadSection(DirectionalRoadSection.builder()
                                .nwbRoadSectionId(ROAD_SECTION_ID_INT)
                                .nwbGeometry(geometry)
                                .accessible(true)
                                .direction(Direction.FORWARD)
                                .build())
                        .trafficSign(DirectionalTrafficSign.builder()
                                .nwbRoadSectionId(GEOJSON_ID)
                                .trafficSignType(TrafficSignType.C7)
                                .windowTimes(WINDOW_TIMES)
                                .build())
                        .build(), NWB_VERSION, GEOJSON_ID));
    }

    @Test
    void map_ok_accessibleBackward() {
        when(geometry.reverse()).thenReturn(backwardGeometry);
        when(geoJsonLineStringCoordinateMapper.map(backwardGeometry)).thenReturn(geojsonLineString);

        assertEquals(AccessibilityGeoJsonFeature.builder()
                .id(GEOJSON_ID)
                .geometry(LineStringGeojson.builder()
                        .coordinates(geojsonLineString)
                        .build())
                .properties(AccessibilityGeoJsonProperties.builder()
                        .id(GEOJSON_ID)
                        .versionId(NWB_VERSION)
                        .accessible(true)
                        .trafficSignType(TrafficSignType.C7)
                        .windowTimes(WINDOW_TIMES)
                        .build())
                .build(), roadSectionAccessibilityGeoJsonFeatureMapper.map(
                DirectionalRoadSectionAndTrafficSign.builder()
                        .roadSection(DirectionalRoadSection.builder()
                                .nwbRoadSectionId(ROAD_SECTION_ID_INT)
                                .nwbGeometry(geometry)
                                .accessible(true)
                                .direction(Direction.BACKWARD)
                                .build())
                        .trafficSign(DirectionalTrafficSign.builder()
                                .nwbRoadSectionId(GEOJSON_ID)
                                .trafficSignType(TrafficSignType.C7)
                                .windowTimes(WINDOW_TIMES)
                                .build())
                        .build(), NWB_VERSION, GEOJSON_ID));
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
                .build(), roadSectionAccessibilityGeoJsonFeatureMapper.map(
                DirectionalRoadSectionAndTrafficSign.builder()
                        .roadSection(DirectionalRoadSection.builder()
                                .nwbRoadSectionId(ROAD_SECTION_ID_INT)
                                .nwbGeometry(geometry)
                                .accessible(false)
                                .direction(Direction.FORWARD)
                                .build())
                        .trafficSign(DirectionalTrafficSign.builder()
                                .nwbRoadSectionId(GEOJSON_ID)
                                .trafficSignType(TrafficSignType.C7)
                                .windowTimes(WINDOW_TIMES)
                                .build())
                        .build(), NWB_VERSION, GEOJSON_ID));
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
                .build(), roadSectionAccessibilityGeoJsonFeatureMapper.map(
                DirectionalRoadSectionAndTrafficSign.builder()
                        .roadSection(DirectionalRoadSection.builder()
                                .nwbRoadSectionId(ROAD_SECTION_ID_INT)
                                .nwbGeometry(geometry)
                                .accessible(false)
                                .direction(Direction.FORWARD)
                                .build())
                        .trafficSign(null)
                        .build(), NWB_VERSION, GEOJSON_ID));
    }


}
