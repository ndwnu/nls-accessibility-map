package nu.ndw.nls.accessibilitymap.accessibility.roadchange.service;

import static org.assertj.core.api.Assertions.assertThat;

import nu.ndw.nls.accessibilitymap.accessibility.cache.Cache;
import nu.ndw.nls.accessibilitymap.accessibility.cache.configuration.CacheConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.roadchange.configuration.RoadChangesCacheConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.roadchange.dto.RoadChanges;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoadChangesCacheWatcherTest {

    @Mock
    private RoadChangesDataService roadChangesDataService;

    @Mock
    private RoadChangesCacheConfiguration roadChangesCacheConfiguration;

    @Test
    void constructor() {

        var roadChangesCacheWatcher = new RoadChangesCacheWatcher(roadChangesCacheConfiguration, roadChangesDataService) {
            @Override
            public CacheConfiguration getCacheConfiguration() {

                return super.getCacheConfiguration();
            }

            @Override
            public Cache<RoadChanges> getCache() {
                return super.getCache();
            }
        };

        assertThat(roadChangesCacheWatcher.getCacheConfiguration()).isEqualTo(roadChangesCacheConfiguration);
        assertThat(roadChangesCacheWatcher.getCache()).isEqualTo(roadChangesDataService);
    }
}
