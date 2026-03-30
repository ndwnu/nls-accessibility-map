package nu.ndw.nls.accessibilitymap.accessibility.service;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.index.Snap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Location;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.SnapRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.querygraph.QueryGraphConfigurer;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.util.Snapper;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.weighting.WeightingFactory;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.roadchange.dto.RoadChanges;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityNetwork;
import nu.ndw.nls.accessibilitymap.accessibility.service.exception.AccessibilityLocationNotFoundException;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessibilityNetworkProvider {

    private final QueryGraphConfigurer queryGraphConfigurer;

    private final Snapper snapper;

    private final WeightingFactory weightingFactory;

    public AccessibilityNetwork get(
            NetworkData networkData,
            Restrictions restrictions,
            RoadChanges roadChanges,
            Location from,
            Location destination
    ) {

        NetworkGraphHopper networkGraphHopper = networkData.getNetworkGraphHopper();

        Optional<Snap> fromSnap = snapper.snapLocation(networkGraphHopper, from);
        if (fromSnap.isEmpty()) {
            throw new AccessibilityLocationNotFoundException(from);
        }
        Optional<Snap> destinationSnap = snapper.snapLocation(networkGraphHopper, destination);
        if (Objects.nonNull(destination) && destinationSnap.isEmpty()) {
            throw new AccessibilityLocationNotFoundException(from);
        }

        List<SnapRestriction> snapRestrictions = restrictions.stream()
                .map(restriction -> snapper.snapRestriction(networkGraphHopper, restriction)
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

        QueryGraph queryGraph = QueryGraph.create(networkGraphHopper.getBaseGraph(), snaps);
        Map<Integer, List<Restriction>> integerListMap = queryGraphConfigurer.createEdgeRestrictions(
                networkData.getEncodingManager(),
                queryGraph,
                snapRestrictions);
        Weighting weightingWithRestrictions = weightingFactory.createWeighting(networkData,
                queryGraph,
                integerListMap.keySet(),
                roadChanges,
                true);
        Weighting weightingWithoutRestrictions = weightingFactory.createWeighting(networkData,
                queryGraph,
                integerListMap.keySet(),
                roadChanges,
                false);

        return new AccessibilityNetwork(
                networkData,
                roadChanges,
                queryGraph,
                restrictions, integerListMap,
                fromSnap.get(),
                destinationSnap.orElse(null),
                weightingWithRestrictions,
                weightingWithoutRestrictions);
    }
}
