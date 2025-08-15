package nu.ndw.nls.accessibilitymap.accessibility.service.mapper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.FuelTypeRestriction;
import org.springframework.stereotype.Component;

/**
 * The FuelTypeRestrictionReducer class is a concrete implementation of the AccessibilityRestrictionReducer abstract class for
 * FuelTypeRestriction objects. It is responsible for reducing a list of FuelTypeRestriction objects into their corresponding
 * AccessibilityReasons by filtering out duplicates based on their properties. This ensures only unique fuel type restrictions contribute to
 * the accessibility reasons list.
 */
@Component
public class FuelTypeRestrictionReducer extends AccessibilityRestrictionReducer<FuelTypeRestriction> {

    /**
     * Reduces a given list of FuelTypeRestriction objects by filtering out any duplicates based on their properties and returns a
     * corresponding list of AccessibilityReason objects.
     *
     * @param restrictions a list of FuelTypeRestriction objects to be reduced for unique restrictions
     * @return a list of AccessibilityReason objects derived from the unique FuelTypeRestriction objects
     */
    @Override
    public List<AccessibilityReason> reduceRestrictions(List<FuelTypeRestriction> restrictions) {
        Queue<FuelTypeRestriction> queue = new ArrayDeque(restrictions);
        List<FuelTypeRestriction> uniqueFuelTypeRestrictions = new ArrayList<>();
        uniqueFuelTypeRestrictions.add(queue.poll());
        while (!queue.isEmpty()) {
            FuelTypeRestriction fuelTypeRestriction = queue.poll();
            uniqueFuelTypeRestrictions.stream()
                    .filter(f -> !f.isEqual(fuelTypeRestriction))
                    .findFirst()
                    .ifPresent(f -> uniqueFuelTypeRestrictions.add(fuelTypeRestriction));
        }
        return uniqueFuelTypeRestrictions.stream()
                // create a new reason for each unique restriction
                .map(r -> r.getAccessibilityReason()
                        .withRestrictions(List.of(r)))
                .toList();
    }

    @Override
    public Class<FuelTypeRestriction> getType() {
        return FuelTypeRestriction.class;
    }
}
