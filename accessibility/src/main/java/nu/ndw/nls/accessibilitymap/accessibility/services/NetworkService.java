package nu.ndw.nls.accessibilitymap.accessibility.services;

import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.util.EdgeIteratorState;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.model.WindowTimeEncodedValue;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NetworkService {

    private final NetworkGraphHopper networkGraphHopper;

    public boolean hasWindowTimeByRoadSectionId(long roadSectionId, WindowTimeEncodedValue windowTimeEncodedValue) {
        Integer edgeKey = networkGraphHopper.getEdgeMap().get(roadSectionId);
        EdgeIteratorState edge = networkGraphHopper.getBaseGraph().getEdgeIteratorStateForKey(edgeKey);
        BooleanEncodedValue encodedValue = networkGraphHopper.getEncodingManager()
                .getBooleanEncodedValue(windowTimeEncodedValue.getEncodedValue());

        return edge.get(encodedValue) || edge.getReverse(encodedValue);
    }

}
