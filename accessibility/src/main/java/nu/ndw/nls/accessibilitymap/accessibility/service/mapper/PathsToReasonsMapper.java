package nu.ndw.nls.accessibilitymap.accessibility.service.mapper;

import com.graphhopper.routing.Path;
import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.routing.AccessibilityReasonEdgeVisitor;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityReasons;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
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
 * This class uses the following primary methods: - {@code mapRoutesToReasons}: The main entry point to map a list of {@link Path} to
 * their accessibility reasons. - {@code retrieveAccessibilityReasonsFromEdges}: Processes the edges of a single {@link Path} to determine
 * the associated accessibility reasons.
 */
@Component
public class PathsToReasonsMapper {

    private final EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor;
    private final Map<Class<? extends AccessibilityRestriction>, AccessibilityRestrictionReducer> accessibilityRestrictionReducerMap;

    public PathsToReasonsMapper(EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor,
            List<AccessibilityRestrictionReducer<? extends AccessibilityRestriction>> accessibilityRestrictionReducers) {
        this.edgeIteratorStateReverseExtractor = edgeIteratorStateReverseExtractor;
        this.accessibilityRestrictionReducerMap = accessibilityRestrictionReducers.stream()
                .collect(Collectors.toMap(AccessibilityRestrictionReducer::getType,
                        Function.identity()));
    }

    /**
     * Maps a list of routing paths for their corresponding accessibility reasons. Each path in the provided list is processed to extract
     * accessibility restrictions along the route.
     *
     * @param routes               a list of {@link Path} objects representing the routes to be processed
     * @param accessibilityReasons an {@link AccessibilityReasons} object containing potential accessibility restrictions and reasons that
     *                             can be matched to the routes
     * @param networkGraphHopper   an instance of {@link NetworkGraphHopper} used to retrieve encoding information and facilitate processing
     *                             of path edges
     * @return a list of lists of {@link AccessibilityReason} objects, where each inner list represents the accessibility reasons associated
     * with a specific path
     */
    public List<List<AccessibilityReason>> mapRoutesToReasons(List<Path> routes, AccessibilityReasons accessibilityReasons,
            NetworkGraphHopper networkGraphHopper) {
        return routes.stream()
                .map(path -> retrieveAccessibilityReasonsFromEdges(accessibilityReasons, networkGraphHopper, path))
                .toList();
    }

    /**
     * Retrieves a list of accessibility reasons from the edges of a given path. This method processes each edge of the provided path using
     * an {@code AccessibilityReasonEdgeVisitor} to analyse and collect accessibility reasons based on the associated restrictions.
     *
     * @param accessibilityReasons an {@code AccessibilityReasons} object that contains potential restrictions and reasons associated with
     *                             specific road sections and directions
     * @param networkGraphHopper   an instance of {@code NetworkGraphHopper} to provide encoding information and support edge processing
     * @param path                 the {@code Path} object representing the series of edges to be analysed for accessibility reasons
     * @return a list of {@code AccessibilityReason} objects representing the compiled accessibility reasons extracted from the edges of the
     * provided path
     */
    private List<AccessibilityReason> retrieveAccessibilityReasonsFromEdges(AccessibilityReasons accessibilityReasons,
            NetworkGraphHopper networkGraphHopper, Path path) {
        AccessibilityReasonEdgeVisitor edgeVisitor = AccessibilityReasonEdgeVisitor.of(accessibilityReasons,
                networkGraphHopper.getEncodingManager(), edgeIteratorStateReverseExtractor, accessibilityRestrictionReducerMap);
        path.forEveryEdge(edgeVisitor);
        return edgeVisitor.getAccessibilityReasonList();
    }
}
