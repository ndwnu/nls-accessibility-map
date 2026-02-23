package nu.ndw.nls.accessibilitymap.accessibility.reason.graphhopper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import com.graphhopper.util.EdgeIteratorState;
import java.util.List;
import java.util.Map;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason.ReasonType;
import nu.ndw.nls.accessibilitymap.accessibility.reason.mapper.RestrictionMapper;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityReasonEdgeVisitorTest {

    private AccessibilityReasonEdgeVisitor accessibilityReasonEdgeVisitor;

    @Mock
    private RestrictionMapper restrictionMapper;

    @Mock
    private DirectionalSegment directionalSegment;

    @Mock
    private EdgeIteratorState edgeIteratorState;

    @Mock
    private Restrictions restrictions;

    @Mock
    private AccessibilityReason<?> accessibilityReason1;

    @Mock
    private AccessibilityReason<?> accessibilityReason2;

    @Mock
    @SuppressWarnings("rawtypes")
    private AccessibilityReason accessibilityReason3;

    @Mock
    private ReasonType reasonType1;

    @Mock
    private ReasonType reasonType2;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @BeforeEach
    void setUp() {

        accessibilityReasonEdgeVisitor = AccessibilityReasonEdgeVisitor.create(Map.of(1, directionalSegment), List.of(restrictionMapper));
    }

    @Test
    @SuppressWarnings("unchecked")
    void visit() {

        when(edgeIteratorState.getEdgeKey()).thenReturn(1);
        when(directionalSegment.getRestrictions()).thenReturn(restrictions);

        when(restrictionMapper.mapRestrictions(restrictions)).thenReturn(List.of(accessibilityReason1, accessibilityReason2));
        when(accessibilityReason1.getReasonType()).thenReturn(reasonType1);
        when(accessibilityReason2.getReasonType()).thenReturn(reasonType1);
        when(accessibilityReason1.reduce(accessibilityReason2)).thenReturn(accessibilityReason3);

        accessibilityReasonEdgeVisitor.next(edgeIteratorState, 0, 0);
        accessibilityReasonEdgeVisitor.finish();

        assertThat(accessibilityReasonEdgeVisitor.getReasons()).containsExactly(accessibilityReason3);
        assertThat(accessibilityReasonEdgeVisitor.getPathFollowed()).containsExactly(directionalSegment);

        loggerExtension.containsLog(Level.DEBUG, "Reduced reason type reasonType1 to accessibilityReason3");
    }

    @Test
    void visit_differentReasonTypes() {

        when(edgeIteratorState.getEdgeKey()).thenReturn(1);
        when(directionalSegment.getRestrictions()).thenReturn(restrictions);

        when(restrictionMapper.mapRestrictions(restrictions)).thenReturn(List.of(accessibilityReason1, accessibilityReason2));
        when(accessibilityReason1.getReasonType()).thenReturn(reasonType1);
        when(accessibilityReason2.getReasonType()).thenReturn(reasonType2);

        accessibilityReasonEdgeVisitor.next(edgeIteratorState, 0, 0);
        accessibilityReasonEdgeVisitor.finish();

        assertThat(accessibilityReasonEdgeVisitor.getReasons()).containsExactlyInAnyOrder(accessibilityReason1, accessibilityReason2);
        assertThat(accessibilityReasonEdgeVisitor.getPathFollowed()).containsExactly(directionalSegment);

        loggerExtension.containsLog(Level.DEBUG, "Reduced reason type reasonType1 to accessibilityReason1");
        loggerExtension.containsLog(Level.DEBUG, "Reduced reason type reasonType2 to accessibilityReason2");
    }
}
