package nu.ndw.nls.accessibilitymap.backend.exception;

import nu.ndw.nls.springboot.web.error.exceptions.BadRequestException;

public class IncompleteArgumentsException extends BadRequestException {

    public IncompleteArgumentsException(String s) {
        super(s);
    }
}
