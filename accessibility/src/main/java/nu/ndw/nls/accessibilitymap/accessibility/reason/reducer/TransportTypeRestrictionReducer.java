package nu.ndw.nls.accessibilitymap.accessibility.reason.reducer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.TransportTypeRestriction;
import org.springframework.stereotype.Component;

/**
 * A reducer that processes a list of {@link TransportTypeRestriction} objects and consolidates them
 * based on their uniqueness, ultimately extracting a list of their corresponding {@link AccessibilityReason}s.
 *
 * This class extends {@link AccessibilityRestrictionReducer}, specifically handling
 * {@link TransportTypeRestriction} objects. It ensures that duplicate restrictions, as determined
 * by the {@link TransportTypeRestriction#isEqual(AccessibilityRestriction)}, are avoided and only
 * unique restrictions are maintained.
 *
 * Key features:
 * - Filters out duplicate {@link TransportTypeRestriction}s by comparing their values using
 *   {@link TransportTypeRestriction#isEqual(AccessibilityRestriction)}.
 * - Converts the reduced list of restrictions into a list of {@link AccessibilityReason}s.
 * - Provides the type of restriction it works with, which is {@link TransportTypeRestriction}.
 */
@Component
public class TransportTypeRestrictionReducer implements AccessibilityRestrictionReducer<TransportTypeRestriction> {

    /**
     * Processes a list of {@link TransportTypeRestriction} objects, removes duplicates
     * based on the {@link TransportTypeRestriction#isEqual(AccessibilityRestriction)} method,
     * and returns a list of associated {@link AccessibilityReason} objects.
     *
     * The method ensures that only unique transport type restrictions are considered and maps
     * each unique restriction to its corresponding accessibility reason.
     *
     * @param restrictions a list of {@link TransportTypeRestriction} objects to process and reduce,
     *                     potentially containing duplicate restrictions
     * @return a list of {@link AccessibilityReason} objects corresponding to the unique restrictions
     *         in the input list
     */
    @Override
    @SuppressWarnings("java:S3740")
    public List<AccessibilityReason> reduceRestrictions(List<TransportTypeRestriction> restrictions) {
        Queue<TransportTypeRestriction> queue = new ArrayDeque(restrictions);
        List<TransportTypeRestriction> uniqueTransportTypeRestrictions = new ArrayList<>();
        uniqueTransportTypeRestrictions.add(queue.poll());
        while (!queue.isEmpty()) {
            TransportTypeRestriction fuelTypeRestriction = queue.poll();
            uniqueTransportTypeRestrictions.stream()
                    .filter(f -> !f.isEqual(fuelTypeRestriction))
                    .findFirst()
                    .ifPresent(t -> uniqueTransportTypeRestrictions.add(fuelTypeRestriction));
        }
        return uniqueTransportTypeRestrictions.stream()
                // create a new reason for each unique restriction
                .map(r-> r.getAccessibilityReason()
                        .withRestrictions(List.of(r)))

                .toList();
    }

    @Override
    public Class<TransportTypeRestriction> getType() {
        return TransportTypeRestriction.class;
    }
}
