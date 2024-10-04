package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.graphhopper;

import static java.util.stream.Collectors.groupingBy;
import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.accessibility.dto.AdditionalSnap;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueryGraphConfigurer {

    private final NetworkGraphHopper networkGraphHopper;
    private final EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor;

    public void configure(QueryGraph queryGraph, List<AdditionalSnap> snappedTrafficSigns) {
        EncodingManager encodingManager = networkGraphHopper.getEncodingManager();
        Map<Integer, List<AdditionalSnap>> additionalSnapByRoadSectionId = snappedTrafficSigns
                .stream()
                .collect(groupingBy(additionalSnap -> additionalSnap.getTrafficSign().roadSectionId()));
        EdgeExplorer edgeExplorer = queryGraph.createEdgeExplorer();
        for (int startNode = 0; startNode < queryGraph.getNodes(); startNode++) {
            EdgeIterator edgeIterator = edgeExplorer.setBaseNode(startNode);
            while (edgeIterator.next()) {
                int linkId = getLinkId(edgeIterator, encodingManager);
                boolean directionReversed = edgeIteratorStateReverseExtractor.hasReversed(edgeIterator);
                if (edgeHasTrafficSigns(linkId,additionalSnapByRoadSectionId)) {
                    Predicate<AdditionalSnap> filterOnDirection = directionReversed
                            ? snap -> snap.getTrafficSign().direction().isBackward()
                            : snap -> snap.getTrafficSign().direction().isForward();
                    List<AdditionalSnap> additionalSnaps = additionalSnapByRoadSectionId
                            .get(linkId)
                            .stream()
                            .filter(filterOnDirection)
                            .toList();

                }

            }
        }
    }
    private boolean edgeHasTrafficSigns(int linkId,Map<Integer, List<AdditionalSnap>> additionalSnapByRoadSectionId ) {
        return additionalSnapByRoadSectionId.containsKey(linkId);
    }
    private int getLinkId(EdgeIteratorState edge, EncodingManager encodingManager) {
        return edge.get(encodingManager.getIntEncodedValue(WAY_ID_KEY));
    }
}
