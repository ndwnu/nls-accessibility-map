package nu.ndw.nls.accessibilitymap.accessibility.reason.dto;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

public class AccessibilityReasonGroup extends ArrayList<AccessibilityReason<?>> {

    @Serial
    private static final long serialVersionUID = 1L;

    public AccessibilityReasonGroup(List<AccessibilityReason<?>> accessibilityReasons) {
        super(accessibilityReasons);
    }
}
