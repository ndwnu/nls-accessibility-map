package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.routing;

import static java.util.stream.Collectors.groupingBy;
import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;

import com.graphhopper.routing.Path.EdgeVisitor;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import com.graphhopper.util.EdgeIteratorState;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityReasons;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction.RestrictionType;
import nu.ndw.nls.accessibilitymap.accessibility.service.mapper.AccessibilityRestrictionReducer;

/**
 * This class implements the {@link EdgeVisitor} interface, enabling the processing of edges in a graph to determine the most restrictive
 * accessibility reasons based on defined accessibility restrictions. It uses a structured approach to classify and analyse restrictions
 * based on their types, compiling a consolidated list of reasons after all edges have been processed.
 * <p>
 * The {@code AccessibilityReasonEdgeVisitor} works with various components, including an encoding manager, a reverse-edge extractor, and a
 * collection of reducers for specific restriction types, to produce a comprehensive analysis of accessibility limitations in a
 * transportation graph.
 * <p>
 * The class operates in three main stages: - During edge iteration, it evaluates accessibility reasons for each road section and direction,
 * categorising them by restriction type. - Upon completion of edge processing, a list of the most restrictive accessibility reasons is
 * compiled. - It relies on reducers to handle specific types of accessibility restrictions to determine the most restrictive reasons from
 * the collected data.
 * <p>
 * This class is designed for private instantiation via a static factory method to ensure proper initialisation of all required
 * dependencies.
 */
@Slf4j
@RequiredArgsConstructor
public class AccessibilityReasonEdgeVisitor implements EdgeVisitor {

    private final AccessibilityReasons accessibilityReasons;

    private final EncodingManager encodingManager;

    private final EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor;

    private final Map<Class<? extends AccessibilityRestriction>, AccessibilityRestrictionReducer> accessibilityRestrictionReducers;

    @Getter
    private List<AccessibilityReason> accessibilityReasonList = new ArrayList<>();

    private final Map<RestrictionType, List<AccessibilityRestriction>> reasonsByRestriction = new HashMap<>();

    /**
     * Processes the next edge in the iteration. For a given edge, check for accessibility restrictions associated with the road section and
     * direction. Merges the restrictions into a structured map grouped by their restriction types.
     *
     * @param edgeIteratorState the current edge being processed
     * @param index             the index of the edge in the iteration
     * @param prevEdgeId        the ID of the previously visited edge
     */
    @Override
    public void next(EdgeIteratorState edgeIteratorState, int index, int prevEdgeId) {

        int linkId = getLinkId(encodingManager, edgeIteratorState);
        Direction direction = edgeIteratorStateReverseExtractor.hasReversed(edgeIteratorState) ? Direction.BACKWARD : Direction.FORWARD;
        if (accessibilityReasons.hasReasons(linkId, direction)) {
            accessibilityReasons.getReasonsByRoadSectionAndDirection(linkId, direction).stream()
                    .flatMap(reasons -> reasons.restrictions().stream())
                    .collect(groupingBy(AccessibilityRestriction::getTypeOfRestriction))
                    .forEach((type, restrictions) -> reasonsByRestriction.merge(type, restrictions, (one, two) -> {
                        one.addAll(two);
                        return one;
                    }));
        }
    }

    /**
     * Finalises the process of analysing accessibility restrictions by computing a list of accessibility reasons based on the gathered
     * restriction data.
     * <p>
     * This method processes the `reasonsByRestriction` map, which groups accessibility restrictions by their restriction types. It uses the
     * {@code getMostRestrictive} method to determine the most restrictive accessibility reasons for each restriction type. The results are
     * then collected into a single list and stored in the {@code accessibilityReasonList} field.
     * <p>
     * This method should be called after all edges have been processed to compile the final list of accessibility reasons.
     */
    @Override
    public void finish() {

        accessibilityReasonList = reasonsByRestriction.entrySet()
                .stream()
                .map(e -> getMostRestrictive(e.getKey(), e.getValue()))
                .flatMap(Collection::stream)
                .toList();

    }

    /**
     * Determines the most restrictive accessibility reasons for a specific restriction type based on a provided list of restrictions.
     * Processes the provided list of restrictions and applies a reduction if an associated reducer is available, otherwise throws an
     * exception if the restriction type is unknown.
     *
     * @param restrictionType    the type of restriction being analysed
     * @param restrictionsByType the list of accessibility restrictions associated with the specified restriction type
     * @return a list of the most restrictive accessibility reasons, reduced and grouped by traffic sign
     * @throws IllegalArgumentException if the restriction type is unknown or unsupported
     */
    private List<AccessibilityReason> getMostRestrictive(
            RestrictionType restrictionType,
            List<AccessibilityRestriction> restrictionsByType) {

        AccessibilityRestriction accessibilityRestriction = restrictionsByType.getFirst();
        if (accessibilityRestrictionReducers.containsKey(accessibilityRestriction.getClass())) {
            return applyRestrictionReduction(restrictionsByType, accessibilityRestriction).stream()
                    .collect(mergeDuplicates()).values()
                    .stream()
                    .toList();
        } else {
            throw new IllegalArgumentException("Unknown restriction type " + restrictionType);
        }
    }

    private static Collector<AccessibilityReason, ?, Map<String, AccessibilityReason>> mergeDuplicates() {

        return Collectors.toMap(
                AccessibilityReason::trafficSignExternalId,
                accessibilityReason -> accessibilityReason.toBuilder().build(),
                (left, right) -> {
                    left.mergeRestrictions(right.restrictions());
                    return left;
                }
        );
    }

    private List<AccessibilityReason> applyRestrictionReduction(
            List<AccessibilityRestriction> restrictionsByType,
            AccessibilityRestriction accessibilityRestriction) {

        return accessibilityRestrictionReducers.get(accessibilityRestriction.getClass())
                .reduceRestrictions(restrictionsByType);
    }

    private int getLinkId(EncodingManager encodingManager, EdgeIteratorState edgeIteratorState) {

        return edgeIteratorState.get(encodingManager.getIntEncodedValue(WAY_ID_KEY));
    }
}
