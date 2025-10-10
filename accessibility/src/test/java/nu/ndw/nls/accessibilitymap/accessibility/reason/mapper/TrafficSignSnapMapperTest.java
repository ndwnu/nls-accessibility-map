package nu.ndw.nls.accessibilitymap.accessibility.reason.mapper;

import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.index.LocationIndexTree;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.EdgeIteratorState;
import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.TrafficSignSnap;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrafficSignSnapMapperTest {

    private static final double X_COORDINATE = 0D;

    private static final double Y_COORDINATE = 1D;

    private static final int ROAD_SECTION_ID = 123;

    private static final String TRAFFIC_SIGN_ID = "id";

    @Mock
    private NetworkGraphHopper networkGraphHopper;

    @Mock
    private TrafficSign trafficSign;

    @Captor
    private ArgumentCaptor<EdgeFilter> edgeFilterCaptor;

    @Mock
    private LocationIndexTree locationIndexTree;

    @Mock
    private Snap snap;

    @Mock
    private EncodingManager encodingManager;

    @Mock
    private IntEncodedValue intEncodedValue;

    @Mock
    private EdgeIteratorState edgeIteratorState;

    private TrafficSignSnapMapper trafficSignSnapMapper;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @BeforeEach
    void setUp() {
        trafficSignSnapMapper = new TrafficSignSnapMapper();
    }

    @Test
    void map() {
        setupBaseFixture();
        when(snap.isValid()).thenReturn(true);
        List<TrafficSignSnap> trafficSignSnaps = trafficSignSnapMapper.map(List.of(trafficSign), networkGraphHopper);
        assertThat(trafficSignSnaps).hasSize(1);
        assertThat(trafficSignSnaps.getFirst().getSnap()).isEqualTo(snap);
        assertThat(trafficSignSnaps.getFirst().getTrafficSign()).isEqualTo(trafficSign);

        verifyEdgeFilterOk();
        verifyEdgeFilterNoMatch();
    }

    @Test
    void map_snap_invalid() {
        setupBaseFixture();

        when(trafficSign.externalId()).thenReturn(TRAFFIC_SIGN_ID);
        when(snap.isValid()).thenReturn(false);

        List<TrafficSignSnap> trafficSignSnaps = trafficSignSnapMapper.map(List.of(trafficSign), networkGraphHopper);

        assertThat(trafficSignSnaps).isEmpty();
        loggerExtension.containsLog(
                Level.DEBUG,
                "No road section present for traffic sign id %s with road section id %d in nwb map on graphhopper network".formatted(
                        TRAFFIC_SIGN_ID,
                        ROAD_SECTION_ID));
    }

    private void setupBaseFixture() {
        when(trafficSign.roadSectionId()).thenReturn(ROAD_SECTION_ID);
        when(networkGraphHopper.getLocationIndex()).thenReturn(locationIndexTree);
        when(trafficSign.networkSnappedLongitude()).thenReturn(X_COORDINATE);
        when(trafficSign.networkSnappedLatitude()).thenReturn(Y_COORDINATE);
        when(locationIndexTree.findClosest(eq(Y_COORDINATE), eq(X_COORDINATE), edgeFilterCaptor.capture())).thenReturn(snap);
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
