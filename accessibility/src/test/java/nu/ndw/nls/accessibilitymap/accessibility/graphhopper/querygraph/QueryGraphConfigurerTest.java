package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.querygraph;

import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.MOTOR_VEHICLE_ACCESS_FORBIDDEN_WINDOWED;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.TRAFFIC_SIGN_ID;
import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.FetchMode;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint3D;
import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.TrafficSignEdgeRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.TrafficSignEdgeRestrictions;
import nu.ndw.nls.accessibilitymap.accessibility.services.accessibility.dto.TrafficSignSnap;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QueryGraphConfigurerTest {

    private static final int ROAD_SECTION_ID = 123;

    private static final double LON = 0.0;

    private static final double LAT = 1.0;

    private static final int TRAFFIC_SIGN_ID_VALUE = 1;

    private static final String MESSAGE_NOT_ASSIGNED = "Query graph configuration summary. "
            + "Total traffic signs in request 1. "
            + "Total not assignable road sections with traffic sign 1, notAssigned {%s=[trafficSignSnap]}";

    @Mock
    private EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor;

    @Mock
    private EncodingManager encodingManager;

    @Mock
    private EdgeManager edgeManager;

    @Mock
    private QueryGraph queryGraph;

    @Mock
    private TrafficSignSnap trafficSignSnap;

    @Mock
    private TrafficSign trafficSign;

    @Mock
    private Snap snap;

    @Mock
    private EdgeExplorer edgeExplorer;

    @Mock
    private EdgeIterator edgeIterator;

    @Mock
    private IntEncodedValue intEncodedValueWayId;

    @Mock
    private GHPoint3D ghPoint;

    @Mock
    private PointList pointList;

    @Mock
    private LineString lineString;

    @Mock
    private Point point;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @InjectMocks
    private QueryGraphConfigurer queryGraphConfigurer;

    @Test
    void createEdgeRestrictions_assignRestrictionsSuccessfully() {
        setupFixtureForQueryGraph();
        setupFixtureForTrafficSignSnap();

        when(trafficSignSnap.getSnap()).thenReturn(snap);
        when(snap.getClosestNode()).thenReturn(0);

        when(trafficSign.direction()).thenReturn(Direction.FORWARD);
        when(edgeIteratorStateReverseExtractor.hasReversed(edgeIterator)).thenReturn(false);
        when(trafficSign.id()).thenReturn(TRAFFIC_SIGN_ID_VALUE);
        when(trafficSign.roadSectionId()).thenReturn(ROAD_SECTION_ID);
        when(encodingManager.getIntEncodedValue(WAY_ID_KEY)).thenReturn(intEncodedValueWayId);
        when(edgeIterator.get(intEncodedValueWayId)).thenReturn(ROAD_SECTION_ID);
        when(edgeIterator.fetchWayGeometry(FetchMode.ALL)).thenReturn(pointList);
        when(pointList.toLineString(false)).thenReturn(lineString);
        when(lineString.getStartPoint()).thenReturn(point);
        when(point.getCoordinate()).thenReturn(new Coordinate(LON, LAT));
        when(snap.getSnappedPoint()).thenReturn(ghPoint);
        when(ghPoint.getLon()).thenReturn(LON);
        when(ghPoint.getLat()).thenReturn(LAT);

        TrafficSignEdgeRestrictions restrictions = queryGraphConfigurer.createEdgeRestrictions(queryGraph, List.of(trafficSignSnap));

        assertThat(restrictions.hasEdgeRestriction(edgeIterator.getEdgeKey())).isTrue();
        TrafficSignEdgeRestriction restriction = restrictions.getEdgeRestriction(edgeIterator.getEdgeKey());
        assertThat(restriction.getTrafficSignId()).isEqualTo(TRAFFIC_SIGN_ID_VALUE);
    }

    @Test
    void createEdgeRestrictions_noRestrictionsAssigned_edgeNotInSameDirection() {
        setupFixtureForQueryGraph();
        setupFixtureForTrafficSignSnap();

        when(trafficSignSnap.getSnap()).thenReturn(snap);
        when(snap.getClosestNode()).thenReturn(0);

        when(trafficSign.direction()).thenReturn(Direction.BACKWARD);
        when(edgeIteratorStateReverseExtractor.hasReversed(edgeIterator)).thenReturn(false);

        TrafficSignEdgeRestrictions restrictions = queryGraphConfigurer.createEdgeRestrictions(queryGraph, List.of(trafficSignSnap));

        assertThat(restrictions.hasEdgeRestriction(edgeIterator.getEdgeKey())).isFalse();
    }

    @Test
    void createEdgeRestrictions_noRestrictionsAssigned_roadSectionIdMismatch() {
        setupFixtureForQueryGraph();
        setupFixtureForTrafficSignSnap();

        when(trafficSignSnap.getSnap()).thenReturn(snap);
        when(snap.getSnappedPoint()).thenReturn(ghPoint);
        when(ghPoint.getLon()).thenReturn(LON);
        when(ghPoint.getLat()).thenReturn(LAT);

        //Latitude is the Y axis, longitude is the X axis
        when(point.getCoordinate()).thenReturn(new Coordinate(LON, LAT));
        when(edgeIterator.fetchWayGeometry(FetchMode.ALL)).thenReturn(pointList);
        when(snap.getClosestNode()).thenReturn(0);
        when(pointList.toLineString(false)).thenReturn(lineString);
        when(lineString.getStartPoint()).thenReturn(point);
        when(trafficSign.direction()).thenReturn(Direction.FORWARD);
        when(edgeIteratorStateReverseExtractor.hasReversed(edgeIterator)).thenReturn(false);
        when(trafficSign.roadSectionId()).thenReturn(124); // Mismatched road section ID
        when(encodingManager.getIntEncodedValue(WAY_ID_KEY)).thenReturn(intEncodedValueWayId);
        when(edgeIterator.get(intEncodedValueWayId)).thenReturn(ROAD_SECTION_ID);

        TrafficSignEdgeRestrictions restrictions = queryGraphConfigurer.createEdgeRestrictions(queryGraph, List.of(trafficSignSnap));

        assertThat(restrictions.hasEdgeRestriction(edgeIterator.getEdgeKey())).isFalse();
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            false
            true
            """)
    void configure_assigned(boolean reversed) {

        setupFixtureForQueryGraph();
        setupFixtureForTrafficSignSnap();

        when(trafficSignSnap.getSnap()).thenReturn(snap);
        when(trafficSign.roadSectionId()).thenReturn(ROAD_SECTION_ID);
        when(trafficSign.id()).thenReturn(TRAFFIC_SIGN_ID_VALUE);
        when(edgeIteratorStateReverseExtractor.hasReversed(edgeIterator)).thenReturn(reversed);
        when(encodingManager.getIntEncodedValue(WAY_ID_KEY)).thenReturn(intEncodedValueWayId);
        when(edgeIterator.get(intEncodedValueWayId)).thenReturn(ROAD_SECTION_ID);

        if (reversed) {
            when(trafficSign.direction()).thenReturn(Direction.BACKWARD);
        } else {
            when(trafficSign.direction()).thenReturn(Direction.FORWARD);
        }

        when(snap.getSnappedPoint()).thenReturn(ghPoint);
        when(ghPoint.getLon()).thenReturn(LON);
        when(ghPoint.getLat()).thenReturn(LAT);
        when(edgeIterator.fetchWayGeometry(FetchMode.ALL)).thenReturn(pointList);
        when(pointList.toLineString(false)).thenReturn(lineString);
        when(lineString.getStartPoint()).thenReturn(point);
        //Latitude is the Y axis, longitude is the X axis
        when(point.getCoordinate()).thenReturn(new Coordinate(LON, LAT));

        verify(edgeManager).setValueOnEdge(edgeIterator, TRAFFIC_SIGN_ID, TRAFFIC_SIGN_ID_VALUE);
        verify(edgeManager).setValueOnEdge(edgeIterator, MOTOR_VEHICLE_ACCESS_FORBIDDEN_WINDOWED, true);
        verify(encodingManager, times(1))
                .getIntEncodedValue(WAY_ID_KEY);

        loggerExtension.containsLog(
                Level.INFO,
                "Query graph configuration summary. "
                        + "Total traffic signs in request 1. "
                        + "Total not assignable road sections with traffic sign 0, notAssigned {}");
    }

    @Test
    void configure_notAssigned_noTrafficSign_inDirection() {

        setupFixtureForQueryGraph();
        setupFixtureForTrafficSignSnap();

        when(trafficSignSnap.getSnap()).thenReturn(snap);
        when(trafficSign.direction()).thenReturn(Direction.FORWARD);
        when(trafficSign.roadSectionId()).thenReturn(ROAD_SECTION_ID);
        when(edgeIteratorStateReverseExtractor.hasReversed(edgeIterator)).thenReturn(true);

        verifyNoAssignment();
        loggerExtension.containsLog(Level.WARN, MESSAGE_NOT_ASSIGNED.formatted(ROAD_SECTION_ID));
    }

    @Test
    void configure_notAssigned_edgeNotBehind() {

        setupFixtureForQueryGraph();
        setupFixtureForTrafficSignSnap();

        when(trafficSignSnap.getSnap()).thenReturn(snap);
        when(trafficSign.roadSectionId()).thenReturn(ROAD_SECTION_ID);
        when(edgeIteratorStateReverseExtractor.hasReversed(edgeIterator)).thenReturn(false);
        when(trafficSign.direction()).thenReturn(Direction.FORWARD);
        when(snap.getSnappedPoint()).thenReturn(ghPoint);
        when(ghPoint.getLon()).thenReturn(2.0);
        when(ghPoint.getLat()).thenReturn(3.0);
        when(edgeIterator.fetchWayGeometry(FetchMode.ALL)).thenReturn(pointList);
        when(pointList.toLineString(false)).thenReturn(lineString);
        when(lineString.getStartPoint()).thenReturn(point);
        //Latitude is the Y axis, longitude is the X axis
        when(point.getCoordinate()).thenReturn(new Coordinate(LON, LAT));

        verify(edgeManager, times(0)).setValueOnEdge(edgeIterator, TRAFFIC_SIGN_ID, TRAFFIC_SIGN_ID_VALUE);
        verify(edgeManager, times(0)).setValueOnEdge(edgeIterator, MOTOR_VEHICLE_ACCESS_FORBIDDEN_WINDOWED, true);

        loggerExtension.containsLog(Level.WARN, MESSAGE_NOT_ASSIGNED.formatted(ROAD_SECTION_ID));
    }

    @Test
    void configure_notAssigned_roadSectionIdNotEqual() {

        setupFixtureForQueryGraph();
        setupFixtureForTrafficSignSnap();

        when(trafficSignSnap.getSnap()).thenReturn(snap);
        when(trafficSign.roadSectionId()).thenReturn(12);
        when(edgeIteratorStateReverseExtractor.hasReversed(edgeIterator)).thenReturn(false);
        when(encodingManager.getIntEncodedValue(WAY_ID_KEY)).thenReturn(intEncodedValueWayId);
        when(edgeIterator.get(intEncodedValueWayId)).thenReturn(ROAD_SECTION_ID);
        when(trafficSign.direction()).thenReturn(Direction.FORWARD);
        when(snap.getSnappedPoint()).thenReturn(ghPoint);
        when(ghPoint.getLon()).thenReturn(LON);
        when(ghPoint.getLat()).thenReturn(LAT);
        when(edgeIterator.fetchWayGeometry(FetchMode.ALL)).thenReturn(pointList);
        when(pointList.toLineString(false)).thenReturn(lineString);
        when(lineString.getStartPoint()).thenReturn(point);
        //Latitude is the Y axis, longitude is the X axis
        when(point.getCoordinate()).thenReturn(new Coordinate(LON, LAT));

        verify(edgeManager, times(0)).setValueOnEdge(edgeIterator, TRAFFIC_SIGN_ID, TRAFFIC_SIGN_ID_VALUE);
        verify(edgeManager, times(0)).setValueOnEdge(edgeIterator, MOTOR_VEHICLE_ACCESS_FORBIDDEN_WINDOWED, true);

        loggerExtension.containsLog(Level.WARN,
                "Traffic sign trafficSignSnap and road section id 12 does not match linked edge with road section id 123");
    }

    private void setupFixtureForTrafficSignSnap() {

        when(trafficSignSnap.getTrafficSign()).thenReturn(trafficSign);
    }

    private void setupFixtureForQueryGraph() {

        when(queryGraph.createEdgeExplorer()).thenReturn(edgeExplorer);
        when(queryGraph.getNodes()).thenReturn(1);
        when(queryGraph.getEdges()).thenReturn(1);
        when(edgeExplorer.setBaseNode(0)).thenReturn(edgeIterator);
        when(edgeIterator.next()).thenReturn(true, false);
    }

    private void verifyNoAssignment() {

        verify(edgeManager, times(0)).setValueOnEdge(edgeIterator, TRAFFIC_SIGN_ID, TRAFFIC_SIGN_ID_VALUE);
        verify(edgeManager, times(0)).setValueOnEdge(edgeIterator, MOTOR_VEHICLE_ACCESS_FORBIDDEN_WINDOWED, true);
    }
}
