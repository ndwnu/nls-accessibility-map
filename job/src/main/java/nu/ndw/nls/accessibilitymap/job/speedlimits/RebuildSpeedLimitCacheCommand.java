package nu.ndw.nls.accessibilitymap.job.speedlimits;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.speedlimit.SpeedLimit;
import nu.ndw.nls.accessibilitymap.accessibility.network.NetworkDataService;
import nu.ndw.nls.accessibilitymap.accessibility.speedlimit.dto.SpeedLimits;
import nu.ndw.nls.accessibilitymap.accessibility.speedlimit.service.SpeedLimitDataService;
import nu.ndw.nls.accessibilitymap.job.speedlimits.mapper.SpeedLimitMapper;
import nu.ndw.nls.roadattributesapi.client.feign.generated.api.v1.SpeedLimitsApiClient;
import nu.ndw.nls.roadattributesapi.client.feign.generated.model.v1.RoadSectionSpeedLimitJson;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Slf4j
@Component
@Command(name = "rebuildSpeedLimitCache")
@RequiredArgsConstructor
public class RebuildSpeedLimitCacheCommand implements Callable<Integer> {

    public static final int ITEMS_PER_PAGE = 1000;

    private final SpeedLimitsApiClient speedLimitsApiClient;

    private final SpeedLimitDataService speedLimitDataService;

    private final NetworkDataService networkDataService;

    private final SpeedLimitMapper speedLimitMapper;

    @Override
    public Integer call() {

        try {
            log.info("Updating speed limits");

            LocalDate nwbVersion = LocalDate.parse(
                    networkDataService.get().getNwbNetworkData().getNwbVersionId() + "",
                    DateTimeFormatter.ofPattern("yyyyMMdd"));

            SpeedLimits speedLimits = new SpeedLimits();
            loadSpeedLimits(nwbVersion, speedLimits::add);
            log.info("Downloaded {} speed limits", speedLimits.size());

            speedLimitDataService.write(() -> speedLimits);
            return 0;
        } catch (Exception exception) {
            log.error("Failed updating speed limits", exception);
            return 1;
        }
    }

    private void loadSpeedLimits(LocalDate nwbVersion, Consumer<SpeedLimit> speedLimitConsumer) throws IllegalAccessException {

        int currentPage = 0;
        while (true) {
            var speedLimitsResponse = speedLimitsApiClient.getSpeedLimits(nwbVersion, currentPage, ITEMS_PER_PAGE)
                    .getBody();

            if (Objects.isNull(speedLimitsResponse)) {
                throw new IllegalAccessException("No response received from speed limits api");
            }

            List<RoadSectionSpeedLimitJson> speedLimits = speedLimitsResponse.getSpeedLimits();
            speedLimits.forEach(roadSectionSpeedLimitJson ->
                    roadSectionSpeedLimitJson.getDirectionalSpeedLimit().forEach(roadSectionDirectionalSpeedLimitJson ->
                            speedLimitConsumer.accept(
                                    speedLimitMapper.map(
                                            roadSectionSpeedLimitJson,
                                            roadSectionDirectionalSpeedLimitJson))));

            if (speedLimits.size() < ITEMS_PER_PAGE) {
                return;
            } else {
                currentPage++;
            }
        }
    }
}
