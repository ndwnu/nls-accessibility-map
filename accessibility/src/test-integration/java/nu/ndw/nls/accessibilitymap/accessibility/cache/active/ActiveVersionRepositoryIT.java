package nu.ndw.nls.accessibilitymap.accessibility.cache.active;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jooq.test.autoconfigure.JooqTest;
import org.springframework.context.annotation.Import;

@JooqTest
@Import({
        ActiveVersionRepository.class
})
class ActiveVersionRepositoryIT {

    @Autowired
    ActiveVersionRepository repository;

    @Test
    void findActiveVersion() {
        repository.switchActiveVersion("test", "active");

        assertThat(repository.findActiveVersion("test")).contains("active");
    }

    @Test
    void findActiveVersion_notFound() {

        assertThat(repository.findActiveVersion("test")).isEmpty();
    }

    @Test
    void switchActiveVersion_update() {
        repository.switchActiveVersion("test", "active");
        repository.switchActiveVersion("test", "active2");

        assertThat(repository.findActiveVersion("test")).contains("active2");
    }
}
