package nu.ndw.nls.accessibilitymap.accessibility.reason.graphhopper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.Path;
import java.util.List;
import java.util.Map;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReasonGroup;
import nu.ndw.nls.accessibilitymap.accessibility.reason.mapper.RestrictionMapper;
import nu.ndw.nls.accessibilitymap.accessibility.service.debug.AccessibilityDebugger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PathsToReasonsMapperTest {

    private PathsToReasonsMapper pathsToReasonsMapper;

    @Mock
    private RestrictionMapper restrictionMapper;

    @Mock
    private AccessibilityDebugger accessibilityDebugger;

    @Mock
    private Path path;

    @Mock
    private DirectionalSegment directionalSegment;

    @Mock
    private AccessibilityReasonEdgeVisitor accessibilityReasonEdgeVisitor;

    @Mock
    private AccessibilityReason<?> accessibilityReason;

    private MockedStatic<AccessibilityReasonEdgeVisitor> accessibilityReasonEdgeVisitorMockedStatic;

    @BeforeEach
    void setUp() {

        pathsToReasonsMapper = new PathsToReasonsMapper(List.of(restrictionMapper), accessibilityDebugger);
        accessibilityReasonEdgeVisitorMockedStatic = Mockito.mockStatic(AccessibilityReasonEdgeVisitor.class);
    }

    @AfterEach
    void tearDown() {

        accessibilityReasonEdgeVisitorMockedStatic.close();
    }

    @Test
    void mapRoutesToReasons() {

        Map<Integer, DirectionalSegment> directionalSegmentsById = Map.of(1, directionalSegment);
        accessibilityReasonEdgeVisitorMockedStatic.when(
                        () -> AccessibilityReasonEdgeVisitor.create(directionalSegmentsById, List.of(restrictionMapper)))
                .thenReturn(accessibilityReasonEdgeVisitor);
        when(accessibilityReasonEdgeVisitor.getReasons()).thenReturn(List.of(accessibilityReason));
        when(accessibilityReasonEdgeVisitor.getPathFollowed()).thenReturn(List.of(directionalSegment));

        var accessibilityReasonGroups = pathsToReasonsMapper.mapRoutesToReasons(List.of(path), directionalSegmentsById);

        assertThat(accessibilityReasonGroups).hasSize(1);
        AccessibilityReasonGroup accessibilityReasonGroup = accessibilityReasonGroups.getFirst();
        assertThat(accessibilityReasonGroup).containsExactly(accessibilityReason);

        verify(accessibilityDebugger).writeDebug(List.of(directionalSegment));
        verify(path).forEveryEdge(accessibilityReasonEdgeVisitor);
    }
}
