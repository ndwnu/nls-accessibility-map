package nu.ndw.nls.accessibilitymap.accessibility.service.exception;

import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Location;

public class AccessibilityLocationNotFoundException extends RuntimeException {

    public AccessibilityLocationNotFoundException(Location location) {

        super(("Location could not be resolved at %s, %s. Please try a different location that is closer to actual "
               + "road sections in the network.").formatted(location.latitude(), location.longitude()));
    }
}
