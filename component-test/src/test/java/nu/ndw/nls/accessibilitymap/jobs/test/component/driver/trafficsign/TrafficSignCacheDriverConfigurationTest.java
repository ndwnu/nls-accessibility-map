package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.trafficsign;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrafficSignCacheDriverConfigurationTest {

    @Test
    void getActiveVersion() {

        var trafficSignCacheDriverConfiguration = TrafficSignCacheDriverConfiguration.builder()
                .locationOnDisk(Path.of("some-path"))
                .fileNameActiveVersion("some-file-name")
                .build();

        assertThat(trafficSignCacheDriverConfiguration.getActiveVersion()).isEqualTo(Path.of("some-path", "some-file-name").toFile());
    }
}