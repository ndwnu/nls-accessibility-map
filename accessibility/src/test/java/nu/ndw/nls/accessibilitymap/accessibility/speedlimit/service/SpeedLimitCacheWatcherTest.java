package nu.ndw.nls.accessibilitymap.accessibility.speedlimit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import nu.ndw.nls.accessibilitymap.accessibility.cache.Cache;
import nu.ndw.nls.accessibilitymap.accessibility.cache.TaskSchedulerFactory;
import nu.ndw.nls.accessibilitymap.accessibility.cache.configuration.CacheConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.speedlimit.configuration.SpeedLimitCacheConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.speedlimit.dto.SpeedLimits;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SpeedLimitCacheWatcherTest {

    @Mock
    private SpeedLimitCacheConfiguration speedLimitCacheConfiguration;

    @Mock
    private SpeedLimitDataService speedLimitDataService;

    @Mock
    private TaskSchedulerFactory taskSchedulerFactory;

    @Test
    void constructor() {

        var speedLimitCacheWatcher = new SpeedLimitCacheWatcher(
                speedLimitCacheConfiguration,
                speedLimitDataService,
                taskSchedulerFactory) {
            @Override
            public CacheConfiguration getCacheConfiguration() {

                return super.getCacheConfiguration();
            }

            @Override
            public Cache<SpeedLimits> getCache() {
                return super.getCache();
            }
        };

        assertThat(speedLimitCacheWatcher.getCacheConfiguration()).isEqualTo(speedLimitCacheConfiguration);
        assertThat(speedLimitCacheWatcher.getCache()).isEqualTo(speedLimitDataService);
        verify(taskSchedulerFactory).createTaskScheduler();
    }
}
