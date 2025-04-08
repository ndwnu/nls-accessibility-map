//package nu.ndw.nls.accessibilitymap.accessibility.services;
//
//import java.util.List;
//import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
//import nu.ndw.nls.accessibilitymap.shared.network.services.NetworkMetaDataService;
//import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
//import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto.Id;
//import nu.ndw.nls.db.nwb.jooq.services.NwbRoadSectionCrudService;
//import nu.ndw.nls.geometry.crs.CrsTransformer;
//import nu.ndw.nls.geometry.distance.FractionAndDistanceCalculator;
//import nu.ndw.nls.geometry.distance.model.CoordinateAndBearing;
//import org.junit.jupiter.api.Test;
//import org.locationtech.jts.geom.GeometryFactory;
//import org.locationtech.jts.geom.LineString;
//import org.locationtech.jts.geom.PrecisionModel;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.*;
//
//@org.junit.jupiter.api.extension.ExtendWith(MockitoExtension.class)
//class NwbRoadSectionSnapServiceTest {
//
//    private static final int ROAD_SECTION_ID = 100;
//    private static final double LATITUDE = 52.0;
//    private static final double LONGITUDE = 5.0;
//    private static final double FRACTION = 0.5;
//    private static final double NWB_SNAPPED_LON = 5.1;
//    private static final double NWB_SNAPPED_LAT = 52.2;
//    @Mock
//    private NwbRoadSectionCrudService roadSectionService;
//
//    @Mock
//    private FractionAndDistanceCalculator fractionAndDistanceCalculator;
//
//    @Mock
//    private NetworkMetaDataService networkMetaDataService;
//
//    @Mock
//    private CrsTransformer crsTransformer;
//
//    @InjectMocks
//    private NwbRoadSectionSnapService snapService;
//
//    private
//
//    @Test
//    void testSnapTrafficSign_RoadSectionFound_ShouldReturnCoordinateAndBearing() {
//        int nwbVersion = 1;
//        TrafficSign trafficSign = TrafficSign.builder()
//                .id(1)
//                .externalId("test-id")
//                .roadSectionId(ROAD_SECTION_ID)
//                .trafficSignType(null)
//                .latitude(LATITUDE)
//                .longitude(LONGITUDE)
//                .direction(null)
//                .fraction(FRACTION)
//                .nwbSnappedLon(NWB_SNAPPED_LON)
//                .nwbSnappedLat(NWB_SNAPPED_LAT)
//                .textSigns(List.of())
//                .restrictions(null)
//                .build();
//
//        LineString mockGeometry = new GeometryFactory(new PrecisionModel(), 28992).createLineString(
//                new org.locationtech.jts.geom.Coordinate[]{
//                        new org.locationtech.jts.geom.Coordinate(5.0, 52.0),
//                        new org.locationtech.jts.geom.Coordinate(5.2, 52.2)
//                });
//
//        NwbRoadSectionDto roadSectionDto = new NwbRoadSectionDto(new Id(nwbVersion, 100), mockGeometry);
//        CoordinateAndBearing coordinateAndBearing = new CoordinateAndBearing(5.15, 52.1, 90.0);
//
//        when(networkMetaDataService.loadMetaData().nwbVersion()).thenReturn(nwbVersion);
//        when(roadSectionService.findById(new Id(nwbVersion, 100))).thenReturn(Optional.of(roadSectionDto));
//        when(crsTransformer.transformFromRdNewToWgs84(roadSectionDto.getGeometry())).thenReturn(mockGeometry);
//        when(fractionAndDistanceCalculator.getCoordinateAndBearing(any(LineString.class), eq(0.5))).thenReturn(coordinateAndBearing);
//
//        Optional<CoordinateAndBearing> result = snapService.snapTrafficSign(trafficSign);
//
//        assertThat(result).isPresent();
//        assertThat(result.get()).isEqualTo(coordinateAndBearing);
//        verify(roadSectionService, times(1)).findById(new Id(nwbVersion, 100));
//    }
//
//    @Test
//    void testSnapTrafficSign_RoadSectionNotFound_ShouldReturnEmpty() {
//        int nwbVersion = 1;
//        TrafficSign trafficSign = TrafficSign.builder()
//                .id(1)
//                .externalId("test-id")
//                .roadSectionId(100)
//                .trafficSignType(null)
//                .latitude(52.0)
//                .longitude(5.0)
//                .direction(null)
//                .fraction(0.5)
//                .nwbSnappedLon(5.1)
//                .nwbSnappedLat(52.2)
//                .textSigns(List.of())
//                .restrictions(null)
//                .build();
//
//        when(networkMetaDataService.loadMetaData().nwbVersion()).thenReturn(nwbVersion);
//        when(roadSectionService.findById(new Id(nwbVersion, 100))).thenReturn(Optional.empty());
//
//        Optional<CoordinateAndBearing> result = snapService.snapTrafficSign(trafficSign);
//
//        assertThat(result).isEmpty();
//        verify(roadSectionService, times(1)).findById(new Id(nwbVersion, 100));
//    }
//
//    @Test
//    void testSnapTrafficSign_NetMetaDataReturnsCorrectVersion() {
//        int nwbVersion = 2;
//        TrafficSign trafficSign = TrafficSign.builder()
//                .id(1)
//                .externalId("test-id")
//                .roadSectionId(200)
//                .trafficSignType(null)
//                .latitude(51.0)
//                .longitude(6.0)
//                .direction(null)
//                .fraction(0.3)
//                .nwbSnappedLon(6.1)
//                .nwbSnappedLat(51.2)
//                .textSigns(List.of())
//                .restrictions(null)
//                .build();
//
//        NwbRoadSectionDto.Id expectedId = new Id(nwbVersion, trafficSign.roadSectionId());
//
//        when(networkMetaDataService.loadMetaData().nwbVersion()).thenReturn(nwbVersion);
//        when(roadSectionService.findById(expectedId)).thenReturn(Optional.empty());
//
//        snapService.snapTrafficSign(trafficSign);
//
//        verify(networkMetaDataService, times(1)).loadMetaData();
//        verify(roadSectionService, times(1)).findById(expectedId);
//    }
//}
