package nu.ndw.nls.accessibilitymap.accessibility.network;

import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.cache.CacheWatcher;
import nu.ndw.nls.accessibilitymap.accessibility.network.configuration.NetworkCacheConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NetworkCacheWatcher extends CacheWatcher<NetworkData> {

    public NetworkCacheWatcher(
            NetworkCacheConfiguration networkCacheConfiguration,
            NetworkDataService networkDataService) {

        super(networkCacheConfiguration, networkDataService);
    }
}
