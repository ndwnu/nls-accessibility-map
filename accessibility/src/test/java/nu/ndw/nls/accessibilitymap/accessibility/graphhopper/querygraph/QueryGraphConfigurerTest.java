package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.querygraph;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.FetchMode;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint3D;
import java.util.Map;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QueryGraphConfigurerTest {

    private QueryGraphConfigurer queryGraphConfigurer;

    @Mock
    private EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor;

    @Mock
    private QueryGraph queryGraph;

    @Mock
    private Restriction restriction;

    @Mock
    private Snap snap;

    @Mock
    private EdgeExplorer edgeExplorer;

    @Mock
    private EdgeIterator edgeIterator;

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

    @BeforeEach
    void setUp() {

        queryGraphConfigurer = new QueryGraphConfigurer(edgeIteratorStateReverseExtractor);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            false
            true
            """)
    void createEdgeRestrictions(boolean reversed) {
        setupFixtureForQueryGraph();

        when(snap.getClosestNode()).thenReturn(0);

        when(edgeIteratorStateReverseExtractor.hasReversed(edgeIterator)).thenReturn(reversed);
        when(edgeIterator.fetchWayGeometry(FetchMode.ALL)).thenReturn(pointList);
        when(pointList.toLineString(false)).thenReturn(lineString);
        when(lineString.getStartPoint()).thenReturn(point);
        when(point.getCoordinate()).thenReturn(new Coordinate(1.0, 2.0));
        when(snap.getSnappedPoint()).thenReturn(ghPoint);
        when(ghPoint.getLon()).thenReturn(1.0);
        when(ghPoint.getLat()).thenReturn(2.0);

        when(restriction.direction()).thenReturn(reversed ? Direction.BACKWARD : Direction.FORWARD);
        Map<Integer, Restriction> restrictionsByEdgeKey = queryGraphConfigurer.createEdgeRestrictions(
                queryGraph,
                Map.of(restriction, snap));

        assertThat(restrictionsByEdgeKey.get(edgeIterator.getEdgeKey())).isEqualTo(restriction);
        loggerExtension.containsLog(
                Level.INFO,
                "Query graph configuration summary. "
                + "Total restriction: 1. "
                + "Total not assignable restrictions: 0. []");
    }

    @Test
    void createEdgeRestrictions_noRestrictionAssigned_edgeNotInSameDirection() {
        setupFixtureForQueryGraph();

        when(snap.getClosestNode()).thenReturn(0);

        when(edgeIteratorStateReverseExtractor.hasReversed(edgeIterator)).thenReturn(true);
        when(restriction.direction()).thenReturn(Direction.FORWARD);

        Map<Integer, Restriction> restrictionsByEdgeKey = queryGraphConfigurer.createEdgeRestrictions(
                queryGraph,
                Map.of(restriction, snap));

        assertThat(restrictionsByEdgeKey).isEmpty();
        loggerExtension.containsLog(
                Level.WARN,
                "Query graph configuration summary. "
                + "Total restriction: 1. "
                + "Total not assignable restrictions: 1. [restriction]");
    }

    @Test
    void createEdgeRestrictions_noRestrictionAssigned_isTrafficSignInFrontOfEdge() {
        setupFixtureForQueryGraph();

        when(snap.getClosestNode()).thenReturn(0);

        when(edgeIteratorStateReverseExtractor.hasReversed(edgeIterator)).thenReturn(false);
        when(restriction.direction()).thenReturn(Direction.FORWARD);

        when(edgeIterator.fetchWayGeometry(FetchMode.ALL)).thenReturn(pointList);
        when(pointList.toLineString(false)).thenReturn(lineString);
        when(lineString.getStartPoint()).thenReturn(point);
        when(point.getCoordinate()).thenReturn(new Coordinate(1.0, 2.0));
        when(snap.getSnappedPoint()).thenReturn(ghPoint);
        when(ghPoint.getLon()).thenReturn(3.0);
        when(ghPoint.getLat()).thenReturn(4.0);

        Map<Integer, Restriction> restrictionsByEdgeKey = queryGraphConfigurer.createEdgeRestrictions(
                queryGraph,
                Map.of(restriction, snap));

        assertThat(restrictionsByEdgeKey).isEmpty();
        loggerExtension.containsLog(
                Level.WARN,
                "Query graph configuration summary. "
                + "Total restriction: 1. "
                + "Total not assignable restrictions: 1. [restriction]");
    }

    private void setupFixtureForQueryGraph() {

        when(queryGraph.createEdgeExplorer()).thenReturn(edgeExplorer);
        when(queryGraph.getNodes()).thenReturn(1);
        when(queryGraph.getEdges()).thenReturn(1);
        when(edgeExplorer.setBaseNode(0)).thenReturn(edgeIterator);
        when(edgeIterator.next()).thenReturn(true, false);
    }
}
