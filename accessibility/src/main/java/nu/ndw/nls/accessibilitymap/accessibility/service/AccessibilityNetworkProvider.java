package nu.ndw.nls.accessibilitymap.accessibility.service;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.storage.index.Snap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Location;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.SnapRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.GraphHopperNetwork;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.querygraph.QueryGraphConfigurer;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.util.Snapper;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityNetwork;
import nu.ndw.nls.accessibilitymap.accessibility.service.exception.AccessibilityLocationNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessibilityNetworkProvider {

    private final QueryGraphConfigurer queryGraphConfigurer;

    private final Snapper snapper;

    public AccessibilityNetwork get(
            NetworkData networkData,
            Restrictions restrictions,
            Location from,
            Location destination) {

        GraphHopperNetwork graphHopperNetwork = networkData.getGraphHopperNetwork();

        Optional<Snap> fromSnap = snapper.snapLocation(graphHopperNetwork.network(), from);
        if (fromSnap.isEmpty()) {
            throw new AccessibilityLocationNotFoundException(from);
        }
        Optional<Snap> destinationSnap = snapper.snapLocation(graphHopperNetwork.network(), destination);
        if (Objects.nonNull(destination) && destinationSnap.isEmpty()) {
            throw new AccessibilityLocationNotFoundException(from);
        }

        List<SnapRestriction> snapRestrictions = restrictions.stream()
                .map(restriction -> snapper.snapRestriction(graphHopperNetwork.network(), restriction)
                        .map(snap -> new SnapRestriction(snap, restriction)).orElse(null))
                .filter(Objects::nonNull)
                .toList();

        List<Snap> snaps = Stream.of(
                        snapRestrictions.stream().map(SnapRestriction::snap),
                        Stream.of(fromSnap.get()),
                        destinationSnap.stream()
                )
                .flatMap(snapStream -> snapStream)
                .toList();

        QueryGraph queryGraph = QueryGraph.create(graphHopperNetwork.network().getBaseGraph(), snaps);

        return new AccessibilityNetwork(
                networkData,
                queryGraph,
                restrictions,
                queryGraphConfigurer.createEdgeRestrictions(
                        networkData.getGraphHopperNetwork().network().getEncodingManager(),
                        queryGraph,
                        snapRestrictions),
                fromSnap.get(),
                destinationSnap.orElse(null));
    }
}
