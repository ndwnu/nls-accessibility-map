package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.mapper.isochone;

import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;

import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.EdgeIteratorState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IsoLabelToRoadSectionIdMapper {

    public int map(
            EdgeIteratorState edge,
            EncodingManager encodingManager
    ) {
        return edge.get(encodingManager.getIntEncodedValue(WAY_ID_KEY));
    }
}
