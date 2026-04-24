package nu.ndw.nls.accessibilitymap.accessibility.cache.locking;

import java.util.UUID;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class LockOwner {

    private final UUID lockOwnerId;

    public LockOwner() {
        this.lockOwnerId = UUID.randomUUID();
    }
}

