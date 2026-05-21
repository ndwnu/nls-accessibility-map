package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.util;

import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.index.LocationIndexTree;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.EdgeIteratorState;
import java.util.Optional;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Location;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.service.PointMatchService;
import nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode;
import nu.ndw.nls.routingmapmatcher.model.singlepoint.SinglePointMatch.CandidateMatch;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.locationtech.jts.geom.Point;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SnapperTest {

    private Snapper snapper;

    @Mock
    private PointMatchService pointMatchService;

    @Mock
    private NetworkGraphHopper networkGraphHopper;

    @Mock
    private NetworkData networkData;

    @Mock
    private LocationIndexTree locationIndexTree;

    @Mock
    private EncodingManager encodingManager;

    @Mock
    private Location location;

    @Mock
    private Point point;

    @Mock
    private Point snappedPoint;

    @Mock
    private CandidateMatch candidateMatch;

    @Mock
    private Snap actualSnap;

    @Mock
    private IntEncodedValue roadSectionEncodedValue;

    @Mock
    private Restriction restriction;

    @Captor
    private ArgumentCaptor<EdgeFilter> edgeFilterCaptor;

    @Mock
    private EdgeIteratorState edgeIteratorState;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @BeforeEach
    void setUp() {

        snapper = new Snapper(pointMatchService);
    }

    @Test
    void snapLocation() {

        when(location.point()).thenReturn(point);
        when(networkData.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        when(networkGraphHopper.getLocationIndex()).thenReturn(locationIndexTree);
        when(locationIndexTree.findClosest(eq(2.0), eq(1.0), edgeFilterCaptor.capture())).thenReturn(actualSnap);
        when(pointMatchService.match(networkGraphHopper, point)).thenReturn(Optional.of(candidateMatch));
        when(candidateMatch.getSnappedPoint()).thenReturn(snappedPoint);
        when(snappedPoint.isValid()).thenReturn(true);
        when(snappedPoint.getX()).thenReturn(1.0);
        when(snappedPoint.getY()).thenReturn(2.0);
        when(networkGraphHopper.getEncodingManager()).thenReturn(encodingManager);
        when(encodingManager.getIntEncodedValue(WAY_ID_KEY)).thenReturn(roadSectionEncodedValue);
        when(edgeIteratorState.get(roadSectionEncodedValue)).thenReturn(23);
        when(networkData.findCarriageWayTypeCodeByRoadSectionId(23)).thenReturn(Optional.of(CarriagewayTypeCode.RB));

        Optional<Snap> foundSnap = snapper.snapLocation(networkData, location);

        assertThat(foundSnap).contains(actualSnap);
        assertThat(edgeFilterCaptor.getValue().accept(edgeIteratorState)).isTrue();
    }

    @Test
    void snapLocation_notCarAccessible() {

        when(location.point()).thenReturn(point);
        when(networkData.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        when(networkGraphHopper.getLocationIndex()).thenReturn(locationIndexTree);
        when(locationIndexTree.findClosest(eq(2.0), eq(1.0), edgeFilterCaptor.capture())).thenReturn(actualSnap);
        when(pointMatchService.match(networkGraphHopper, point)).thenReturn(Optional.of(candidateMatch));
        when(candidateMatch.getSnappedPoint()).thenReturn(snappedPoint);
        when(snappedPoint.isValid()).thenReturn(true);
        when(snappedPoint.getX()).thenReturn(1.0);
        when(snappedPoint.getY()).thenReturn(2.0);
        when(networkGraphHopper.getEncodingManager()).thenReturn(encodingManager);
        when(encodingManager.getIntEncodedValue(WAY_ID_KEY)).thenReturn(roadSectionEncodedValue);
        when(edgeIteratorState.get(roadSectionEncodedValue)).thenReturn(23);
        when(networkData.findCarriageWayTypeCodeByRoadSectionId(23)).thenReturn(Optional.of(CarriagewayTypeCode.FP));

        Optional<Snap> foundSnap = snapper.snapLocation(networkData, location);

        assertThat(foundSnap).contains(actualSnap);
        assertThat(edgeFilterCaptor.getValue().accept(edgeIteratorState)).isFalse();
    }

    @Test
    void snapLocation_roadSectionNotPresentInNetworkGraphHopper() {

        when(location.point()).thenReturn(point);
        when(networkData.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        when(networkGraphHopper.getLocationIndex()).thenReturn(locationIndexTree);
        when(locationIndexTree.findClosest(eq(2.0), eq(1.0), edgeFilterCaptor.capture())).thenReturn(actualSnap);
        when(pointMatchService.match(networkGraphHopper, point)).thenReturn(Optional.of(candidateMatch));
        when(candidateMatch.getSnappedPoint()).thenReturn(snappedPoint);
        when(snappedPoint.isValid()).thenReturn(true);
        when(snappedPoint.getX()).thenReturn(1.0);
        when(snappedPoint.getY()).thenReturn(2.0);
        when(networkGraphHopper.getEncodingManager()).thenReturn(encodingManager);
        when(encodingManager.getIntEncodedValue(WAY_ID_KEY)).thenReturn(roadSectionEncodedValue);
        when(edgeIteratorState.get(roadSectionEncodedValue)).thenReturn(23);
        when(networkData.findCarriageWayTypeCodeByRoadSectionId(23)).thenReturn(Optional.empty());

        Optional<Snap> foundSnap = snapper.snapLocation(networkData, location);

        assertThat(foundSnap).contains(actualSnap);
        assertThatThrownBy(() -> edgeFilterCaptor.getValue().accept(edgeIteratorState))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Road section not found for link id: 23");
    }

    @Test
    void snapLocation_noLocation() {

        Optional<Snap> foundSnap = snapper.snapLocation(networkData, null);

        assertThat(foundSnap).isEmpty();
    }

    @Test
    void snapLocation_noMatchFound() {
        when(networkData.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        when(location.point()).thenReturn(point);
        when(pointMatchService.match(networkGraphHopper, point)).thenReturn(Optional.empty());

        Optional<Snap> foundSnap = snapper.snapLocation(networkData, location);

        assertThat(foundSnap).isEmpty();
    }

    @Test
    void snapLocation_notValid() {
        when(networkData.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        when(location.point()).thenReturn(point);
        when(pointMatchService.match(networkGraphHopper, point)).thenReturn(Optional.of(candidateMatch));
        when(candidateMatch.getSnappedPoint()).thenReturn(snappedPoint);
        when(snappedPoint.isValid()).thenReturn(false);

        Optional<Snap> foundSnap = snapper.snapLocation(networkData, location);

        assertThat(foundSnap).isEmpty();
    }

    @Test
    void snapRestriction() {

        when(restriction.networkSnappedLatitude()).thenReturn(1.0);
        when(restriction.networkSnappedLongitude()).thenReturn(2.0);
        when(restriction.roadSectionId()).thenReturn(23);

        when(networkGraphHopper.getLocationIndex()).thenReturn(locationIndexTree);
        when(networkGraphHopper.getEncodingManager()).thenReturn(encodingManager);
        when(encodingManager.getIntEncodedValue(WAY_ID_KEY)).thenReturn(roadSectionEncodedValue);
        when(edgeIteratorState.get(roadSectionEncodedValue)).thenReturn(23);

        when(locationIndexTree.findClosest(eq(1.0), eq(2.0), edgeFilterCaptor.capture())).thenReturn(actualSnap);
        when(actualSnap.isValid()).thenReturn(true);

        Optional<Snap> foundSnap = snapper.snapRestriction(networkGraphHopper, restriction);

        assertThat(edgeFilterCaptor.getValue().accept(edgeIteratorState)).isTrue();
        assertThat(foundSnap).contains(actualSnap);
    }

    @Test
    void snapRestriction_notValid() {

        when(restriction.networkSnappedLatitude()).thenReturn(1.0);
        when(restriction.networkSnappedLongitude()).thenReturn(2.0);
        when(restriction.roadSectionId()).thenReturn(23);

        when(networkGraphHopper.getLocationIndex()).thenReturn(locationIndexTree);
        when(networkGraphHopper.getEncodingManager()).thenReturn(encodingManager);
        when(encodingManager.getIntEncodedValue(WAY_ID_KEY)).thenReturn(roadSectionEncodedValue);
        when(edgeIteratorState.get(roadSectionEncodedValue)).thenReturn(23);

        when(locationIndexTree.findClosest(eq(1.0), eq(2.0), edgeFilterCaptor.capture())).thenReturn(actualSnap);
        when(actualSnap.isValid()).thenReturn(false);

        Optional<Snap> foundSnap = snapper.snapRestriction(networkGraphHopper, restriction);

        assertThat(edgeFilterCaptor.getValue().accept(edgeIteratorState)).isTrue();
        assertThat(foundSnap).isEmpty();

        loggerExtension.containsLog(Level.DEBUG,
                "No road section present for restriction 'restriction' that could be linked to the nwb map in the Graph Hopper network.");
    }

    @Test
    void snapRestriction_notMatchingRoadSection() {

        when(restriction.networkSnappedLatitude()).thenReturn(1.0);
        when(restriction.networkSnappedLongitude()).thenReturn(2.0);
        when(restriction.roadSectionId()).thenReturn(29);

        when(networkGraphHopper.getLocationIndex()).thenReturn(locationIndexTree);
        when(networkGraphHopper.getEncodingManager()).thenReturn(encodingManager);
        when(encodingManager.getIntEncodedValue(WAY_ID_KEY)).thenReturn(roadSectionEncodedValue);
        when(edgeIteratorState.get(roadSectionEncodedValue)).thenReturn(23);

        when(locationIndexTree.findClosest(eq(1.0), eq(2.0), edgeFilterCaptor.capture())).thenReturn(actualSnap);
        when(actualSnap.isValid()).thenReturn(false);

        Optional<Snap> foundSnap = snapper.snapRestriction(networkGraphHopper, restriction);

        assertThat(edgeFilterCaptor.getValue().accept(edgeIteratorState)).isFalse();
        assertThat(foundSnap).isEmpty();

        loggerExtension.containsLog(Level.DEBUG,
                "No road section present for restriction 'restriction' that could be linked to the nwb map in the Graph Hopper network.");
    }
}
