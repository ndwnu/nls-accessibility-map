package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.service;

import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.cache.CacheWatcher;
import nu.ndw.nls.accessibilitymap.accessibility.cache.TaskSchedulerFactory;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.configuration.TrafficSignCacheConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.dto.TrafficSigns;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TrafficSignCacheWatcher extends CacheWatcher<TrafficSigns> {

    public TrafficSignCacheWatcher(
            TrafficSignCacheConfiguration trafficSignCacheConfiguration,
            TrafficSignDataService trafficSignDataService,
            TaskSchedulerFactory taskSchedulerFactory,
            RetryTemplate cacheReadRetryTemplate
    ) {

        super(trafficSignCacheConfiguration, trafficSignDataService, cacheReadRetryTemplate, taskSchedulerFactory.createTaskScheduler());
    }
}
