package nu.ndw.nls.accessibilitymap.backend.management.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperService;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(path = "/management/graph-hopper")
@RequiredArgsConstructor
@Profile({"component-test"})
@PreAuthorize("hasAuthority('admin')")
public class GraphHopperController {

    private final GraphHopperService graphHopperService;

    @PutMapping(path = "/reload")
    public void reloadGraphHopperNetwork() {

        graphHopperService.createNetworkGraphHopper();
    }
}
