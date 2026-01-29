package nu.ndw.nls.accessibilitymap.accessibility.reason.graphhopper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.Path;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import java.util.List;
import java.util.Map;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReasons;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.reason.reducer.AccessibilityRestrictionReducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PathsToReasonsMapperTest {

    private PathsToReasonsMapper pathsToReasonsMapper;

    @Mock
    private EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor;

    @Mock
    private AccessibilityReasons accessibilityReasons;

    @Mock
    private EncodingManager encodingManager;

    @Mock
    private AccessibilityRestrictionReducer accessibilityRestrictionReducer;


    @Mock
    private AccessibilityReasonEdgeVisitorFactory accessibilityReasonEdgeVisitorFactory;

    @Mock
    private AccessibilityReasonEdgeVisitor accessibilityReasonEdgeVisitor;

    @Mock
    private Path path;

    @Mock
    private List<AccessibilityReason> accessibilityReasonsList;

    private List<AccessibilityRestrictionReducer<? extends AccessibilityRestriction<?>>> accessibilityRestrictionReducers;


    @BeforeEach
    void setUp() {

        when(accessibilityRestrictionReducer.getType()).thenReturn(AccessibilityRestriction.class);
        accessibilityRestrictionReducers = List.of(accessibilityRestrictionReducer);
        pathsToReasonsMapper = new PathsToReasonsMapper(
                edgeIteratorStateReverseExtractor,
                accessibilityReasonEdgeVisitorFactory,
                accessibilityRestrictionReducers);
        assertThat(pathsToReasonsMapper.getAccessibilityRestrictionReducerMap())
                .isEqualTo(Map.of(AccessibilityRestriction.class, accessibilityRestrictionReducer));
    }

    @Test
    void mapRoutesToReasons() {

        when(accessibilityReasonEdgeVisitorFactory.create(
                accessibilityReasons,
                encodingManager,
                edgeIteratorStateReverseExtractor,
                pathsToReasonsMapper.getAccessibilityRestrictionReducerMap()))
                .thenReturn(accessibilityReasonEdgeVisitor);
        when(accessibilityReasonEdgeVisitor.getAccessibilityReasonList()).thenReturn(accessibilityReasonsList);

        List<List<AccessibilityReason>> result = pathsToReasonsMapper.mapRoutesToReasons(
                List.of(path),
                accessibilityReasons,
                encodingManager);

        assertThat(result).containsExactly(accessibilityReasonsList);

        verify(path).forEveryEdge(accessibilityReasonEdgeVisitor);
    }
}
