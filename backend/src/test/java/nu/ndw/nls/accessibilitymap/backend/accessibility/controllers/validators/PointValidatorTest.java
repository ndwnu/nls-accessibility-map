package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.validators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import nu.ndw.nls.accessibilitymap.backend.exceptions.IncompleteArgumentsException;
import org.junit.jupiter.api.Test;

class PointValidatorTest {

    private final PointValidator pointValidator = new PointValidator();

    @Test
    void validateConsistentValues_bothNotSet() {
        pointValidator.validateConsistentValues(null, null);
    }

    @Test
    void validateConsistentValues_bothSet() {
        pointValidator.validateConsistentValues(1.0, 1.0);
    }

    @Test
    void validateConsistentValues_fail_latitudeNotSet() {
        IncompleteArgumentsException incompleteArgumentsException = assertThrows(IncompleteArgumentsException.class,
                () -> {
                    pointValidator.validateConsistentValues(null, 1.0);
                });

        assertEquals("When longitude is present, latitude must also be specified", incompleteArgumentsException.getMessage());
    }

    @Test
    void validateConsistentValues_fail_longitudeNotSet() {
        IncompleteArgumentsException incompleteArgumentsException = assertThrows(IncompleteArgumentsException.class,
                () -> {
                    pointValidator.validateConsistentValues(1.0, null);
                });

        assertEquals("When latitude is present, longitude must also be specified", incompleteArgumentsException.getMessage());
    }

}