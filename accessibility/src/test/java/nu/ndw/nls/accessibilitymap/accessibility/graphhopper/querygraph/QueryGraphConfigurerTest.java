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
import java.util.List;
import java.util.Map;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.SnapRestriction;
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
    private Restriction restriction1;

    @Mock
    private Restriction restriction2;

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
    void createEdgeRestrictions_withRestrictionsOnTheExactSamePlace(boolean reversed) {

        when(queryGraph.createEdgeExplorer()).thenReturn(edgeExplorer);
        when(queryGraph.getNodes()).thenReturn(1);
        when(queryGraph.getEdges()).thenReturn(1);
        when(edgeExplorer.setBaseNode(24))
                .thenReturn(edgeIterator)
                .thenReturn(edgeIterator);
        when(edgeIterator.next())
                .thenReturn(true, false)
                .thenReturn(true, false);
        when(snap.getClosestNode()).thenReturn(24);

        when(edgeIteratorStateReverseExtractor.hasReversed(edgeIterator)).thenReturn(reversed);
        when(edgeIterator.fetchWayGeometry(FetchMode.ALL)).thenReturn(pointList);
        when(edgeIterator.getEdgeKey()).thenReturn(234);
        when(pointList.toLineString(false)).thenReturn(lineString);
        when(lineString.getStartPoint()).thenReturn(point);
        when(point.getCoordinate()).thenReturn(new Coordinate(1.0, 2.0));
        when(snap.getSnappedPoint()).thenReturn(ghPoint);
        when(ghPoint.getLon()).thenReturn(1.0);
        when(ghPoint.getLat()).thenReturn(2.0);

        when(restriction1.direction()).thenReturn(reversed ? Direction.BACKWARD : Direction.FORWARD);
        when(restriction2.direction()).thenReturn(reversed ? Direction.BACKWARD : Direction.FORWARD);
        Map<Integer, List<Restriction>> restrictionsByEdgeKey = queryGraphConfigurer.createEdgeRestrictions(
                queryGraph,
                List.of(
                        new SnapRestriction(snap, restriction1),
                        new SnapRestriction(snap, restriction2)
                ));

        assertThat(restrictionsByEdgeKey.get(234)).containsExactlyInAnyOrder(restriction1, restriction2);
        loggerExtension.containsLog(
                Level.INFO,
                "Query graph configuration summary. "
                + "Total restriction: 2. "
                + "Total not assignable restrictions: 0. []");
    }

    @Test
    void createEdgeRestrictions_noRestrictionAssigned_edgeNotInSameDirection() {

        when(queryGraph.createEdgeExplorer()).thenReturn(edgeExplorer);
        when(queryGraph.getNodes()).thenReturn(1);
        when(queryGraph.getEdges()).thenReturn(1);
        when(edgeExplorer.setBaseNode(24)).thenReturn(edgeIterator);
        when(edgeIterator.next()).thenReturn(true, false);
        when(snap.getClosestNode()).thenReturn(24);

        when(edgeIteratorStateReverseExtractor.hasReversed(edgeIterator)).thenReturn(true);
        when(restriction1.direction()).thenReturn(Direction.FORWARD);

        Map<Integer, List<Restriction>> restrictionsByEdgeKey = queryGraphConfigurer.createEdgeRestrictions(
                queryGraph,
                List.of(new SnapRestriction(snap, restriction1)));

        assertThat(restrictionsByEdgeKey).isEmpty();
        loggerExtension.containsLog(
                Level.WARN,
                "Query graph configuration summary. "
                + "Total restriction: 1. "
                + "Total not assignable restrictions: 1. [restriction1]");
    }

    @Test
    void createEdgeRestrictions_noRestrictionAssigned_isSnapInFrontOfEdge() {

        when(queryGraph.createEdgeExplorer()).thenReturn(edgeExplorer);
        when(queryGraph.getNodes()).thenReturn(1);
        when(queryGraph.getEdges()).thenReturn(1);
        when(edgeExplorer.setBaseNode(24)).thenReturn(edgeIterator);
        when(edgeIterator.next()).thenReturn(true, false);
        when(snap.getClosestNode()).thenReturn(24);

        when(edgeIteratorStateReverseExtractor.hasReversed(edgeIterator)).thenReturn(false);
        when(restriction1.direction()).thenReturn(Direction.FORWARD);

        when(edgeIterator.fetchWayGeometry(FetchMode.ALL)).thenReturn(pointList);
        when(pointList.toLineString(false)).thenReturn(lineString);
        when(lineString.getStartPoint()).thenReturn(point);
        when(point.getCoordinate()).thenReturn(new Coordinate(1.0, 2.0));
        when(snap.getSnappedPoint()).thenReturn(ghPoint);
        when(ghPoint.getLon()).thenReturn(3.0);
        when(ghPoint.getLat()).thenReturn(4.0);

        Map<Integer, List<Restriction>> restrictionsByEdgeKey = queryGraphConfigurer.createEdgeRestrictions(
                queryGraph,
                List.of(new SnapRestriction(snap, restriction1)));

        assertThat(restrictionsByEdgeKey).isEmpty();
        loggerExtension.containsLog(
                Level.WARN,
                "Query graph configuration summary. "
                + "Total restriction: 1. "
                + "Total not assignable restrictions: 1. [restriction1]");
    }
}
