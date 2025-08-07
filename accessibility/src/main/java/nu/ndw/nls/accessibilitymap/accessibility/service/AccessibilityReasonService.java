package nu.ndw.nls.accessibilitymap.accessibility.service;

import com.google.common.base.Stopwatch;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.querygraph.QueryRoutingCHGraph;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.PMap;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.EdgeRestrictions;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.querygraph.QueryGraphConfigurer;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.routing.AccessibilityReasonEdgeVisitor;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.routing.AlternativeRouteWithPathAwareCost;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.TrafficSignSnap;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityReasons;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.RoutePoints;
import nu.ndw.nls.accessibilitymap.accessibility.service.mapper.AccessibilityReasonsMapper;
import nu.ndw.nls.accessibilitymap.accessibility.service.mapper.TrafficSignSnapMapper;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.TrafficSignDataService;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccessibilityReasonService {

    private final AccessibilityReasonsMapper accessibilityReasonsMapper;
    private final GraphHopperService graphHopperService;
    private final TrafficSignSnapMapper trafficSignSnapMapper;
    private final QueryGraphConfigurer queryGraphConfigurer;
    private final EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor;
    private final TrafficSignDataService trafficSignDataService;

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

        List<Snap> snapList = new ArrayList<>();
        Snap startSegment = getStartSegment(routePoints, networkGraphHopper);
        Snap endSegment = getEndSegment(routePoints, networkGraphHopper);
        snapList.add(startSegment);
        snapList.add(endSegment);
        snapList.addAll(trafficSignSnaps.stream().map(TrafficSignSnap::getSnap).toList());
        QueryGraph queryGraph = QueryGraph.create(networkGraphHopper.getBaseGraph(), snapList);
        EdgeRestrictions edgeRestrictions = queryGraphConfigurer.createEdgeRestrictions(queryGraph, networkGraphHopper.getEncodingManager(),
                trafficSignSnaps);
        stopwatch.reset().start();
        log.debug("Calculating alternative routes");
        stopwatch.reset().start();
        final PMap pMap = createRoutingConfiguration();
        AlternativeRouteWithPathAwareCost router = new AlternativeRouteWithPathAwareCost(
                new QueryRoutingCHGraph(graphHopperService.getCHGraph(), queryGraph),
                pMap, edgeRestrictions);
        List<Path> alternatives = router.calcPaths(startSegment.getClosestNode(), endSegment.getClosestNode());
        AccessibilityReasons accessibilityReasons = accessibilityReasonsMapper.mapToAoAccessibilityReasons(blockingTrafficSigns);
        log.debug("Calculating alternative routes took: {} ms", stopwatch.elapsed().toMillis());
        return alternatives.stream()
                .map(path -> {
                    AccessibilityReasonEdgeVisitor edgeVisitor = new AccessibilityReasonEdgeVisitor(accessibilityReasons,
                            networkGraphHopper.getEncodingManager(), edgeIteratorStateReverseExtractor);
                    path.forEveryEdge(edgeVisitor);
                    return edgeVisitor.getAccessibilityReasonList();
                }).toList();

    }


    private static PMap createRoutingConfiguration() {
        PMap pMap = new PMap();
        pMap.putObject("alternative_route.max_weight_factor", 4.0);
        pMap.putObject("alternative_route.max_share_factor", 0.25);
        pMap.putObject("alternative_route.local_optimality_factor", 0.25);
        pMap.putObject("alternative_route.max_paths", 100);
        return pMap;
    }

//    private boolean routeIsAccessible(NetworkGraphHopper networkGraphHopper, QueryGraph queryGraph, Set<Integer> blockedEdges,
//            Snap startSegment, Snap endSegment) {
//
//        Weighting weighting = buildWeightingWithRestrictions(networkGraphHopper, blockedEdges);
//        Dijkstra dijkstra = new Dijkstra(
//                queryGraph,
//                weighting,
//                TraversalMode.EDGE_BASED);
//        Path route = dijkstra.calcPath(startSegment.getClosestNode(), endSegment.getClosestNode());
//        return route.isFound();
//    }

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
