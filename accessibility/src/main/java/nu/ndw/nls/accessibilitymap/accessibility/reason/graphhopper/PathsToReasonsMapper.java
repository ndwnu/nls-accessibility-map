package nu.ndw.nls.accessibilitymap.accessibility.reason.graphhopper;

import com.graphhopper.routing.Path;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NwbNetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReasonGroup;
import nu.ndw.nls.accessibilitymap.accessibility.reason.mapper.RestrictionMapper;
import nu.ndw.nls.accessibilitymap.accessibility.service.debug.AccessibilityDebugger;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PathsToReasonsMapper {

    private final List<RestrictionMapper> restrictionMappers;

    private final AccessibilityDebugger accessibilityDebugger;

    public List<AccessibilityReasonGroup> mapRoutesToReasons(
            List<Path> routes,
            Map<Integer, DirectionalSegment> directionalSegmentsById,
            NwbNetworkData nwbNetworkData) {

        return routes.stream()
                .map(path -> new AccessibilityReasonGroup(
                        calculateReasonsForPath(directionalSegmentsById, path, nwbNetworkData)))
                .toList();
    }

    private List<AccessibilityReason<?>> calculateReasonsForPath(
            Map<Integer, DirectionalSegment> directionalSegmentsById,
            Path path,
            NwbNetworkData nwbNetworkData) {

        AccessibilityReasonEdgeVisitor edgeVisitor = AccessibilityReasonEdgeVisitor.create(
                directionalSegmentsById,
                restrictionMappers,
                nwbNetworkData);

        path.forEveryEdge(edgeVisitor);

        accessibilityDebugger.writeDebug(edgeVisitor.getPathFollowed());
        return edgeVisitor.getReasons();
    }
}
