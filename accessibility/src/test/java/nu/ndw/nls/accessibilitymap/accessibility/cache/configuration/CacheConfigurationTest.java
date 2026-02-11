package nu.ndw.nls.accessibilitymap.accessibility.cache.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import nu.ndw.nls.springboot.test.util.validation.ValidationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("java:S5786")
public class CacheConfigurationTest extends ValidationTest {

    private CacheConfiguration cacheConfiguration;

    @BeforeEach
    void setUp() {
        cacheConfiguration = CacheConfiguration.builder()
                .name("name")
                .folder(Path.of("folder"))
                .fileNameActiveVersion("activeVersion")
                .build();
    }

    @Test
    void initValue() {

        cacheConfiguration = new CacheConfiguration();
        assertThat(cacheConfiguration.isLoadDataOnStartup()).isTrue();
        assertThat(cacheConfiguration.isFailOnCacheReadError()).isTrue();
        assertThat(cacheConfiguration.isWatchForUpdates()).isTrue();
        assertThat(cacheConfiguration.getFileWatcherInterval()).isEqualTo(Duration.ofSeconds(1));
        assertThat(cacheConfiguration.getAcceptableConsequentReadFailures()).isEqualTo(1);
    }

    @Test
    void validate() {

        validate(cacheConfiguration, List.of(), List.of());
    }

    @Test
    void validate_name_null() {

        cacheConfiguration.setName(null);
        validate(
                cacheConfiguration,
                List.of("name"),
                List.of("must not be null"));
    }

    @Test
    void validate_folder_null() {

        cacheConfiguration.setFolder(null);
        validate(
                cacheConfiguration,
                List.of("folder"),
                List.of("must not be null"));
    }

    @Test
    void validate_fileNameActiveVersion_null() {

        cacheConfiguration.setFileNameActiveVersion(null);
        validate(
                cacheConfiguration,
                List.of("fileNameActiveVersion"),
                List.of("must not be empty"));
    }

    @Override
    protected Class<?> getClassToTest() {
        return cacheConfiguration.getClass();
    }
}
