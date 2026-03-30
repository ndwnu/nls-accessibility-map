package nu.ndw.nls.accessibilitymap.job.cache.command;

import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.network.NetworkDataService;
import nu.ndw.nls.accessibilitymap.accessibility.roadchange.service.RoadChangesDataService;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.TrafficSignDataService;
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

    private final TrafficSignDataService trafficSignDataService;

    private final RoadChangesDataService roadChangesDataService;

    @Override
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
            try {
                rebuildTrafficSignCacheCommand.call();
            } catch (RuntimeException exception) {
                log.error("An error occurred while creating traffic sign cache", exception);
                return 1;
            }
        }

        if (roadChangesDataService.dataExists()) {
            log.info("Changed road sections cache already exists, skipping creation");
        } else {
            try {
                roadChangesDataService.createEmptyCache();
            } catch (RuntimeException exception) {
                log.error("An error occurred while creating changed road sections cache", exception);
                return 1;
            }
        }
        return 0;
    }
}
