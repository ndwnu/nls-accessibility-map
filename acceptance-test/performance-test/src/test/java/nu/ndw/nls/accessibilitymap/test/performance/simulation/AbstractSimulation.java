package nu.ndw.nls.accessibilitymap.test.performance.simulation;

import static io.gatling.javaapi.core.CoreDsl.details;
import static io.gatling.javaapi.core.CoreDsl.stressPeakUsers;
import static io.gatling.javaapi.http.HttpDsl.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.gatling.javaapi.core.Assertion;
import io.gatling.javaapi.core.OpenInjectionStep;
import io.gatling.javaapi.core.PopulationBuilder;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.accessibilitymap.AccessibilityMapApiClient;
import nu.ndw.nls.accessibilitymap.test.performance.configuration.ActiveSimulationConfiguration;
import nu.ndw.nls.accessibilitymap.test.performance.configuration.Assertions;
import nu.ndw.nls.accessibilitymap.test.performance.configuration.GenericAssertion;
import nu.ndw.nls.accessibilitymap.test.performance.configuration.Simulation;
import nu.ndw.nls.springboot.test.component.driver.keycloak.KeycloakDriver;
import nu.ndw.nls.springboot.test.component.state.StateManager;
import org.springframework.http.MediaType;

@RequiredArgsConstructor
public abstract class AbstractSimulation {

    @Getter(AccessLevel.PROTECTED)
    private final KeycloakDriver keycloakDriver;

    private final StateManager stateManager;

    @Getter(AccessLevel.PROTECTED)
    private final ObjectMapper objectMapper;

    private Object simulationSpecificConfiguration;

    @Getter(AccessLevel.PROTECTED)
    private final AccessibilityMapApiClient accessibilityMapApiClient;

    public void before() {

        stateManager.beforeScenario();
    }

    public void after() {

        stateManager.afterScenario();
    }

    public abstract List<PopulationBuilder> getSimulations();

    protected HttpProtocolBuilder getHttpProtocol() {

        HttpProtocolBuilder httpBuilder = http
                .baseUrl(accessibilityMapApiClient.getEndpoint())
                .acceptHeader(MediaType.APPLICATION_JSON_VALUE)
                .contentTypeHeader(MediaType.APPLICATION_JSON_VALUE);

        if (Objects.nonNull(keycloakDriver.getActiveClient())) {
            httpBuilder = httpBuilder.authorizationHeader(keycloakDriver.getActiveClient().obtainBearerToken());
        }

        return httpBuilder;
    }

    protected OpenInjectionStep getSimulationBehaviour() {

        return stressPeakUsers(getConfiguration().configuration().concurrentUsers())
                .during(getConfiguration().configuration().rampUpTime());
    }

    public List<Assertion> getAssertions() {

        ArrayList<Assertion> assertions = new ArrayList<Assertion>();
        GenericAssertion genericAssertion = getAssertionsConfiguration().genericAssertion();

        genericAssertion.scenarioDurationMax().ifPresent(responseTime -> assertions.add(
                details(getConfiguration().name()).responseTime().max().lte((int) responseTime.toMillis())));
        genericAssertion.scenarioDuration99thPercentile().ifPresent(responseTime -> assertions.add(
                details(getConfiguration().name()).responseTime().percentile(99).lte((int) responseTime.toMillis())));
        genericAssertion.scenarioDuration95thPercentile().ifPresent(responseTime -> assertions.add(
                details(getConfiguration().name()).responseTime().percentile(95).lte((int) responseTime.toMillis())));
        genericAssertion.scenarioDuration75thPercentile().ifPresent(responseTime -> assertions.add(
                details(getConfiguration().name()).responseTime().percentile(75).lte((int) responseTime.toMillis())));
        genericAssertion.scenarioDuration50thPercentile().ifPresent(responseTime -> assertions.add(
                details(getConfiguration().name()).responseTime().percentile(50).lte((int) responseTime.toMillis())));

        assertions.add(details(getConfiguration().name()).successfulRequests().percent()
                .gte(getAssertionsConfiguration().genericAssertion().successfulRunPercentage().doubleValue()));

        getAssertionsConfiguration().sections().forEach(section -> {
                    section.responseTimeMax().ifPresent(responseTime -> assertions.add(
                            details(getConfiguration().name(), section.name()).responseTime().max().lte((int) responseTime.toMillis())));
                    section.responseTime99thPercentile().ifPresent(responseTime -> assertions.add(
                            details(getConfiguration().name(), section.name()).responseTime().percentile(99)
                                    .lte((int) responseTime.toMillis())));
                    section.responseTime95thPercentile().ifPresent(responseTime -> assertions.add(
                            details(getConfiguration().name(), section.name()).responseTime().percentile(95)
                                    .lte((int) responseTime.toMillis())));
                    section.responseTime75thPercentile().ifPresent(responseTime -> assertions.add(
                            details(getConfiguration().name(), section.name()).responseTime().percentile(75)
                                    .lte((int) responseTime.toMillis())));
                    section.responseTime50thPercentile().ifPresent(responseTime -> assertions.add(
                            details(getConfiguration().name(), section.name()).responseTime().percentile(20)
                                    .lte((int) responseTime.toMillis())));
                }
        );
        return assertions;
    }

    private Simulation getConfiguration() {

        return ActiveSimulationConfiguration.INSTANCE.getSimulation();
    }

    protected String getSimulationName() {

        return getConfiguration().name();
    }

    @SuppressWarnings("unchecked")
    protected <T> T getSimulationSpecificConfiguration() {

        if (Objects.nonNull(simulationSpecificConfiguration)) {
            return (T) simulationSpecificConfiguration;
        }

        if (getSimulationSpecificConfigurationClass().isEmpty()) {
            throw new ConstraintViolationException("""
                    You requested a simulation specific configuration but you dit not provide any configuration under
                    `simulation-configuration`. Please add this configuration in your application properties.
                    """
                    , null);
        }

        Object configuration = objectMapper.convertValue(getConfiguration().configuration().simulationSpecificConfiguration(),
                getSimulationSpecificConfigurationClass().get());

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<Object>> errors = validator.validate(configuration);

            if (!errors.isEmpty()) {
                throw new ConstraintViolationException(errors);
            }
        }

        simulationSpecificConfiguration = configuration;

        return (T) this.simulationSpecificConfiguration;
    }

    protected Assertions getAssertionsConfiguration() {

        return getConfiguration().assertions();
    }

    protected Optional<Class<?>> getSimulationSpecificConfigurationClass() {

        return Optional.empty();
    }
}
