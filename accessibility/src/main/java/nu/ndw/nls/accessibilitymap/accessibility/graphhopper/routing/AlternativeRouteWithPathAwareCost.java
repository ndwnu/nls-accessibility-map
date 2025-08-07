/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.routing;

import com.graphhopper.routing.AlternativeRouteCH;
import com.graphhopper.routing.RoutingAlgorithm;
import com.graphhopper.routing.SPTEntry;
import com.graphhopper.storage.RoutingCHEdgeIteratorState;
import com.graphhopper.storage.RoutingCHGraph;
import com.graphhopper.util.PMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.EdgeRestrictions;

/**
 * Implementation of an alternative routing algorithm that incorporates path-aware costs and considers the impact of traffic sign
 * restrictions on routing decisions.
 * <p>
 * This class extends `AlternativeRouteCH` and implements the `RoutingAlgorithm` interface. It modifies route calculation by adapting edge
 * weights based on restrictions associated with traffic signs. The algorithm penalizes weights of edges based on the number of traffic sign
 * restrictions present, ensuring that restricted routes incur higher weights during the shortest path calculation.
 * <p>
 * Key Features: - Extends base alternative routing with support for traffic sign awareness. - Dynamically calculates edge weights using
 * penalties for restricted routes. - Integrates a heuristic combining path-aware restrictions with conventional shortest path
 * computations.
 */
@Slf4j
public class AlternativeRouteWithPathAwareCost extends AlternativeRouteCH implements RoutingAlgorithm {

    private static final double PENALTY_FACTOR = 0.20;
    private static final int PENALTY_UNIT = 1;
    private final EdgeRestrictions edgeRestrictions;

    public AlternativeRouteWithPathAwareCost(RoutingCHGraph graph, PMap hints,
            EdgeRestrictions edgeRestrictions) {
        super(graph, hints);
        this.edgeRestrictions = edgeRestrictions;
    }

    /**
     * Calculates the weight of an edge during routing, considering traffic sign restrictions and penalties.
     * <p>
     * The method retrieves traffic signs associated with the edge, evaluates the number of restrictions, and applies a penalty to the
     * original weight based on the number of restrictions present.
     *
     * @param iter     the current edge iterator state, representing the edge being processed.
     * @param currEdge the shortest-path tree entry of the current edge being evaluated.
     * @param reverse  flag indicating the direction of traversal (true if in reverse).
     * @return the calculated weight of the edge, considering potential penalties for restrictions.
     */
    @Override
    protected double calcWeight(RoutingCHEdgeIteratorState iter, SPTEntry currEdge, boolean reverse) {
        Set<TrafficSignType> trafficSigns = getTrafficSigns(iter, reverse).stream()
                .map(TrafficSign::trafficSignType)
                .collect(Collectors.toSet());

        if (trafficSigns.isEmpty()) {
            return super.calcWeight(iter, currEdge, reverse);
        }

        Integer numberOfRestrictions = calculateNumberOfRestrictions(currEdge, reverse,
                trafficSigns);
        log.debug("Edge {} has restrictions {}", iter.getOrigEdge(), trafficSigns);
        double origWeight = super.calcWeight(iter, currEdge, reverse);
        log.debug("Original weight {}", origWeight);
        log.debug("Number of restrictions {}", numberOfRestrictions);
        double penaltyWeight = origWeight * (PENALTY_UNIT + (numberOfRestrictions * PENALTY_FACTOR));
        log.debug("Penalty weight {}", penaltyWeight);
        return penaltyWeight;

    }

    /**
     * Calculates the number of restrictions by checking the given set of traffic signs against restrictions present on the route.
     *
     * @param currEdge     the current edge in the shortest path tree being processed
     * @param reverse      a flag indicating whether the direction of route processing is reversed
     * @param trafficSigns a set of traffic sign identifiers to be checked
     * @return the number of restrictions that are not found on the route
     */
    private Integer calculateNumberOfRestrictions(SPTEntry currEdge, boolean reverse, Set<TrafficSignType> trafficSigns) {
        AtomicReference<Integer> numberOfRestrictions = new AtomicReference<>(trafficSigns.size());
        Set<TrafficSignType> onRoute = extractRestrictionsFromRoute(currEdge, reverse);
        trafficSigns.forEach(s -> {
            if (onRoute.contains(s)) {
                log.debug("Found same restriction on route {}", s);
                numberOfRestrictions.set(numberOfRestrictions.get() - 1);
            }
        });
        return numberOfRestrictions.get();
    }

    private List<TrafficSign> getTrafficSigns(RoutingCHEdgeIteratorState iter, boolean reverse) {
        int edgeKey = reverse ? iter.getOrigEdgeKeyLast() : iter.getOrigEdgeKeyFirst();
        return edgeRestrictions.getTrafficSignsByEdgeKey().getOrDefault(edgeKey, Collections.emptyList());
    }

    /**
     * Extracts a set of restrictions from a route based on the provided edge and direction. This method traverses the parent edges
     * recursively and collects traffic sign restrictions encountered along the route.
     *
     * @param currEdge The current edge in the route represented as an instance of SPTEntry. It contains information about the edge's
     *                 parent, adjacent node, and edge ID.
     * @param reverse  A boolean flag indicating whether the traversal is in reverse direction or not.
     * @return A set of strings representing the restrictions (e.g., traffic sign types) found in the route.
     */
    private Set<TrafficSignType> extractRestrictionsFromRoute(SPTEntry currEdge, boolean reverse) {
        Set<TrafficSignType> restrictions = new HashSet<>();
        while (currEdge.getParent() != null) {
            currEdge = currEdge.getParent();
            if (currEdge.edge != -1) {
                try {
                    RoutingCHEdgeIteratorState iter = graph.getEdgeIteratorState(currEdge.edge, currEdge.adjNode);

                    Set<TrafficSignType> trafficSigns = getTrafficSigns(iter, reverse).stream()
                            .map(TrafficSign::trafficSignType)
                            .collect(Collectors.toSet());

                    restrictions.addAll(trafficSigns);
                } catch (Exception e) {
                    log.error("Error extracting restrictions from route {}", currEdge.edge, e);
                }
            }

        }

        return restrictions;
    }

}
