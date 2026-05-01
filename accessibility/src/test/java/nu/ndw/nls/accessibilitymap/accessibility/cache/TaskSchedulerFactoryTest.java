package nu.ndw.nls.accessibilitymap.accessibility.cache;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

class TaskSchedulerFactoryTest {

    private TaskSchedulerFactory factory;

    @BeforeEach
    void setUp() {
        factory = new TaskSchedulerFactory();
    }

    @Test
    void createTaskScheduler() {
        TaskScheduler taskScheduler = factory.createTaskScheduler();

        assertThat(taskScheduler)
                .isInstanceOf(ThreadPoolTaskScheduler.class)
                .isNotNull();
    }
}
