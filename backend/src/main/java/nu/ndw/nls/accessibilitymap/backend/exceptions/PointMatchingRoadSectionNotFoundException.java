package nu.ndw.nls.accessibilitymap.backend.exceptions;

import nu.ndw.nls.accessibilitymap.accessibility.exceptions.ResourceNotFoundException;

public class PointMatchingRoadSectionNotFoundException extends ResourceNotFoundException {

    public PointMatchingRoadSectionNotFoundException(String s) {
        super(s);
    }
}
