package nu.ndw.nls.accessibilitymap.job.cache.command;

import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.network.NetworkDataService;
import nu.ndw.nls.accessibilitymap.accessibility.speedlimit.service.SpeedLimitDataService;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.service.TrafficSignDataService;
import nu.ndw.nls.accessibilitymap.job.speedlimits.RebuildSpeedLimitCacheCommand;
import nu.ndw.nls.accessibilitymap.job.trafficsign.command.RebuildTrafficSignCacheCommand;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Slf4j
@Component
@Command(name = "initializeCache")
@RequiredArgsConstructor
public class InitializeCacheCommand implements Callable<Integer> {

    private final NetworkDataService networkDataService;

    private final RebuildTrafficSignCacheCommand rebuildTrafficSignCacheCommand;

    private final RebuildSpeedLimitCacheCommand rebuildSpeedLimitCacheCommand;

    private final TrafficSignDataService trafficSignDataService;

    private final SpeedLimitDataService speedLimitDataService;

    @Override
    @SuppressWarnings("java:S1142")
    public Integer call() {
        if (networkDataService.dataExists()) {
            log.info("Network cache already exists, skipping creation");
        } else {
            try {
                networkDataService.recompileData();
            } catch (RuntimeException exception) {
                log.error("An error occurred while creating network", exception);
                return 1;
            }
        }

        if (trafficSignDataService.dataExists()) {
            log.info("Traffic sign cache already exists, skipping creation");
        } else {
            if (rebuildTrafficSignCacheCommand.call() != 0) {
                return 1;
            }
        }

        if (speedLimitDataService.dataExists()) {
            log.info("Speed limit cache already exists, skipping creation");
        } else {
            if (rebuildSpeedLimitCacheCommand.call() != 0) {
                return 1;
            }
        }

        return 0;
    }
}
