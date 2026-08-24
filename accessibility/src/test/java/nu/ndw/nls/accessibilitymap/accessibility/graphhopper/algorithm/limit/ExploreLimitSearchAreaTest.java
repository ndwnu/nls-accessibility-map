package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.algorithm.limit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.FetchMode;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.BBox;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.algorithm.RestrictionsIsochroneLabel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExploreLimitSearchAreaTest {

    private ExploreLimitSearchArea exploreLimitSearchArea;

    @Mock
    private QueryGraph queryGraph;

    @Mock
    private EncodingManager encodingManager;

    @Mock
    private EdgeIteratorState edgeIteratorState;

    @BeforeEach
    void setUp() {
        exploreLimitSearchArea = new ExploreLimitSearchArea(new BBox(0, 10, 0, 10), queryGraph);
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0, 10, 0",
            "0, 10, 10, 10",
            "0, 0, 0, 10",
            "10, 0, 10, 10"
    })
    void isInLimit_insideSearchArea(double fromLatitude, double fromLongitude, double toLatitude, double toLongitude) {
        RestrictionsIsochroneLabel label = createRestrictionsIsochroneLabel();

        mockWayGeometry(label, fromLatitude, fromLongitude, toLatitude, toLongitude);

        assertThat(exploreLimitSearchArea.isInLimit(label, encodingManager)).isTrue();
    }

    @ParameterizedTest
    @CsvSource({
            "0, -1, 10, -1",
            "0, 11, 10, 11",
            "-1, 0, -1, 10",
            "11, 0, 11, 10"
    })
    void isInLimit_outsideSearchArea(double fromLatitude, double fromLongitude, double toLatitude, double toLongitude) {
        RestrictionsIsochroneLabel label = createRestrictionsIsochroneLabel();

        mockWayGeometry(label, fromLatitude, fromLongitude, toLatitude, toLongitude);

        assertThat(exploreLimitSearchArea.isInLimit(label, encodingManager)).isFalse();
    }

    @Test
    void debug_insideSearchArea() {
        RestrictionsIsochroneLabel label = createRestrictionsIsochroneLabel();

        mockWayGeometry(label, 0, 0, 10, 10);

        assertThat(exploreLimitSearchArea.debug(label, encodingManager)).isEqualTo(
                "ExploreLimitSearchArea{searchArea=0.0,10.0,0.0,10.0, isWithinSearchArea=true}");
    }

    @Test
    void debug_outsideSearchArea() {
        RestrictionsIsochroneLabel label = createRestrictionsIsochroneLabel();

        mockWayGeometry(label, 11, 11, 12, 12);

        assertThat(exploreLimitSearchArea.debug(label, encodingManager)).isEqualTo(
                "ExploreLimitSearchArea{searchArea=0.0,10.0,0.0,10.0, isWithinSearchArea=false}");
    }

    @Test
    void getQueryGraph() {

        assertThat(exploreLimitSearchArea.getQueryGraph()).isEqualTo(queryGraph);
    }

    private static RestrictionsIsochroneLabel createRestrictionsIsochroneLabel() {
        return new RestrictionsIsochroneLabel(0, 5, 5, null, 0L, 0.0, 0.0, new Restrictions(), false);
    }

    private void mockWayGeometry(
            RestrictionsIsochroneLabel label,
            double fromLatitude,
            double fromLongitude,
            double toLatitude,
            double toLongitude) {
        PointList points = new PointList();
        points.add(fromLatitude, fromLongitude);
        points.add(toLatitude, toLongitude);

        when(queryGraph.getEdgeIteratorState(label.getEdge(), label.getNode())).thenReturn(edgeIteratorState);
        when(edgeIteratorState.fetchWayGeometry(FetchMode.TOWER_ONLY)).thenReturn(points);
    }
}
