package nu.ndw.nls.accessibilitymap.accessibility.service.mapper;

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
public class MaximumRestrictionReducer extends AccessibilityRestrictionReducer<MaximumRestriction> {

    /**
     * Reduces a list of {@link MaximumRestriction} objects into a list of {@link AccessibilityReason} objects, selecting the accessibility
     * reason associated with the restriction that has the minimum value.
     *
     * @param restrictions a list of {@link MaximumRestriction} instances, representing various maximum restrictions to be reduced.
     * @return a list of {@link AccessibilityReason} objects corresponding to the {@link MaximumRestriction} with the smallest value.
     */
    @Override
    public List<AccessibilityReason> reduceRestrictions(List<MaximumRestriction> restrictions) {

        return restrictions.stream()
                .min(Comparator.comparingDouble(r -> r.getValue().value()))
                .map(r -> r.getAccessibilityReason()
                        .withRestrictions(List.of(r)))
                .stream()
                .toList();
    }

    @Override
    public Class<MaximumRestriction> getType() {
        return MaximumRestriction.class;
    }


}
