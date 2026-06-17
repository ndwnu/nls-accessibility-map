package nu.ndw.nls.accessibilitymap.backend.exception;

import nu.ndw.nls.springboot.web.error.exceptions.ResourceNotFoundException;

public class MunicipalityNotFoundException extends ResourceNotFoundException {

    public MunicipalityNotFoundException(String message) {
        super(message);
    }
}
