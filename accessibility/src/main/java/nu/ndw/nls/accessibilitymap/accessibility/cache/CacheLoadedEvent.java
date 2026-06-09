package nu.ndw.nls.accessibilitymap.accessibility.cache;

import lombok.Builder;
import lombok.Getter;

@Builder
public class CacheLoadedEvent {

    public enum Type {
        NETWORK_DATA,
        TRAFFIC_SIGNS
    }

    @Getter
    private final Type type;
}
