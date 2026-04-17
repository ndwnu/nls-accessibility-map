package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.cache.locking.DistributedLockService;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.json.JsonWriter;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.configuration.TrafficSignCacheConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.dto.TrafficSigns;
import nu.ndw.nls.springboot.core.time.ClockService;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrafficSignDataServiceTest {

    private TrafficSignDataService trafficSignDataService;

    @Mock
    private ClockService clockService;

    @Mock
    private DistributedLockService distributedLockService;

    @Mock
    private JsonWriter jsonWriter;

    private TrafficSign trafficSign1;

    private TrafficSign trafficSign2;

    private Path testDir;

    private TrafficSignCacheConfiguration trafficSignCacheConfiguration;

    @BeforeEach
    void setUp() throws IOException {

        trafficSign1 = TrafficSign.builder().id(1).build();
        trafficSign2 = TrafficSign.builder().id(2).build();

        testDir = Files.createTempDirectory(this.getClass().getSimpleName());

        trafficSignCacheConfiguration = TrafficSignCacheConfiguration.builder()
                .name("testCache")
                .folder(testDir.resolve("testFolder"))
                .build();

        trafficSignDataService = new TrafficSignDataService(trafficSignCacheConfiguration,
                clockService,
                distributedLockService,
                new ObjectMapper(), jsonWriter);
    }

    @AfterEach
    void tearDown() throws IOException {

        FileUtils.deleteDirectory(testDir.toFile());
    }

    @Test
    void findAll() {
        when(clockService.now())
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.123-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.433-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        trafficSignDataService.write(new TrafficSigns(trafficSign1, trafficSign2));

        Set<TrafficSign> trafficSigns = trafficSignDataService.findAll();
        assertThat(trafficSigns).containsExactlyInAnyOrder(trafficSign1, trafficSign2);
    }

    @Test
    void readWrite() {

        when(clockService.now())
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.123-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.433-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        trafficSignDataService.write(new TrafficSigns(trafficSign1, trafficSign2));
        trafficSignDataService.read();

        Set<TrafficSign> trafficSigns = trafficSignDataService.findAll();
        assertThat(trafficSigns).containsExactlyInAnyOrder(trafficSign1, trafficSign2);
    }

    @Test
    void dataExists() throws IOException {

        Files.createDirectories(trafficSignCacheConfiguration.getActiveVersion().toPath());

        assertThat(trafficSignDataService.dataExists()).isTrue();
    }

    @Test
    void dataExists_false() {

        assertThat(trafficSignDataService.dataExists()).isFalse();
    }
}
