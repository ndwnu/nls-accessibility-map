package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.rule.restrictive;

import java.util.function.BiPredicate;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.AccessibilityRequest;

public interface TrafficSignRestriction extends BiPredicate<TrafficSign, AccessibilityRequest> {

}
