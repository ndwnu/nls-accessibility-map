package nu.ndw.nls.accessibilitymap.test.acceptance.driver.job;

import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.springboot.test.component.driver.docker.DockerDriver;
import nu.ndw.nls.springboot.test.component.driver.docker.dto.Environment;
import nu.ndw.nls.springboot.test.component.state.StateManagement;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrafficSignJobDriver implements StateManagement {

    private final DockerDriver dockerDriver;

    public void runTrafficSignUpdateCacheJob() {

        dockerDriver.startServiceAndWaitToBeFinished(
                "nls-accessibility-map-traffic-sign-update-cache-job",
                List.of(
                        Environment.builder()
                                .key("COMMAND")
                                .value("update-cache")
                                .build(),
                        Environment.builder()
                                .key("NU_NDW_NLS_ACCESSIBILITYMAP_TRAFFICSIGNS_CACHE_FAILONNODATAONSTARTUP")
                                .value("false")
                                .build()
                ));
    }

    @Override
    public void clearState() {

    }
}
