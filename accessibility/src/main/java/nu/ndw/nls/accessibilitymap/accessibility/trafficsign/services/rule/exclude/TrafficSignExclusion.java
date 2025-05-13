package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.rule.exclude;

import java.util.function.BiPredicate;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityRequest;

public interface TrafficSignExclusion extends BiPredicate<TrafficSign, AccessibilityRequest> {

}
