package nu.ndw.nls.accessibilitymap.accessibility.speedlimit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.cache.active.ActiveVersionRepository;
import nu.ndw.nls.accessibilitymap.accessibility.cache.locking.DistributedLockService;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.speedlimit.SpeedLimit;
import nu.ndw.nls.accessibilitymap.accessibility.json.JsonWriter;
import nu.ndw.nls.accessibilitymap.accessibility.speedlimit.configuration.SpeedLimitCacheConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.speedlimit.dto.SpeedLimits;
import nu.ndw.nls.springboot.core.time.ClockService;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.retry.RetryTemplate;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class SpeedLimitDataServiceTest {

    private SpeedLimitDataService speedLimitDataService;

    @Mock
    private ClockService clockService;

    @Mock
    private DistributedLockService distributedLockService;

    @Mock
    private ActiveVersionRepository activeVersionRepository;

    @Mock
    private JsonWriter jsonWriter;

    @Mock
    private RetryTemplate retryTemplate;

    @Mock
    private SpeedLimit speedLimit1;

    @Mock
    private SpeedLimit speedLimit2;

    private Path testDir;

    private SpeedLimitCacheConfiguration speedLimitCacheConfiguration;

    @BeforeEach
    void setUp() throws IOException {

        testDir = Files.createTempDirectory(this.getClass().getSimpleName());

        speedLimitCacheConfiguration = SpeedLimitCacheConfiguration.builder()
                .name("testCache")
                .folder(testDir.resolve("testFolder"))
                .build();

        speedLimitDataService = new SpeedLimitDataService(
                speedLimitCacheConfiguration,
                clockService,
                distributedLockService,
                new JsonMapper(),
                jsonWriter,
                activeVersionRepository,
                retryTemplate

        );
    }

    @AfterEach
    void tearDown() throws IOException {

        FileUtils.deleteDirectory(testDir.toFile());
    }

    @Test
    void readWrite() {

        when(clockService.now())
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.123-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.433-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        when(activeVersionRepository.findActiveVersion(speedLimitCacheConfiguration.getName()))
                .thenReturn(Optional.of("2022-03-11T09:03:01.123-01:00"));
        speedLimitDataService.write(() -> new SpeedLimits(speedLimit1, speedLimit2));
        speedLimitDataService.read();

        Set<SpeedLimit> speedLimits = speedLimitDataService.findAll();
        assertThat(speedLimits).containsExactlyInAnyOrder(speedLimit1, speedLimit2);
    }

    @Test
    void dataExists() throws IOException {
        when(activeVersionRepository.findActiveVersion(speedLimitCacheConfiguration.getName()))
                .thenReturn(Optional.of("2022-03-11T09:03:01.123-01:00"));
        Files.createDirectories(speedLimitCacheConfiguration.getFolder().resolve("2022-03-11T09:03:01.123-01:00"));

        assertThat(speedLimitDataService.dataExists()).isTrue();
    }

    @Test
    void dataExists_false() {

        assertThat(speedLimitDataService.dataExists()).isFalse();
    }

    @Test
    void findAll() {
        when(clockService.now())
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.123-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.433-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        when(activeVersionRepository.findActiveVersion(speedLimitCacheConfiguration.getName())).thenReturn(Optional.of(
                "2022-03-11T09:03:01.433-01:00"));
        speedLimitDataService.write(() -> new SpeedLimits(speedLimit1, speedLimit2));

        Set<SpeedLimit> speedLimits = speedLimitDataService.findAll();
        assertThat(speedLimits).containsExactlyInAnyOrder(speedLimit1, speedLimit2);
    }
}
