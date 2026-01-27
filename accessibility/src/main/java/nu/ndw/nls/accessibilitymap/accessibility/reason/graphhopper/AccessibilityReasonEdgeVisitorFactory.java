package nu.ndw.nls.accessibilitymap.accessibility.reason.graphhopper;

import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import java.util.Map;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReasons;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.reason.reducer.AccessibilityRestrictionReducer;
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
