package nu.ndw.nls.accessibilitymap.job.speedlimits;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.speedlimit.SpeedLimit;
import nu.ndw.nls.accessibilitymap.accessibility.network.NetworkDataService;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NwbNetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.speedlimit.dto.SpeedLimits;
import nu.ndw.nls.accessibilitymap.accessibility.speedlimit.service.SpeedLimitDataService;
import nu.ndw.nls.accessibilitymap.job.speedlimits.mapper.SpeedLimitMapper;
import nu.ndw.nls.roadattributesapi.client.feign.generated.api.v1.SpeedLimitsApiClient;
import nu.ndw.nls.roadattributesapi.client.feign.generated.model.v1.RoadSectionDirectionalSpeedLimitJson;
import nu.ndw.nls.roadattributesapi.client.feign.generated.model.v1.RoadSectionSpeedLimitJson;
import nu.ndw.nls.roadattributesapi.client.feign.generated.model.v1.SpeedLimitsRoadSectionResponseJson;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import picocli.CommandLine;

@ExtendWith(MockitoExtension.class)
class RebuildSpeedLimitCacheCommandTest {

    private RebuildSpeedLimitCacheCommand rebuildSpeedLimitCacheCommand;

    @Mock
    private SpeedLimitsApiClient speedLimitsApiClient;

    @Mock
    private SpeedLimitDataService speedLimitDataService;

    @Mock
    private NetworkDataService networkDataService;

    @Mock
    private SpeedLimitMapper speedLimitMapper;

    @Mock
    private NetworkData networkData;

    @Mock
    private NwbNetworkData nwbNetworkData;

    @Mock
    private ResponseEntity<SpeedLimitsRoadSectionResponseJson> speedLimitApiResponse;

    @Mock
    private SpeedLimitsRoadSectionResponseJson speedLimitsRoadSectionResponseJson;

    @Mock
    private RoadSectionSpeedLimitJson roadSectionSpeedLimitJson;

    @Mock
    private RoadSectionDirectionalSpeedLimitJson roadSectionDirectionalSpeedLimitJson1;

    @Mock
    private RoadSectionDirectionalSpeedLimitJson roadSectionDirectionalSpeedLimitJson2;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @BeforeEach
    void setUp() {

        rebuildSpeedLimitCacheCommand = new RebuildSpeedLimitCacheCommand(
                speedLimitsApiClient,
                speedLimitDataService,
                networkDataService,
                speedLimitMapper
        );
    }

    @Test
    void call() {

        LocalDate nwbVersionLocatDate = mockNwbVersion();

        when(roadSectionSpeedLimitJson.getDirectionalSpeedLimit()).thenReturn(List.of(
                roadSectionDirectionalSpeedLimitJson1,
                roadSectionDirectionalSpeedLimitJson2));
        when(speedLimitsApiClient.getSpeedLimits(nwbVersionLocatDate, 0, 1000)).thenReturn(speedLimitApiResponse);
        when(speedLimitsApiClient.getSpeedLimits(nwbVersionLocatDate, 1, 1000)).thenReturn(speedLimitApiResponse);

        when(speedLimitApiResponse.getBody()).thenReturn(speedLimitsRoadSectionResponseJson);

        List<RoadSectionSpeedLimitJson> fullPage = buildRoadSectionSpeedLimitsJson(1000);
        List<RoadSectionSpeedLimitJson> lastPage = buildRoadSectionSpeedLimitsJson(999);
        when(speedLimitsRoadSectionResponseJson.getSpeedLimits())
                .thenReturn(fullPage)
                .thenReturn(lastPage);

        AtomicInteger idCounter = new AtomicInteger(1);
        when(speedLimitMapper.map(roadSectionSpeedLimitJson, roadSectionDirectionalSpeedLimitJson1))
                .thenAnswer(invocation -> new SpeedLimit(idCounter.getAndIncrement(), Direction.FORWARD, 50.0));
        when(speedLimitMapper.map(roadSectionSpeedLimitJson, roadSectionDirectionalSpeedLimitJson2))
                .thenAnswer(invocation -> new SpeedLimit(idCounter.getAndIncrement(), Direction.BACKWARD, 50.0));

        assertThat(new CommandLine(rebuildSpeedLimitCacheCommand).execute()).isZero();

        verify(speedLimitDataService).write(assertArg(speedLimitsSupplier -> {
            SpeedLimits speedLimits = speedLimitsSupplier.get();

            // 2 pages, (1000 + 999) items * 2 directional segments
            assertThat(speedLimits).hasSize(3998);

            assertThat(speedLimits)
                    .extracting(SpeedLimit::roadSectionId)
                    .containsExactlyElementsOf(IntStream.rangeClosed(1, 3998).boxed().toList());

            assertThat(speedLimits)
                    .filteredOn(speedLimit -> speedLimit.direction() == Direction.FORWARD)
                    .hasSize(1999);
            assertThat(speedLimits)
                    .filteredOn(speedLimit -> speedLimit.direction() == Direction.BACKWARD)
                    .hasSize(1999);
        }));
    }

    @Test
    void call_error_generic() {

        LocalDate nwbVersionLocatDate = mockNwbVersion();

        when(roadSectionSpeedLimitJson.getDirectionalSpeedLimit()).thenReturn(List.of(
                roadSectionDirectionalSpeedLimitJson1,
                roadSectionDirectionalSpeedLimitJson2));
        when(speedLimitsApiClient.getSpeedLimits(nwbVersionLocatDate, 0, 1000)).thenReturn(speedLimitApiResponse);
        when(speedLimitsApiClient.getSpeedLimits(nwbVersionLocatDate, 1, 1000)).thenReturn(speedLimitApiResponse);

        when(speedLimitApiResponse.getBody()).thenReturn(speedLimitsRoadSectionResponseJson);

        List<RoadSectionSpeedLimitJson> fullPage = buildRoadSectionSpeedLimitsJson(1000);
        List<RoadSectionSpeedLimitJson> lastPage = buildRoadSectionSpeedLimitsJson(999);
        when(speedLimitsRoadSectionResponseJson.getSpeedLimits())
                .thenReturn(fullPage)
                .thenReturn(lastPage);

        AtomicInteger idCounter = new AtomicInteger(1);
        when(speedLimitMapper.map(roadSectionSpeedLimitJson, roadSectionDirectionalSpeedLimitJson1))
                .thenAnswer(invocation -> new SpeedLimit(idCounter.getAndIncrement(), Direction.FORWARD, 50.0));
        when(speedLimitMapper.map(roadSectionSpeedLimitJson, roadSectionDirectionalSpeedLimitJson2))
                .thenAnswer(invocation -> new SpeedLimit(idCounter.getAndIncrement(), Direction.BACKWARD, 50.0));

        doThrow(new RuntimeException("Test exception")).when(speedLimitDataService).write(any());
        assertThat(new CommandLine(rebuildSpeedLimitCacheCommand).execute()).isEqualTo(1);
        loggerExtension.containsLog(Level.ERROR, "Failed updating speed limits", "Test exception");
    }

    @Test
    void call_error_noResponseBodyFromSpeedLimitsApi() {

        LocalDate nwbVersionLocatDate = mockNwbVersion();

        when(speedLimitsApiClient.getSpeedLimits(nwbVersionLocatDate, 0, 1000)).thenReturn(speedLimitApiResponse);
        when(speedLimitApiResponse.getBody()).thenReturn(null);

        assertThat(new CommandLine(rebuildSpeedLimitCacheCommand).execute()).isEqualTo(1);
        loggerExtension.containsLog(Level.ERROR, "Failed updating speed limits", "No response received from speed limits api");
    }

    private List<RoadSectionSpeedLimitJson> buildRoadSectionSpeedLimitsJson(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> roadSectionSpeedLimitJson)
                .toList();
    }

    private LocalDate mockNwbVersion() {
        when(networkDataService.get()).thenReturn(networkData);
        when(networkData.getNwbNetworkData()).thenReturn(nwbNetworkData);
        when(nwbNetworkData.getNwbVersionId()).thenReturn(19991201);

        return LocalDate.of(1999, 12, 1);
    }
}
