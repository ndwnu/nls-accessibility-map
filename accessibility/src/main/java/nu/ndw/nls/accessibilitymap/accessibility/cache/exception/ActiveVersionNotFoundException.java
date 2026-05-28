package nu.ndw.nls.accessibilitymap.accessibility.cache.exception;

public class ActiveVersionNotFoundException extends RuntimeException {

    public ActiveVersionNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
