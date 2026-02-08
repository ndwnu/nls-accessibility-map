package nu.ndw.nls.accessibilitymap.job.cache.command;

import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.network.NetworkDataService;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.TrafficSignDataService;
import nu.ndw.nls.accessibilitymap.job.trafficsign.command.UpdateCacheCommand;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Slf4j
@Component
@Command(name = "initializeCache")
@RequiredArgsConstructor
public class InitializeCacheCommand implements Callable<Integer> {

    private final NetworkDataService networkDataService;

    private final UpdateCacheCommand updateCacheCommand;

    private final TrafficSignDataService trafficSignDataService;

    @Override
    public Integer call() {
        if (networkDataService.networkExists()) {
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
                updateCacheCommand.call();
            } catch (RuntimeException exception) {
                log.error("An error occurred while creating traffic sign cache", exception);
                return 1;
            }
        }
        return 0;
    }
}
