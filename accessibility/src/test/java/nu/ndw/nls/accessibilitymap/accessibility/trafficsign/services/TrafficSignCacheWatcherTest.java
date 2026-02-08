package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services;

import static org.assertj.core.api.Assertions.assertThat;

import nu.ndw.nls.accessibilitymap.accessibility.cache.Cache;
import nu.ndw.nls.accessibilitymap.accessibility.cache.configuration.CacheConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.configuration.TrafficSignCacheConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.dto.TrafficSigns;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrafficSignCacheWatcherTest {

    @Mock
    private TrafficSignCacheConfiguration trafficSignCacheConfiguration;

    @Mock
    private TrafficSignDataService trafficSignDataService;

    @Test
    void constructor() {

        var trafficSignCacheWatcher = new TrafficSignCacheWatcher(trafficSignCacheConfiguration, trafficSignDataService) {
            @Override
            public CacheConfiguration getCacheConfiguration() {

                return super.getCacheConfiguration();
            }

            @Override
            public Cache<TrafficSigns> getCache() {
                return super.getCache();
            }
        };

        assertThat(trafficSignCacheWatcher.getCacheConfiguration()).isEqualTo(trafficSignCacheConfiguration);
        assertThat(trafficSignCacheWatcher.getCache()).isEqualTo(trafficSignDataService);
    }
}
