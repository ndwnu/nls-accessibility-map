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
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityContext;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityNetwork;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessibilityNetworkProvider {

    private final QueryGraphConfigurer queryGraphConfigurer;

    private final Snapper snapper;

    public AccessibilityNetwork get(
            AccessibilityContext accessibilityContext,
            Restrictions restrictions,
            Location from,
            Location destination) {

        GraphHopperNetwork graphHopperNetwork = accessibilityContext.graphHopperNetwork();

        Optional<Snap> fromSnap = snapper.snapLocation(graphHopperNetwork.network(), from);
        if (fromSnap.isEmpty()) {
            throw new AccessibilityException("Could not find a snap point for from location (%s, %s).".formatted(
                    from.latitude(),
                    from.longitude()
            ));
        }
        Optional<Snap> destinationSnap = snapper.snapLocation(graphHopperNetwork.network(), destination);
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
                accessibilityContext,
                queryGraph,
                restrictions,
                queryGraphConfigurer.createEdgeRestrictions(queryGraph, snapRestrictions),
                fromSnap.get(),
                destinationSnap.orElse(null));
    }
}
