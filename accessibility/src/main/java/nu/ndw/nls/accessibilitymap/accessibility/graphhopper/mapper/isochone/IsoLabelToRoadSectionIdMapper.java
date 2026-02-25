package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.mapper.isochone;

import static nu.ndw.nls.routingmapmatcher.network.model.Link.REVERSED_LINK_ID;
import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;

import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.EdgeIteratorState;
import org.springframework.stereotype.Component;

@Component
public class IsoLabelToRoadSectionIdMapper {

    public int map(
            EdgeIteratorState edge,
            EncodingManager encodingManager,
            boolean isReversed,
            boolean isochroneCalculatedInReverse
            ) {
        return isReversed != isochroneCalculatedInReverse
                ? getReversedLinkId(edge, encodingManager)
                : getLinkId(edge, encodingManager);
    }

    private static int getLinkId(EdgeIteratorState edge, EncodingManager encodingManager) {
        return edge.get(encodingManager.getIntEncodedValue(WAY_ID_KEY));
    }

    private static int getReversedLinkId(EdgeIteratorState edge, EncodingManager encodingManager) {
        return edge.get(encodingManager.getIntEncodedValue(REVERSED_LINK_ID));
    }
}
