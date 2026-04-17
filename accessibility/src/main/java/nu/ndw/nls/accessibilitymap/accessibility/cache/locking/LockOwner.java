package nu.ndw.nls.accessibilitymap.accessibility.cache.locking;

import java.util.UUID;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class LockOwner {

    private final String lockOwnerId;

    public LockOwner() {
        this.lockOwnerId = "nls-accessibility-map-%s".formatted(UUID.randomUUID());
    }
}
