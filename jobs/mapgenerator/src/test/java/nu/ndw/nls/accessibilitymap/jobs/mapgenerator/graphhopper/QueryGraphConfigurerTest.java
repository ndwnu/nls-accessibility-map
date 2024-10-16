package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.graphhopper;

import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import com.graphhopper.routing.ev.BooleanEncodedValue;
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
import java.util.Arrays;
import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.model.WindowTimeEncodedValue;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.TrafficSignSnap;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.test.util.LoggerExtension;
import nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QueryGraphConfigurerTest {

    private static final int ROAD_SECTION_ID = 123;
    private static final double LON = 0.0;
    private static final double LAT = 1.0;
    private static final int TRAFFIC_SIGN_ID_VALUE = 1;
    private static final int NO_MATCH_ROAD_SECTION_ID = 456;
    private static final String MESSAGE_NOT_ASSIGNED = "Query graph configuration summary. "
            + "Total traffic signs in request 1. "
            + "Total not assignable road sections with traffic sign 1, notAssigned {123=[trafficSignSnap]}";

    @Mock
    private EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor;
    @Mock
    private EncodingManager encodingManager;

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
    private BooleanEncodedValue booleanEncodedValue;
    @Mock
    private IntEncodedValue intEncodedValueWayId;
    @Mock
    private IntEncodedValue intEncodedValueTrafficSignId;
    @Mock
    private GHPoint3D ghPoint;
    @Mock
    private PointList pointList;
    @Mock
    private LineString lineString;
    @Mock
    private Point point;
    @Captor
    private ArgumentCaptor<String> encodedValueKeyCaptor;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @InjectMocks
    private QueryGraphConfigurer queryGraphConfigurer;

    @ParameterizedTest
    @CsvSource(textBlock = """
            false
            true
            """)
    void configure_ok_assigned(boolean reversed) {
        setupFixtureForQueryGraph();
        setupFixtureForTrafficSignSnap();
        when(trafficSignSnap.getSnap()).thenReturn(snap);
        when(trafficSign.roadSectionId()).thenReturn(ROAD_SECTION_ID);
        when(trafficSign.id()).thenReturn(TRAFFIC_SIGN_ID_VALUE);
        when(edgeIteratorStateReverseExtractor.hasReversed(edgeIterator)).thenReturn(reversed);
        when(encodingManager.getBooleanEncodedValue(any())).thenReturn(booleanEncodedValue);
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
        when(encodingManager.getIntEncodedValue(AccessibilityLink.TRAFFIC_SIGN_ID))
                .thenReturn(intEncodedValueTrafficSignId);
        when(trafficSign.trafficSignType()).thenReturn(TrafficSignType.C12);
        queryGraphConfigurer.configure(queryGraph, List.of(trafficSignSnap));

        if (reversed) {
            verify(edgeIterator).setReverse(intEncodedValueTrafficSignId, TRAFFIC_SIGN_ID_VALUE);
            verify(edgeIterator).setReverse(booleanEncodedValue, true);
        } else {
            verify(edgeIterator).set(intEncodedValueTrafficSignId, TRAFFIC_SIGN_ID_VALUE);
            verify(edgeIterator).set(booleanEncodedValue, true);
        }

        verify(encodingManager, times(WindowTimeEncodedValue.values().length + 1))
                .getBooleanEncodedValue(encodedValueKeyCaptor.capture());

        loggerExtension.containsLog(
                Level.INFO,
                "Query graph configuration summary. "
                        + "Total traffic signs in request 1. "
                        + "Total not assignable road sections with traffic sign 0, notAssigned {}");

        List<String> allKeys = Arrays.stream(WindowTimeEncodedValue.values())
                .map(windowTimeEncodedValue -> windowTimeEncodedValue.getEncodedValue())
                .toList();

        assertThat(encodedValueKeyCaptor.getAllValues())
                .allMatch(allKeys::contains);

        assertThat(encodedValueKeyCaptor.getAllValues().getLast())
                .isEqualTo(WindowTimeEncodedValue.C12.getEncodedValue());
    }

    @Test
    void configure_ok_notAssigned_noTrafficSign() {

        setupFixtureForQueryGraph();
        setupFixtureForTrafficSignSnap();
        when(trafficSign.roadSectionId()).thenReturn(ROAD_SECTION_ID);
        when(encodingManager.getBooleanEncodedValue(any())).thenReturn(booleanEncodedValue);
        when(encodingManager.getIntEncodedValue(WAY_ID_KEY)).thenReturn(intEncodedValueWayId);
        when(edgeIterator.get(intEncodedValueWayId)).thenReturn(NO_MATCH_ROAD_SECTION_ID);

        queryGraphConfigurer.configure(queryGraph, List.of(trafficSignSnap));

        verifyNoAssignment();
        loggerExtension.containsLog(Level.WARN, MESSAGE_NOT_ASSIGNED);
    }

    @Test
    void configure_ok_notAssigned_noTrafficSign_inDirection() {

        setupFixtureForQueryGraph();
        setupFixtureForTrafficSignSnap();
        when(trafficSign.direction()).thenReturn(Direction.FORWARD);
        when(trafficSign.roadSectionId()).thenReturn(ROAD_SECTION_ID);
        when(edgeIteratorStateReverseExtractor.hasReversed(edgeIterator)).thenReturn(true);
        when(encodingManager.getBooleanEncodedValue(any())).thenReturn(booleanEncodedValue);
        when(encodingManager.getIntEncodedValue(WAY_ID_KEY)).thenReturn(intEncodedValueWayId);
        when(edgeIterator.get(intEncodedValueWayId)).thenReturn(ROAD_SECTION_ID);

        queryGraphConfigurer.configure(queryGraph, List.of(trafficSignSnap));

        verifyNoAssignment();
        loggerExtension.containsLog(Level.WARN, MESSAGE_NOT_ASSIGNED);
    }


    @Test
    void configure_ok_notAssigned_edgeNotBehind() {

        setupFixtureForQueryGraph();
        setupFixtureForTrafficSignSnap();
        when(trafficSignSnap.getSnap()).thenReturn(snap);
        when(trafficSign.roadSectionId()).thenReturn(ROAD_SECTION_ID);
        when(edgeIteratorStateReverseExtractor.hasReversed(edgeIterator)).thenReturn(false);
        when(encodingManager.getBooleanEncodedValue(any())).thenReturn(booleanEncodedValue);
        when(encodingManager.getIntEncodedValue(WAY_ID_KEY)).thenReturn(intEncodedValueWayId);
        when(edgeIterator.get(intEncodedValueWayId)).thenReturn(ROAD_SECTION_ID);
        when(trafficSign.direction()).thenReturn(Direction.FORWARD);
        when(snap.getSnappedPoint()).thenReturn(ghPoint);
        when(ghPoint.getLon()).thenReturn(2.0);
        when(ghPoint.getLat()).thenReturn(3.0);
        when(edgeIterator.fetchWayGeometry(FetchMode.ALL)).thenReturn(pointList);
        when(pointList.toLineString(false)).thenReturn(lineString);
        when(lineString.getStartPoint()).thenReturn(point);
        //Latitude is the Y axis, longitude is the X axis
        when(point.getCoordinate()).thenReturn(new Coordinate(LON, LAT));

        queryGraphConfigurer.configure(queryGraph, List.of(trafficSignSnap));

        verify(encodingManager, times(0))
                .getIntEncodedValue(AccessibilityLink.TRAFFIC_SIGN_ID);
        loggerExtension.containsLog(Level.WARN, MESSAGE_NOT_ASSIGNED);
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

        verify(trafficSignSnap, times(0)).getSnap();
        verify(encodingManager, times(0))
                .getIntEncodedValue(AccessibilityLink.TRAFFIC_SIGN_ID);
    }

}
