package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services;

import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.cache.CacheWatcher;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.configuration.TrafficSignCacheConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.dto.TrafficSigns;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TrafficSignCacheWatcher extends CacheWatcher<TrafficSigns> {

    public TrafficSignCacheWatcher(
            TrafficSignCacheConfiguration trafficSignCacheConfiguration,
            TrafficSignDataService trafficSignDataService) {

        super(trafficSignCacheConfiguration, trafficSignDataService);
    }
}
