package nu.ndw.nls.accessibilitymap.accessibility.service.mapper;

import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction;

/**
 * The AccessibilityRestrictionReducer is an abstract generic class designed for reducing a set of
 * accessibility restrictions into a list of their corresponding accessibility reasons.
 * This class is meant to be extended by specific implementations for different types of
 * accessibility restrictions.
 *
 * @param <TYPE> a specific type that extends the {@link AccessibilityRestriction} class. This type
 *               represents the kind of accessibility restriction handled by the reducer.
 */
public abstract class AccessibilityRestrictionReducer<TYPE extends AccessibilityRestriction> {

    public abstract List<AccessibilityReason> reduceRestrictions(List<TYPE> restrictions);

    public abstract Class<TYPE> getType();
}
