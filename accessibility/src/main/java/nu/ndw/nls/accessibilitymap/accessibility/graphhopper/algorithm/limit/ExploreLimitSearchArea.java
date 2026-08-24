package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.algorithm.limit;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.FetchMode;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.BBox;
import lombok.Getter;
import lombok.ToString;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.algorithm.RestrictionsIsochroneLabel;
import nu.ndw.nls.routingmapmatcher.isochrone.v2.exploration.ExploreLimit;

@ToString(callSuper = true)
public class ExploreLimitSearchArea extends ExploreLimit<RestrictionsIsochroneLabel> {

    public static final int LIMIT = 1;

    public static final int IN_ACCESSIBLE = 2;

    public static final int ACCESSIBLE = 0;

    @Getter
    private final BBox searchArea;

    @Getter
    private final QueryGraph queryGraph;

    public ExploreLimitSearchArea(
            BBox searchArea,
            QueryGraph queryGraph
    ) {
        super(LIMIT, false);

        this.searchArea = searchArea;
        this.queryGraph = queryGraph;
    }

    @Override
    protected double getValueForLabel(RestrictionsIsochroneLabel restrictionsIsochroneLabel, EncodingManager encodingManager) {

        EdgeIteratorState currentEdge = queryGraph.getEdgeIteratorState(
                restrictionsIsochroneLabel.getEdge(),
                restrictionsIsochroneLabel.getNode());
        PointList points = currentEdge.fetchWayGeometry(FetchMode.TOWER_ONLY);

        return searchArea.intersects(points)
                ? ACCESSIBLE
                : IN_ACCESSIBLE;
    }

    @Override
    public String debug(RestrictionsIsochroneLabel isochroneLabel, EncodingManager encodingManager) {
        return "ExploreLimitSearchArea{searchArea=%s, isWithinSearchArea=%s}".formatted(
                searchArea,
                isInLimit(isochroneLabel, encodingManager)
        );
    }
}
