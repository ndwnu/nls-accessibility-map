package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.util;

import static nu.ndw.nls.routingmapmatcher.network.model.Link.REVERSED_LINK_ID;
import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;

import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.EdgeIteratorState;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LinkIdResolver {

    public static int resolveLinkId(EdgeIteratorState edgeIteratorState, EncodingManager encodingManager, boolean reversed) {
        if (reversed && hasReversedLinkId(edgeIteratorState, encodingManager)) {
            return getReversedLinkId(edgeIteratorState, encodingManager);
        }
        return getLinkId(edgeIteratorState, encodingManager);
    }

    private static boolean hasReversedLinkId(EdgeIteratorState edge, EncodingManager encodingManager) {
        return getReversedLinkId(edge, encodingManager) > 0;
    }

    private static int getLinkId(EdgeIteratorState edge, EncodingManager encodingManager) {
        return edge.get(encodingManager.getIntEncodedValue(WAY_ID_KEY));
    }

    private static int getReversedLinkId(EdgeIteratorState edge, EncodingManager encodingManager) {
        return edge.get(encodingManager.getIntEncodedValue(REVERSED_LINK_ID));
    }
}
