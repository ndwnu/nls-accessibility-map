package nu.ndw.nls.accessibilitymap.backend.management.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.TrafficSignCacheUpdater;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(path = "/management/traffic-sign")
@RequiredArgsConstructor
@Profile({"component-test"})
@PreAuthorize("hasAuthority('admin')")
public class TrafficSignController {

    private final TrafficSignCacheUpdater trafficSignCacheUpdater;

    @PutMapping(path = "/reload")
    public void reloadGraphHopperNetwork() {

        trafficSignCacheUpdater.updateCache();
    }
}
