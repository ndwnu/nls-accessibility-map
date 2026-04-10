package nu.ndw.nls.accessibilitymap.accessibility.cache.locking;

import java.util.UUID;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Getter
@Component
public class LockInfo {

    private final String lockOwnerId;

    public LockInfo() {
        String podUid = System.getenv("POD_UID");
        if (!StringUtils.hasLength(podUid)) {
            podUid = System.getenv("HOSTNAME");
        }
        this.lockOwnerId = podUid + "-" + UUID.randomUUID();
    }
}
