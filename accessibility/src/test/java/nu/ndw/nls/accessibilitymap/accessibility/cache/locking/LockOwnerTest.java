package nu.ndw.nls.accessibilitymap.accessibility.cache.locking;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LockOwnerTest {

    LockOwner lockInstance;

    @BeforeEach
    void setup() {
        lockInstance = new LockOwner();
    }

    @Test
    void getLockOwnerId() {
        assertThat(lockInstance.getLockOwnerId()).matches(
                "^nls-accessibility-map-[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$");
    }
}
