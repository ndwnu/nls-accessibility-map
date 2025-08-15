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
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.factory.AlgorithmOptionsFactory;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.TrafficSignSnap;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityReasons;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.RoutePoints;
import nu.ndw.nls.accessibilitymap.accessibility.service.mapper.AccessibilityReasonsMapper;
import nu.ndw.nls.accessibilitymap.accessibility.service.mapper.PathsToReasonsMapper;
import nu.ndw.nls.accessibilitymap.accessibility.service.mapper.TrafficSignSnapMapper;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.TrafficSignDataService;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.springframework.stereotype.Service;

/**
 * Service class responsible for calculating and retrieving accessibility reasons based on specific accessibility requests.
 * <p>
 * This service uses various components such as traffic sign data, mapping services, and routing algorithms to analyse accessibility-related
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
    private final GraphHopperService graphHopperService;
    private final TrafficSignSnapMapper trafficSignSnapMapper;
    private final TrafficSignDataService trafficSignDataService;
    private final RoutingAlgorithmFactory routingAlgorithmFactory;
    private final AlgorithmOptionsFactory algorithmOptionsFactory;
    private final PathsToReasonsMapper pathsToReasonsMapper;

    /**
     * Calculates accessibility reasons for a given accessibility request.
     * <p>
     * This method identifies and evaluates traffic signs blocking accessibility within the specified route points and generates
     * corresponding accessibility reasons.
     *
     * @param accessibilityRequest the accessibility request containing details such as start location, end location, and filter criteria to
     *                             retrieve blocking traffic signs.
     * @return a list of lists containing identified accessibility reasons. Each list corresponds to a set of reasons associated with a
     * particular section or segment of the route.
     */
    public List<List<AccessibilityReason>> getReasons(AccessibilityRequest accessibilityRequest) {
        log.debug("Calculating accessibility reasons for request: {}", accessibilityRequest);
        Stopwatch stopwatch = Stopwatch.createStarted();
        List<TrafficSign> blockingTrafficSigns = trafficSignDataService.findAllBy(accessibilityRequest);
        log.debug("Getting Traffic signs took: {} ms", stopwatch.elapsed().toMillis());
        stopwatch.reset().start();
        List<List<AccessibilityReason>> reasons = getReasons(blockingTrafficSigns, RoutePoints
                .builder()
                .startLocationLongitude(accessibilityRequest.startLocationLongitude())
                .startLocationLatitude(accessibilityRequest.startLocationLatitude())
                .endLocationLongitude(accessibilityRequest.endLocationLongitude())
                .endLocationLatitude(accessibilityRequest.endLocationLatitude())
                .build());
        log.debug("Calculating accessibility reasons took: {} ms", stopwatch.elapsed().toMillis());
        return reasons;
    }

    private List<List<AccessibilityReason>> getReasons(List<TrafficSign> blockingTrafficSigns, RoutePoints routePoints) {

        NetworkGraphHopper networkGraphHopper = graphHopperService.getNetworkGraphHopper();
        Stopwatch stopwatch = Stopwatch.createStarted();
        List<TrafficSignSnap> trafficSignSnaps = trafficSignSnapMapper.map(blockingTrafficSigns, networkGraphHopper);
        log.debug("Mapping traffic snaps signs took: {} ms", stopwatch.elapsed().toMillis());
        Snap startSegment = getStartSegment(routePoints, networkGraphHopper);
        Snap endSegment = getEndSegment(routePoints, networkGraphHopper);
        QueryGraph queryGraph = QueryGraph.create(networkGraphHopper.getBaseGraph(), startSegment, endSegment);
        stopwatch.reset().start();
        log.debug("Calculating alternative routes");
        stopwatch.reset().start();
        Weighting weighting = queryGraph.wrapWeighting(networkGraphHopper.createWeighting(NetworkConstants.CAR_PROFILE, new PMap()));
        AccessibilityReasons accessibilityReasons = accessibilityReasonsMapper.mapToAoAccessibilityReasons(
                trafficSignSnaps.stream().map(TrafficSignSnap::getTrafficSign).toList());
        AlgorithmOptions algorithmOptions = algorithmOptionsFactory.createAlgorithmOptions();
        RoutingAlgorithm router = routingAlgorithmFactory.createAlgo(queryGraph, weighting,
                algorithmOptions);
        List<Path> routes = router.calcPaths(startSegment.getClosestNode(), endSegment.getClosestNode()).stream()
                .filter(Path::isFound)
                .toList();
        log.debug("Calculating routes took: {} ms", stopwatch.elapsed().toMillis());
        if (routes.isEmpty()) {
            log.warn("No routes found for request: {}", routePoints);
            return List.of();
        }
        return pathsToReasonsMapper.mapRoutesToReasons(routes, accessibilityReasons, networkGraphHopper.getEncodingManager());

    }


    private static Snap getEndSegment(RoutePoints routePoints, NetworkGraphHopper networkGraphHopper) {
        return networkGraphHopper.getLocationIndex()
                .findClosest(
                        routePoints.endLocationLatitude(),
                        routePoints.endLocationLongitude(),
                        EdgeFilter.ALL_EDGES);
    }

    private static Snap getStartSegment(RoutePoints routePoints, NetworkGraphHopper networkGraphHopper) {
        return networkGraphHopper.getLocationIndex()
                .findClosest(
                        routePoints.startLocationLatitude(),
                        routePoints.startLocationLongitude(),
                        EdgeFilter.ALL_EDGES);
    }

}
