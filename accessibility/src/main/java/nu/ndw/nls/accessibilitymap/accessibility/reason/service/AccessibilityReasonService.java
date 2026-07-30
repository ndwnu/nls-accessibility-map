package nu.ndw.nls.accessibilitymap.accessibility.reason.service;

import static com.graphhopper.routing.util.TraversalMode.EDGE_BASED;
import static com.graphhopper.util.Parameters.Algorithms.DIJKSTRA;
import static com.graphhopper.util.Parameters.Routing.PASS_THROUGH;

import com.google.common.base.Stopwatch;
import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.RoutingAlgorithm;
import com.graphhopper.routing.RoutingAlgorithmFactory;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.PMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NwbNetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReasonGroup;
import nu.ndw.nls.accessibilitymap.accessibility.reason.graphhopper.PathsToReasonsMapper;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityNetwork;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccessibilityReasonService {

    private final RoutingAlgorithmFactory routingAlgorithmFactory;

    private final PathsToReasonsMapper pathsToReasonsMapper;

    @SuppressWarnings("java:S3553")
    public List<AccessibilityReasonGroup> calculateReasons(
            Optional<DirectionalSegment> toSegment,
            Map<Integer, DirectionalSegment> directionalSegmentsById,
            AccessibilityNetwork accessibilityNetwork,
            boolean effectivelyAccessible
    ) {
        return toSegment
                .filter(directionalSegment -> isInaccessible(directionalSegment, effectivelyAccessible))
                .map(directionalSegment -> calculateReasons(directionalSegmentsById, accessibilityNetwork))
                .orElse(Collections.emptyList());
    }

    private static boolean isInaccessible(DirectionalSegment directionalSegment, boolean effectivelyAccessible) {
        if (effectivelyAccessible) {
            return !directionalSegment.getRoadSectionFragment().isAccessibleFromAnySegment();
        } else {
            return !directionalSegment.isAccessible();
        }
    }

    @SuppressWarnings("java:S1941")
    private List<AccessibilityReasonGroup> calculateReasons(
            Map<Integer, DirectionalSegment> directionalSegmentsById,
            AccessibilityNetwork accessibilityNetwork
    ) {

        Stopwatch stopwatch = Stopwatch.createStarted();

        Snap from = accessibilityNetwork.getFrom();
        Snap destination = accessibilityNetwork.getDestination();

        // debug write geojson with the snapped from and destination

        if (Objects.isNull(destination)) {
            return List.of();
        }

        RoutingAlgorithm router = routingAlgorithmFactory.createAlgo(
                accessibilityNetwork.getQueryGraph(),
                accessibilityNetwork.getWeightingOnlyCarAccessible(),
                createAlgorithmOptions());

        List<Path> routes = router.calcPaths(from.getClosestNode(), destination.getClosestNode()).stream()
                .filter(Path::isFound)
                .toList();

        if (routes.isEmpty()) {
            return List.of();
        }

        NwbNetworkData nwbNetworkData = accessibilityNetwork.getNetworkData().getNwbNetworkData();

        List<AccessibilityReasonGroup> reasons = pathsToReasonsMapper.mapRoutesToReasons(
                routes,
                directionalSegmentsById,
                nwbNetworkData);
        log.debug("Calculating accessibility reasons took: {} ms", stopwatch.elapsed().toMillis());

        return reasons;
    }

    public static AlgorithmOptions createAlgorithmOptions() {

        AlgorithmOptions algorithmOptions = new AlgorithmOptions();
        algorithmOptions.setAlgorithm(DIJKSTRA);
        algorithmOptions.setTraversalMode(EDGE_BASED);
        algorithmOptions.setHints(new PMap(Map.of(
                PASS_THROUGH, true
        )));

        return algorithmOptions;
    }
}
