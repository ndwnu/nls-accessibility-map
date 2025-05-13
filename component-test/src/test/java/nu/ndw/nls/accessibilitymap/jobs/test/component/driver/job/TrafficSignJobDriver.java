package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.job;

import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.test.component.core.configuration.GeneralConfiguration;
import nu.ndw.nls.springboot.test.component.driver.docker.DockerDriver;
import nu.ndw.nls.springboot.test.component.driver.docker.dto.Environment;
import nu.ndw.nls.springboot.test.component.driver.docker.dto.Mode;
import nu.ndw.nls.springboot.test.component.state.StateManagement;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrafficSignJobDriver implements StateManagement {

    private final GeneralConfiguration generalConfiguration;

    private final DockerDriver dockerDriver;

    public void runTrafficSignUpdateCacheJob() {

        dockerDriver.startServiceAndWaitToBeFinished(
                "nls-accessibility-map-traffic-sign-update-cache-job",
                generalConfiguration.isWaitForDebuggerToBeConnected() ? Mode.DEBUG : Mode.NORMAL,
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
