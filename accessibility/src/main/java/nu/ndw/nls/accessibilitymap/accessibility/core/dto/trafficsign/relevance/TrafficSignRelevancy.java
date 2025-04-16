package nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.relevance;

import java.util.function.BiPredicate;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.AccessibilityRequest;

public interface TrafficSignRelevancy extends BiPredicate<TrafficSign, AccessibilityRequest> {

}
