package nu.ndw.nls.accessibilitymap.test.performance.simulation.roadsections;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.PopulationBuilder;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.accessibilitymap.AccessibilityMapApiClient;
import nu.ndw.nls.accessibilitymap.test.performance.simulation.AbstractSimulation;
import nu.ndw.nls.springboot.test.component.driver.keycloak.KeycloakDriver;
import nu.ndw.nls.springboot.test.component.state.StateManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RoadSectionsSimulation extends AbstractSimulation {

    public RoadSectionsSimulation(
            KeycloakDriver keycloakDriver,
            StateManager stateManager,
            ObjectMapper objectMapper,
            AccessibilityMapApiClient accessibilityMapApiClient) {

        super(keycloakDriver, stateManager, objectMapper, accessibilityMapApiClient);
    }

    @Override
    public void before() {

        super.before();

        getKeycloakDriver().createAndActivateClient("myClient", Set.of("issue:read", "issue:write"));
    }

    public List<PopulationBuilder> getSimulations() {

        return List.of(
                scenario("Scenario:")
                        .group(getSimulationName()).on(
                                searchIssues()
                        )
                        .injectOpen(getSimulationBehaviour())
                        .protocols(List.of(getHttpProtocol()))
        );
    }

    private ChainBuilder searchIssues() {

        return exec(http("Section")
                .post("/api/rest/static-road-data/location-data-issues/v1/issues/search")
                .body(StringBody(convertToJsonBody(new Object())))
                .check(status().is(HttpStatus.OK.value()))
        );
    }

    private String convertToJsonBody(Object object) {

        try {
            return getObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException exception) {
            throw new RuntimeException(exception);
        }
    }

}
