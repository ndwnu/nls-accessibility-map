package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.List;
import nu.ndw.nls.springboot.test.util.validation.ValidationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrafficSignCacheConfigurationTest extends ValidationTest {

    private TrafficSignCacheConfiguration trafficSignCacheConfiguration;

    @BeforeEach
    void setUp() {
        trafficSignCacheConfiguration = TrafficSignCacheConfiguration.builder()
                .folder(Path.of("folder"))
                .fileNameActiveVersion("activeVersion")
                .build();
    }

    @Test
    void initValue() {
        trafficSignCacheConfiguration = new TrafficSignCacheConfiguration();
        assertThat(trafficSignCacheConfiguration.isFailOnNoDataOnStartup()).isTrue();
    }

    @Test
    void validate() {

        validate(trafficSignCacheConfiguration, List.of(), List.of());
    }

    @Test
    void validate_folder_null() {

        trafficSignCacheConfiguration.setFolder(null);
        validate(trafficSignCacheConfiguration,
                List.of("folder"),
                List.of("must not be null"));
    }

    @Test
    void validate_fileNameActiveVersion_null() {

        trafficSignCacheConfiguration.setFileNameActiveVersion(null);
        validate(trafficSignCacheConfiguration,
                List.of("fileNameActiveVersion"),
                List.of("must not be empty"));
    }

    @Override
    protected Class<?> getClassToTest() {
        return TrafficSignCacheConfiguration.class;
    }
}