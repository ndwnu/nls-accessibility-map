package nu.ndw.nls.accessibilitymap.accessibility.actuator;

import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.network.NetworkDataService;
import nu.ndw.nls.accessibilitymap.accessibility.roadchange.service.RoadChangesDataService;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.service.TrafficSignDataService;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.stereotype.Component;

@Component
@Endpoint(id = "accessibility-map-cache-reload")
@Slf4j
public class CacheActuator {

    private final NetworkDataService networkDataService;

    private final TrafficSignDataService trafficSignDataService;

    private final RoadChangesDataService roadChangesDataService;

    public CacheActuator(
            NetworkDataService networkDataService,
            TrafficSignDataService trafficSignDataService,
            RoadChangesDataService roadChangesDataService
    ) {
        this.networkDataService = networkDataService;
        this.trafficSignDataService = trafficSignDataService;
        this.roadChangesDataService = roadChangesDataService;
    }

    @WriteOperation
    public void reloadCache() {

        networkDataService.read();
        trafficSignDataService.read();
        roadChangesDataService.read();
        log.info("Cache reloaded");
    }
}
