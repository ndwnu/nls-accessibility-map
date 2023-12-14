package nu.ndw.nls.accessibilitymap.backend.validators;

import java.util.Objects;
import nu.ndw.nls.accessibilitymap.backend.exceptions.IncompleteArgumentsException;
import org.springframework.stereotype.Component;

@Component
public class PointValidator {

    public void validateConsistentValues(Double latitude, Double longitude) {
        if (bothAreSetOrNotSet(latitude, longitude)) {
            return;
        }

       if (Objects.isNull(latitude)) {
            throw new IncompleteArgumentsException("When longitude is present, latitude must also be specified");
        } else {
            throw new IncompleteArgumentsException("When latitude is present, longitude must also be specified");
        }
    }

    private boolean bothAreSetOrNotSet(Double latitude, Double longitude) {
        return Objects.isNull(latitude) == Objects.isNull(longitude);
    }
}
