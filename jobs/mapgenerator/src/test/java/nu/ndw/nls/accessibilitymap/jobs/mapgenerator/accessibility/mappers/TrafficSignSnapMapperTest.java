package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.mappers;

import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.index.LocationIndexTree;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.EdgeIteratorState;
import java.util.List;
import java.util.Optional;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.TrafficSignSnap;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.test.utils.LoggerExtension;
import nu.ndw.nls.accessibilitymap.shared.network.dtos.AccessibilityGraphhopperMetaData;
import nu.ndw.nls.accessibilitymap.shared.network.services.NetworkMetaDataService;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto.Id;
import nu.ndw.nls.db.nwb.jooq.services.NwbRoadSectionCrudService;
import nu.ndw.nls.geometry.crs.CrsTransformer;
import nu.ndw.nls.geometry.distance.FractionAndDistanceCalculator;
import nu.ndw.nls.geometry.distance.model.CoordinateAndBearing;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrafficSignSnapMapperTest {

    private static final int NWB_VERSION = 20241001;

    public static final double FRACTION = 0.5;

    private static final double X_COORDINATE = 0D;

    private static final double Y_COORDINATE = 1D;

    private static final int ROAD_SECTION_ID = 123;

    private static final String TRAFFIC_SIGN_ID = "id";

    @Mock
    private NwbRoadSectionCrudService nwbRoadSectionCrudService;

    @Mock
    private FractionAndDistanceCalculator fractionAndDistanceCalculator;

    @Mock
    private NetworkMetaDataService networkMetaDataService;

    @Mock
    private CrsTransformer crsTransformer;

    @Mock
    private NetworkGraphHopper networkGraphHopper;

    @Mock
    private TrafficSign trafficSign;

    @Mock
    private NwbRoadSectionDto nwbRoadSectionDto;

    @Captor
    private ArgumentCaptor<Id> idArgumentCaptor;

    @Captor
    private ArgumentCaptor<EdgeFilter> edgeFilterCaptor;

    @Mock
    private LineString lineStringRd;

    @Mock
    private LineString lineStringWgs84;

    @Mock
    private CoordinateAndBearing coordinateAndBearing;

    @Mock
    private LocationIndexTree locationIndexTree;

    @Mock
    private Coordinate snappedCoordinate;

    @Mock
    private Snap snap;

    @Mock
    private EncodingManager encodingManager;

    @Mock
    private IntEncodedValue intEncodedValue;

    @Mock
    private EdgeIteratorState edgeIteratorState;

    @InjectMocks
    private TrafficSignSnapMapper trafficSingSnapMapper;

    private AccessibilityGraphhopperMetaData accessibilityGraphhopperMetaData;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @BeforeEach
    void setUp() {

        accessibilityGraphhopperMetaData = new AccessibilityGraphhopperMetaData(NWB_VERSION);

        trafficSingSnapMapper = new TrafficSignSnapMapper(
                nwbRoadSectionCrudService,
                fractionAndDistanceCalculator,
                networkMetaDataService,
                crsTransformer,
                networkGraphHopper);
    }

    @Test
    void map_ok() {
        setupBaseFixture();
        when(snap.isValid()).thenReturn(true);
        List<TrafficSignSnap> trafficSignSnaps = trafficSingSnapMapper.map(List.of(trafficSign), true);
        assertThat(trafficSignSnaps).hasSize(1);
        assertThat(trafficSignSnaps.getFirst().getSnap()).isEqualTo(snap);
        assertThat(trafficSignSnaps.getFirst().getTrafficSign()).isEqualTo(trafficSign);

        verifyIdCreatedOk();
        verifyEdgeFilterOk();
        verifyEdgeFilterNoMatch();
    }

    @Test
    void map_ok_snap_invalid() {

        setupBaseFixture();
        when(trafficSign.externalId()).thenReturn(TRAFFIC_SIGN_ID);
        when(snap.isValid()).thenReturn(false);

        List<TrafficSignSnap> trafficSignSnaps = trafficSingSnapMapper.map(List.of(trafficSign), true);

        assertThat(trafficSignSnaps).isEmpty();
        loggerExtension.containsLog(Level.WARN, ("No road section present for traffic sign id %s with "
                + "road section id %s in nwb map version %s on graphhopper network")
                .formatted(TRAFFIC_SIGN_ID, ROAD_SECTION_ID, NWB_VERSION));
    }

    @Test
    void map_ok_no_roadSection() {
        when(trafficSign.externalId()).thenReturn(TRAFFIC_SIGN_ID);
        when(trafficSign.roadSectionId()).thenReturn(ROAD_SECTION_ID);
        when(trafficSign.hasTimeWindowedSign())
                .thenReturn(true);

        when(nwbRoadSectionCrudService.findById(any(Id.class)))
                .thenReturn(Optional.empty());
        when(networkMetaDataService.loadMetaData())
                .thenReturn(accessibilityGraphhopperMetaData);

        List<TrafficSignSnap> trafficSignSnaps = trafficSingSnapMapper.map(List.of(trafficSign), true);

        assertThat(trafficSignSnaps).isEmpty();
        loggerExtension.containsLog(Level.WARN, ("No road section present for traffic sign id %s with road section "
                + "id %s for nwb map version %s in the NWB road section database")
                .formatted(TRAFFIC_SIGN_ID, ROAD_SECTION_ID, NWB_VERSION));
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            false, true,
            true, false
            """)
    void map_ok_no_textSign(boolean isIncludeOnlyTimeWindowedSigns, boolean hasTimeWindowedSign) {

        if (isIncludeOnlyTimeWindowedSigns) {
            when(trafficSign.hasTimeWindowedSign())
                    .thenReturn(hasTimeWindowedSign);
        }

        List<TrafficSignSnap> trafficSignSnaps = trafficSingSnapMapper.map(List.of(trafficSign),
                isIncludeOnlyTimeWindowedSigns);
        assertThat(trafficSignSnaps).isEmpty();
    }

    private void setupBaseFixture() {

        when(trafficSign.hasTimeWindowedSign()).thenReturn(true);
        when(trafficSign.roadSectionId()).thenReturn(ROAD_SECTION_ID);
        when(trafficSign.fraction()).thenReturn(FRACTION);
        when(networkMetaDataService.loadMetaData()).thenReturn(accessibilityGraphhopperMetaData);
        when(nwbRoadSectionCrudService.findById(any(Id.class))).thenReturn(Optional.of(nwbRoadSectionDto));
        when(nwbRoadSectionDto.getGeometry()).thenReturn(lineStringRd);
        when(crsTransformer.transformFromRdNewToWgs84(lineStringRd)).thenReturn(lineStringWgs84);
        when(fractionAndDistanceCalculator.getCoordinateAndBearing(lineStringWgs84, FRACTION))
                .thenReturn(coordinateAndBearing);
        when(networkGraphHopper.getLocationIndex()).thenReturn(locationIndexTree);
        when(coordinateAndBearing.coordinate()).thenReturn(snappedCoordinate);
        when(snappedCoordinate.getX()).thenReturn(X_COORDINATE);
        when(snappedCoordinate.getY()).thenReturn(Y_COORDINATE);
        when(locationIndexTree.findClosest(
                eq(Y_COORDINATE),
                eq(X_COORDINATE),
                edgeFilterCaptor.capture())
        ).thenReturn(snap);
    }

    private void verifyIdCreatedOk() {

        verify(nwbRoadSectionCrudService).findById(idArgumentCaptor.capture());
        Id createdId = idArgumentCaptor.getValue();
        assertThat(createdId.getRoadSectionId()).isEqualTo(ROAD_SECTION_ID);
        assertThat(createdId.getVersionId()).isEqualTo(NWB_VERSION);
    }

    private void verifyEdgeFilterOk() {

        when(networkGraphHopper.getEncodingManager()).thenReturn(encodingManager);
        when(encodingManager.getIntEncodedValue(WAY_ID_KEY)).thenReturn(intEncodedValue);
        when(edgeIteratorState.get(intEncodedValue)).thenReturn(ROAD_SECTION_ID);
        assertThat(edgeFilterCaptor.getValue().accept(edgeIteratorState)).isTrue();
    }

    private void verifyEdgeFilterNoMatch() {

        when(networkGraphHopper.getEncodingManager()).thenReturn(encodingManager);
        when(encodingManager.getIntEncodedValue(WAY_ID_KEY)).thenReturn(intEncodedValue);
        when(edgeIteratorState.get(intEncodedValue)).thenReturn(1);
        assertThat(edgeFilterCaptor.getValue().accept(edgeIteratorState)).isFalse();
    }
}
