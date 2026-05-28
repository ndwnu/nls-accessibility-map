package nu.ndw.nls.accessibilitymap.accessibility.cache.exception;

public class ActiveVersionNotFoundException extends RuntimeException {

    public ActiveVersionNotFoundException(String cacheName) {
        super("No active version found for cache %s".formatted(cacheName));
    }
}
