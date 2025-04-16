package nu.ndw.nls.accessibilitymap.backend.exceptions;

public class ResourceNotFoundException extends IllegalArgumentException {

    public ResourceNotFoundException(String s) {
        super(s);
    }
}
