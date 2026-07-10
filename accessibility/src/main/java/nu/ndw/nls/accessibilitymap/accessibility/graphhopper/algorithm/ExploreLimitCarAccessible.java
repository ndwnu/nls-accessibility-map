package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.algorithm;

import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import com.graphhopper.util.EdgeIteratorState;
import lombok.Getter;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.util.IsCarAccessibleUtil;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NwbNetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import nu.ndw.nls.routingmapmatcher.isochrone.v2.exploration.ExploreLimit;

public class ExploreLimitCarAccessible extends ExploreLimit<RestrictionsIsochroneLabel> {

    public static final int LIMIT = 1;

    public static final int IN_ACCESSIBLE = 2;

    public static final int ACCESSIBLE = 0;

    @Getter
    private final QueryGraph queryGraph;

    @Getter
    private final NwbNetworkData nwbNetworkData;

    @Getter
    private final EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor;

    public ExploreLimitCarAccessible(
            QueryGraph queryGraph,
            NwbNetworkData nwbNetworkData,
            EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor) {

        super(LIMIT, false);

        this.queryGraph = queryGraph;
        this.nwbNetworkData = nwbNetworkData;
        this.edgeIteratorStateReverseExtractor = edgeIteratorStateReverseExtractor;
    }

    @Override
    protected double getValueForLabel(RestrictionsIsochroneLabel isochroneLabel, EncodingManager encodingManager) {

        int roadSectionId = getRoadSectionId(isochroneLabel, encodingManager);

        EdgeIteratorState edgeIteratorState = queryGraph.getEdgeIteratorStateForKey(isochroneLabel.getEdgeKey());
        boolean travellingInReversedDirection = edgeIteratorStateReverseExtractor.hasReversed(edgeIteratorState);

        return nwbNetworkData.findAccessibilityNwbRoadSectionById(roadSectionId).stream()
                .allMatch(accessibilityNwbRoadSection ->
                        IsCarAccessibleUtil.isAccessible(accessibilityNwbRoadSection, travellingInReversedDirection))
                ? ACCESSIBLE
                : IN_ACCESSIBLE;
    }

    private int getRoadSectionId(RestrictionsIsochroneLabel isochroneLabel, EncodingManager encodingManager) {

        return queryGraph.getEdgeIteratorState(isochroneLabel.getEdge(), isochroneLabel.getNode())
                .get(encodingManager.getIntEncodedValue(WAY_ID_KEY));
    }

    @Override
    public String debug(RestrictionsIsochroneLabel isochroneLabel, EncodingManager encodingManager) {
        int roadSectionId = getRoadSectionId(isochroneLabel, encodingManager);

        return "ExploreLimitCarAccessible{limit=%s, roadSectionId=%d, carriagewayTypeCode=%s, reached=%s}".formatted(
                getLimit(),
                roadSectionId,
                nwbNetworkData.findAccessibilityNwbRoadSectionById(roadSectionId)
                        .map(AccessibilityNwbRoadSection::carriagewayTypeCode)
                        .map(String::valueOf)
                        .orElse("No carriageway type code found"),
                !isInLimit(isochroneLabel, encodingManager)
        );
    }
}
