package nu.ndw.nls.accessibilitymap.accessibility.roadchange.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.SneakyThrows;
import nu.ndw.nls.accessibilitymap.accessibility.roadchange.configuration.RoadChangesCacheConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.roadchange.dto.ChangedNwbRoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.roadchange.dto.RoadChanges;
import nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode;
import nu.ndw.nls.db.nwb.jooq.services.NwbVersionCrudService;
import nu.ndw.nls.springboot.core.time.ClockService;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoadChangesDataServiceTest {

    @Mock
    private NwbVersionCrudService nwbVersionCrudService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ClockService clockService;

    @Mock
    private NwbVersionCrudService nbwVersionCrudService;

    private RoadChangesCacheConfiguration roadChangesCacheConfiguration;

    private RoadChanges roadChanges;

    private Path testDir;

    private RoadChangesDataService roadChangesDataService;

    @SneakyThrows
    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();

        roadChanges = new RoadChanges(1, List.of(new ChangedNwbRoadSection(1, true, true, CarriagewayTypeCode.RB)));

        testDir = Files.createTempDirectory(this.getClass().getSimpleName());
        roadChangesCacheConfiguration = RoadChangesCacheConfiguration.builder()
                .folder(testDir)
                .name("testCache")
                .build();

        roadChangesDataService = new RoadChangesDataService(roadChangesCacheConfiguration,
                clockService,
                nbwVersionCrudService,
                objectMapper);
    }

    @AfterEach
    void tearDown() throws IOException {

        FileUtils.deleteDirectory(testDir.toFile());
    }

    @Test
    void createEmptyCache() {
        when(clockService.now())
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.123-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.433-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        when(nbwVersionCrudService.findLatestVersionId()).thenReturn(200);

        roadChangesDataService.createEmptyCache();

        assertThat(roadChangesDataService.get()).usingRecursiveComparison()
                .isEqualTo(new RoadChanges(200, List.of()));
    }

    @Test
    void write_no_data_in_cache() {
        when(clockService.now())
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.123-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.433-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        roadChangesDataService.write(roadChanges);
        RoadChanges writtenData = roadChangesDataService.get();
        assertThat(writtenData).isEqualTo(roadChanges);
    }

    @Test
    void write_previous_data_in_cache_same_version() {
        when(clockService.now())
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.123-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.433-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        roadChangesDataService.write(roadChanges);
        RoadChanges newRoadChanges = new RoadChanges(1, List.of(new ChangedNwbRoadSection(2, true, true, CarriagewayTypeCode.RB)));
        roadChangesDataService.write(newRoadChanges);

        RoadChanges writtenData = roadChangesDataService.get();
        assertThat(writtenData).usingRecursiveComparison()
                .isEqualTo(roadChanges.merge(newRoadChanges));
    }

    @Test
    void write_previous_data_in_cache_different_version() {
        when(clockService.now())
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.123-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:03:01.433-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        roadChangesDataService.write(roadChanges);
        RoadChanges newRoadChanges = new RoadChanges(2, List.of(new ChangedNwbRoadSection(2, true, true, CarriagewayTypeCode.RB)));
        roadChangesDataService.write(newRoadChanges);

        RoadChanges writtenData = roadChangesDataService.get();
        assertThat(writtenData)
                .isEqualTo(newRoadChanges);
    }
}
