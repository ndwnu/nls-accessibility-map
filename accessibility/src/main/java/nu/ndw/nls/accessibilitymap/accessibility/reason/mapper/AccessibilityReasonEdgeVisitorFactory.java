package nu.ndw.nls.accessibilitymap.accessibility.reason.mapper;

import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import java.util.Map;
import nu.ndw.nls.accessibilitymap.accessibility.reason.reducer.AccessibilityRestrictionReducer;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityReasons;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction;
import org.springframework.stereotype.Component;

@Component
public class AccessibilityReasonEdgeVisitorFactory {

    @SuppressWarnings({"java:S3740", "java:S6411"})
    public AccessibilityReasonEdgeVisitor create(
            AccessibilityReasons accessibilityReasons,
            EncodingManager encodingManager,
            EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor,
            Map<Class<? extends AccessibilityRestriction>, AccessibilityRestrictionReducer> accessibilityRestrictionReducers) {

        return new AccessibilityReasonEdgeVisitor(
                accessibilityReasons,
                encodingManager,
                edgeIteratorStateReverseExtractor,
                accessibilityRestrictionReducers);
    }
}
