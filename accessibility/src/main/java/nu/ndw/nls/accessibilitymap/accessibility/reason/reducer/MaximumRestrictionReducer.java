package nu.ndw.nls.accessibilitymap.accessibility.reason.reducer;

import java.util.Comparator;
import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.MaximumRestriction;
import org.springframework.stereotype.Component;

/**
 * The MaximumRestrictionReducer class is responsible for reducing a list of {@link MaximumRestriction} instances into a list of
 * corresponding {@link AccessibilityReason} objects. It extends the {@link AccessibilityRestrictionReducer} abstract class and provides
 * specific implementations for handling maximum restrictions.
 * <p>
 * This reducer selects the minimum value among the given restrictions and retrieves the accessibility reason associated with the
 * restriction that has the smallest value.
 */
@Component
public class MaximumRestrictionReducer implements AccessibilityRestrictionReducer<MaximumRestriction> {

    /**
     * Reduces a list of {@link MaximumRestriction} instances into a list of {@link AccessibilityReason} objects.
     * This method identifies the {@link MaximumRestriction} with the smallest value and associates its
     * {@link AccessibilityReason} with the reduced restriction.
     *
     * @param restrictions a list of {@link MaximumRestriction} objects to be reduced into corresponding {@link AccessibilityReason} objects
     *                      with the minimum restriction value. All restrictions must be of the same type.
     * @return a list containing the {@link AccessibilityReason} associated with the restriction that has the smallest value.
     *         The list will have one element or be empty if no minimum restriction is found.
     * @throws IllegalArgumentException if the restrictions are not of the same type.
     */
    @Override
    public List<AccessibilityReason> reduceRestrictions(List<MaximumRestriction> restrictions) {
        ensureAllRestrictionsAreOfSameType(restrictions);
        return restrictions.stream()
                .min(Comparator.comparingDouble(r -> r.getValue().value()))
                .map(r -> r.getAccessibilityReason()
                        .withRestrictions(List.of(r)))
                .stream()
                .toList();
    }

    private void ensureAllRestrictionsAreOfSameType(List<MaximumRestriction> restrictions) {
        int unique = restrictions.stream()
                .map(r -> r.getTypeOfRestriction())
                .distinct()
                .toList()
                .size();
        if (unique != 1) {
            throw new IllegalArgumentException("All restrictions must be of the same type");
        }
    }

    @Override
    public Class<MaximumRestriction> getType() {
        return MaximumRestriction.class;
    }


}
