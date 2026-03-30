package nu.ndw.nls.accessibilitymap.accessibility.roadchange.service;

import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.cache.CacheWatcher;
import nu.ndw.nls.accessibilitymap.accessibility.roadchange.configuration.RoadChangesCacheConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.roadchange.dto.RoadChanges;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RoadChangesCacheWatcher extends CacheWatcher<RoadChanges> {

    public RoadChangesCacheWatcher(
            RoadChangesCacheConfiguration roadChangesCacheConfiguration,
            RoadChangesDataService trafficSignDataService
    ) {
        super(roadChangesCacheConfiguration, trafficSignDataService);
    }
}
