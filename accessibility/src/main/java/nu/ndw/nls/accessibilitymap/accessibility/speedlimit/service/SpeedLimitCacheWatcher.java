package nu.ndw.nls.accessibilitymap.accessibility.speedlimit.service;

import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.cache.CacheWatcher;
import nu.ndw.nls.accessibilitymap.accessibility.cache.TaskSchedulerFactory;
import nu.ndw.nls.accessibilitymap.accessibility.speedlimit.configuration.SpeedLimitCacheConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.speedlimit.dto.SpeedLimits;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SpeedLimitCacheWatcher extends CacheWatcher<SpeedLimits> {

    public SpeedLimitCacheWatcher(
            SpeedLimitCacheConfiguration speedLimitCacheConfiguration,
            SpeedLimitDataService speedLimitDataService,
            TaskSchedulerFactory taskSchedulerFactory
    ) {

        super(speedLimitCacheConfiguration, speedLimitDataService, taskSchedulerFactory.createTaskScheduler());
    }
}
