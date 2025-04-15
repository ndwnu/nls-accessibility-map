package nu.ndw.nls.accessibilitymap.accessibility.services.mappers;

import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;

import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.EdgeIteratorState;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.TrafficSignSnap;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.springframework.stereotype.Component;

/**
 * The TrafficSignSnapMapper class is responsible for mapping traffic signs to their closest snap point on a network. It utilizes the
 * provided NetworkGraphHopper instance to match traffic signs to valid road sections on the network.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TrafficSignSnapMapper {

    private final NetworkGraphHopper networkGraphHopper;

    /**
     * Maps a collection of TrafficSign objects to their corresponding TrafficSignSnap objects. For each TrafficSign, it tries to find its
     * closest snap point on the network and creates a TrafficSignSnap object if a valid snap is found.
     *
     * @param trafficSigns the collection of TrafficSign objects to be mapped
     * @return a list of TrafficSignSnap objects representing the mapping of TrafficSign objects to their closest snap point on the network
     */
    public List<TrafficSignSnap> map(Collection<TrafficSign> trafficSigns) {

        return trafficSigns.stream()
                .map(trafficSign -> buildSnap(trafficSign)
                        .map(snap -> TrafficSignSnap
                                .builder()
                                .trafficSign(trafficSign)
                                .snap(snap)
                                .build()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private Optional<Snap> buildSnap(TrafficSign trafficSign) {
        Snap snap = networkGraphHopper.getLocationIndex()
                .findClosest(
                        trafficSign.networkSnappedLatitude(),
                        trafficSign.networkSnappedLongitude(),
                        edgeIteratorState -> trafficSignMatchesEdge(trafficSign, edgeIteratorState)
                );
        if (!snap.isValid()) {
            log.warn("No road section present for traffic sign id {} with road section id {} in nwb map on graphhopper network",
                    trafficSign.externalId(), trafficSign.roadSectionId());
        }

        return snap.isValid() ? Optional.of(snap) : Optional.empty();
    }

    /**
     * Tries to match the traffic sign with the edge. They must have the same road section id. If not it should not be linking up. This
     * seems strange at first, but sometimes it happens that a traffic sign is linked to a cycling lane. This graph contains only road
     * sections accessible by motorized vehicles so it might be linked to the closest road and thus will be placed incorrectly. That is why
     * we check this.
     *
     * @param trafficSign       - The traffic sign to compare to
     * @param edgeIteratorState - The edge that it should be linked up with.
     * @return - True if they match, false if they don't.
     */
    private boolean trafficSignMatchesEdge(TrafficSign trafficSign, EdgeIteratorState edgeIteratorState) {

        return getLinkId(edgeIteratorState) == trafficSign.roadSectionId();
    }

    private int getLinkId(EdgeIteratorState edge) {

        return edge.get(networkGraphHopper.getEncodingManager().getIntEncodedValue(WAY_ID_KEY));
    }
}
