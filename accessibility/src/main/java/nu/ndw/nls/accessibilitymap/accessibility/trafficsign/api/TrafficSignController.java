package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.TrafficSignCacheReadWriter;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(path = "/management/traffic-signs")
@RequiredArgsConstructor
@Profile({"component-test"})
public class TrafficSignController {

    private final TrafficSignCacheReadWriter trafficSignCacheReadWriter;

    @PutMapping(path = "/reload")
    public void reloadGraphHopperNetwork() {

        trafficSignCacheReadWriter.read();
    }
}
