package nu.ndw.nls.accessibilitymap.accessibility.reason.graphhopper;

import static org.assertj.core.api.Assertions.assertThat;

import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import java.util.Map;
import nu.ndw.nls.accessibilitymap.accessibility.reason.reducer.AccessibilityRestrictionReducer;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityReasons;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityReasonEdgeVisitorFactoryTest {

    private AccessibilityReasonEdgeVisitorFactory accessibilityReasonEdgeVisitorFactory;

    @Mock
    private AccessibilityReasons accessibilityReasons;

    @Mock
    private EncodingManager encodingManager;

    @Mock
    private EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor;

    @Mock
    private Map<Class<? extends AccessibilityRestriction>, AccessibilityRestrictionReducer> accessibilityRestrictionReducers;

    @BeforeEach
    void setUp() {

        accessibilityReasonEdgeVisitorFactory = new AccessibilityReasonEdgeVisitorFactory();
    }

    @Test
    void create() {

        var accessibilityReasonEdgeVisitor = accessibilityReasonEdgeVisitorFactory.create(
                accessibilityReasons,
                encodingManager,
                edgeIteratorStateReverseExtractor,
                accessibilityRestrictionReducers);

        assertThat(accessibilityReasonEdgeVisitor).isNotNull();
    }

}