package nu.ndw.nls.accessibilitymap.accessibility.reason.graphhopper;

import com.graphhopper.routing.Path;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReasons;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.reason.reducer.AccessibilityRestrictionReducer;
import org.springframework.stereotype.Component;

/**
 * The PathsToReasonsMapper class maps a list of routing paths to the corresponding reasons for accessibility restrictions along those
 * routes.
 * <p>
 * This class relies on the use of {@link EdgeIteratorStateReverseExtractor} to extract properties from edges within the paths and a map of
 * {@link AccessibilityRestrictionReducer} instances to process specific types of accessibility restrictions.
 * <p>
 * It is designed to work with a list of {@link Path} objects, producing a nested list of {@link AccessibilityReason} objects that represent
 * the restrictions associated with each path.
 * <p>
 * This class uses the following primary methods: - {@code mapRoutesToReasons}: The main entry point to map a list of {@link Path} to their
 * accessibility reasons. - {@code retrieveAccessibilityReasonsFromEdges}: Processes the edges of a single {@link Path} to determine the
 * associated accessibility reasons.
 */
@Component
public class PathsToReasonsMapper {

    private final EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor;

    private final AccessibilityReasonEdgeVisitorFactory accessibilityReasonEdgeVisitorFactory;

    @Getter(AccessLevel.PROTECTED)
    @SuppressWarnings({"java:S3740", "java:S6411"})
    private final Map<Class<? extends AccessibilityRestriction>, AccessibilityRestrictionReducer> accessibilityRestrictionReducerMap;

    public PathsToReasonsMapper(
            EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor,
            AccessibilityReasonEdgeVisitorFactory accessibilityReasonEdgeVisitorFactory,
            List<AccessibilityRestrictionReducer<? extends AccessibilityRestriction<?>>> accessibilityRestrictionReducers) {

        this.edgeIteratorStateReverseExtractor = edgeIteratorStateReverseExtractor;
        this.accessibilityReasonEdgeVisitorFactory = accessibilityReasonEdgeVisitorFactory;
        this.accessibilityRestrictionReducerMap = accessibilityRestrictionReducers.stream()
                .collect(Collectors.toMap(AccessibilityRestrictionReducer::getType,
                        Function.identity()));
    }

    /**
     * Maps a list of routes to their respective accessibility reasons. For each route in the input list, this method evaluates and
     * retrieves the accessibility reasons based on the edges in the route, using the provided {@link AccessibilityReasons} and
     * {@link EncodingManager}.
     *
     * @param routes               a list of {@link Path} objects representing the routes to be evaluated for accessibility reasons
     * @param accessibilityReasons an {@link AccessibilityReasons} object containing potential accessibility reasons and restrictions
     *                             relevant to the routes being processed
     * @param encodingManager      an {@link EncodingManager} instance used for retrieving encoded values from the edges in the routes
     * @return a list of lists of {@link AccessibilityReason} objects, where each inner list corresponds to the accessibility reasons
     * gathered for each route
     */
    public List<List<AccessibilityReason>> mapRoutesToReasons(
            List<Path> routes,
            AccessibilityReasons accessibilityReasons,
            EncodingManager encodingManager) {

        return routes.stream()
                .map(path -> retrieveAccessibilityReasonsFromEdges(accessibilityReasons, encodingManager, path))
                .toList();
    }

    /**
     * Retrieves a list of accessibility reasons from the edges of a given path. This method processes each edge in the path, evaluating
     * accessibility restrictions and reasons based on the provided {@link AccessibilityReasons} and {@link EncodingManager}. The evaluation
     * is performed using an instance of {@link AccessibilityReasonEdgeVisitor}, which aggregates a list of accessibility reasons from the
     * path's edges.
     *
     * @param accessibilityReasons an {@link AccessibilityReasons} object containing potential accessibility reasons and restrictions
     *                             relevant to the path being processed
     * @param encodingManager      an {@link EncodingManager} instance used for retrieving encoded values from edges in the graph
     * @param path                 a {@link Path} object representing the route whose edges will be evaluated for accessibility reasons
     * @return a list of {@link AccessibilityReason} objects representing accessibility restrictions and reasons aggregated from the path's
     * edges
     */
    private List<AccessibilityReason> retrieveAccessibilityReasonsFromEdges(
            AccessibilityReasons accessibilityReasons,
            EncodingManager encodingManager,
            Path path) {

        AccessibilityReasonEdgeVisitor edgeVisitor = accessibilityReasonEdgeVisitorFactory.create(
                accessibilityReasons,
                encodingManager,
                edgeIteratorStateReverseExtractor,
                accessibilityRestrictionReducerMap);
        path.forEveryEdge(edgeVisitor);
        return edgeVisitor.getAccessibilityReasonList();
    }
}
