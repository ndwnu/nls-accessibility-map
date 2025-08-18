package nu.ndw.nls.accessibilitymap.accessibility.service;

import com.google.common.base.Stopwatch;
import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.RoutingAlgorithm;
import com.graphhopper.routing.RoutingAlgorithmFactory;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.PMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.factory.AlgorithmOptionsFactory;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.TrafficSignSnap;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityReasons;
import nu.ndw.nls.accessibilitymap.accessibility.service.mapper.AccessibilityReasonsMapper;
import nu.ndw.nls.accessibilitymap.accessibility.service.mapper.PathsToReasonsMapper;
import nu.ndw.nls.accessibilitymap.accessibility.service.mapper.TrafficSignSnapMapper;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.springframework.stereotype.Service;

/**
 * Service class responsible for calculating and retrieving accessibility reasons based on specific accessibility requests.
 * <p>
 * This service uses various components such as traffic sign data, mapping services, and routing algorithms to analyze accessibility-related
 * obstacles and provide detailed reasons for accessibility issues in a given route or area.
 * <p>
 * Responsibilities of this service include: - Querying traffic signs applicable to a given accessibility request. - Mapping traffic sign
 * data to relevant segments of a network graph. - Calculating routes and accessing alternative paths using routing algorithms. -
 * Correlating route paths and traffic signs to deduce accessibility reasons.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AccessibilityReasonService {

    private final AccessibilityReasonsMapper accessibilityReasonsMapper;

    private final TrafficSignSnapMapper trafficSignSnapMapper;

    private final RoutingAlgorithmFactory routingAlgorithmFactory;

    private final AlgorithmOptionsFactory algorithmOptionsFactory;

    private final PathsToReasonsMapper pathsToReasonsMapper;

    public List<List<AccessibilityReason>> calculateReasons(
            AccessibilityRequest accessibilityRequest,
            NetworkData networkData,
            List<TrafficSign> trafficSigns) {

        log.debug("Calculating accessibility reasons for request: {}", accessibilityRequest);
        Stopwatch stopwatch = Stopwatch.createStarted();

        Snap startSegment = getStartSegment(accessibilityRequest, networkData.networkGraphHopper());
        Snap endSegment = getEndSegment(accessibilityRequest, networkData.networkGraphHopper());

        QueryGraph queryGraph = QueryGraph.create(networkData.networkGraphHopper().getBaseGraph(), startSegment, endSegment);
        Weighting weighting = networkData.networkGraphHopper().createWeighting(NetworkConstants.CAR_PROFILE, new PMap());
        AlgorithmOptions algorithmOptions = algorithmOptionsFactory.createAlgorithmOptions();
        RoutingAlgorithm router = routingAlgorithmFactory.createAlgo(queryGraph, weighting, algorithmOptions);
        List<Path> routes = router.calcPaths(startSegment.getClosestNode(), endSegment.getClosestNode()).stream()
                .filter(Path::isFound)
                .toList();

        if (routes.isEmpty()) {
            log.warn("No routes found for request: {}", accessibilityRequest);
            return List.of();
        }

        List<TrafficSignSnap> trafficSignSnaps = trafficSignSnapMapper.map(trafficSigns, networkData.networkGraphHopper());
        AccessibilityReasons accessibilityReasons = accessibilityReasonsMapper.mapToAoAccessibilityReasons(
                trafficSignSnaps.stream().map(TrafficSignSnap::getTrafficSign).toList());

        var reasons = pathsToReasonsMapper.mapRoutesToReasons(
                routes,
                accessibilityReasons,
                networkData.networkGraphHopper().getEncodingManager());
        log.debug("Calculating accessibility reasons took: {} ms", stopwatch.elapsed().toMillis());

        return reasons;
    }

    private static Snap getEndSegment(AccessibilityRequest accessibilityRequest, NetworkGraphHopper networkGraphHopper) {

        return networkGraphHopper.getLocationIndex()
                .findClosest(
                        accessibilityRequest.endLocationLatitude(),
                        accessibilityRequest.endLocationLongitude(),
                        EdgeFilter.ALL_EDGES);
    }

    private static Snap getStartSegment(AccessibilityRequest accessibilityRequest, NetworkGraphHopper networkGraphHopper) {

        return networkGraphHopper.getLocationIndex()
                .findClosest(
                        accessibilityRequest.startLocationLatitude(),
                        accessibilityRequest.startLocationLongitude(),
                        EdgeFilter.ALL_EDGES);
    }

}
