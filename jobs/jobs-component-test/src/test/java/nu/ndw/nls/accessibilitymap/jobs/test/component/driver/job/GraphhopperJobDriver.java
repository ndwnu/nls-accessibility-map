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
public class GraphhopperJobDriver implements StateManagement {

    private final GeneralConfiguration generalConfiguration;

    private final DockerDriver dockerDriver;

    public void runGraphhopperJobCreateOrUpdateNetwork() {

        dockerDriver.startServiceAndWaitToBeFinished(
                "nls-accessibility-map-graphhopper-job",
                generalConfiguration.isWaitForDebuggerToBeConnected() ? Mode.DEBUG : Mode.NORMAL,
                List.of(
                        Environment.builder()
                                .key("GRAPHHOPPER_NETWORKNAME")
                                .value("accessibility_latest_component_test")
                                .build()));
    }

    public void runGraphhopperJobConfigureRabbitMQ() {

        dockerDriver.startServiceAndWaitToBeFinished(
                "nls-accessibility-map-graphhopper-configure-rabbitmq",
                Mode.NORMAL,
                List.of());
    }

    @Override
    public void clearState() {

    }
}
