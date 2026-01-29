package nu.ndw.nls.accessibilitymap.accessibility.reason.reducer;

import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityRestriction;

/**
 * An interface for reducing a collection of accessibility restrictions into a consolidated form.
 * The primary purpose of this interface is to process a list of specific types of accessibility restrictions
 * and transform it into a list of corresponding accessibility reasons with reduced or deduplicated restrictions.
 *
 * @param <TYPE> the specific subtype of {@link AccessibilityRestriction} that this reducer handles
 */
public interface AccessibilityRestrictionReducer<TYPE extends AccessibilityRestriction> {

    List<AccessibilityReason> reduceRestrictions(List<TYPE> restrictions);

    Class<TYPE> getType();
}
