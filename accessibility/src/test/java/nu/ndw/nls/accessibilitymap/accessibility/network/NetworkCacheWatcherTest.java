package nu.ndw.nls.accessibilitymap.accessibility.network;

import static org.assertj.core.api.Assertions.assertThat;

import nu.ndw.nls.accessibilitymap.accessibility.cache.Cache;
import nu.ndw.nls.accessibilitymap.accessibility.cache.configuration.CacheConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.network.configuration.NetworkCacheConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NetworkCacheWatcherTest {

    @Mock
    private NetworkCacheConfiguration networkCacheConfiguration;

    @Mock
    private NetworkDataService networkDataService;

    @Test
    void constructor() {

        var networkCacheWatcher = new NetworkCacheWatcher(networkCacheConfiguration, networkDataService) {
            @Override
            public CacheConfiguration getCacheConfiguration() {

                return super.getCacheConfiguration();
            }

            @Override
            public Cache<NetworkData> getCache() {
                return super.getCache();
            }
        };

        assertThat(networkCacheWatcher.getCacheConfiguration()).isEqualTo(networkCacheConfiguration);
        assertThat(networkCacheWatcher.getCache()).isEqualTo(networkDataService);
    }
}
