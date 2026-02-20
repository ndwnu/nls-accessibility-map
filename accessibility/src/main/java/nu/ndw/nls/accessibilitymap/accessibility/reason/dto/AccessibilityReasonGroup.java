package nu.ndw.nls.accessibilitymap.accessibility.reason.dto;

import java.util.ArrayList;
import java.util.List;

public class AccessibilityReasonGroup extends ArrayList<AccessibilityReason<?>> {

    public AccessibilityReasonGroup(List<AccessibilityReason<?>> accessibilityReasons) {
        super(accessibilityReasons);
    }
}
