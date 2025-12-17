package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.job.configuration.JobConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.job.configuration.ServiceConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.job.dto.CommandRequestBody;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.job.dto.JobArgument;
import nu.ndw.nls.springboot.test.await.services.AwaitService;
import nu.ndw.nls.springboot.test.await.services.predicates.AwaitResponseStatusOkPredicate;
import nu.ndw.nls.springboot.test.component.driver.keycloak.KeycloakDriver;
import nu.ndw.nls.springboot.test.component.driver.web.AbstractWebClient;
import nu.ndw.nls.springboot.test.component.driver.web.dto.Request;
import nu.ndw.nls.springboot.test.component.driver.web.dto.Response;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobActuatorDriver extends AbstractWebClient {

    private final KeycloakDriver keycloakDriver;

    private final ObjectMapper objectMapper;

    private final AwaitService awaitService;

    public Response<String, Void> runJob(String command, List<JobArgument> jobArguments, ServiceConfiguration serviceConfiguration) {
//        Client actuatorClient = keycloakDriver.createClient("ActuatorClient", Set.of("ADMIN"));

        String payload;
        try {
            payload = objectMapper.writeValueAsString(CommandRequestBody.builder()
                    .command(command)
                    .arguments(jobArguments)
                    .build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        Request<String> request = Request.<String>builder()
                .host(serviceConfiguration.getHost())
                .port(serviceConfiguration.getPort())
                .method(HttpMethod.POST)
                .path("/actuator/command")
                .headers(Map.of(
                        HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE,
                        HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE
//                        HttpHeaders.AUTHORIZATION, actuatorClient.obtainBearerToken()
                ))
                .body("{\"payload\": \"%s\"}".formatted(payload.replace("\"", "\\\"")))
                .build();

        return this.request(request, Void.class);
    }

    public void waitForJobToBeReady(JobConfiguration jobConfiguration) {
        if(jobConfiguration.isRunAsService()) {
            awaitService.waitFor(
                    URI.create("http://%s:%s/actuator/health/readiness".formatted(
                            jobConfiguration.getServiceConfiguration().getHost(),
                            jobConfiguration.getServiceConfiguration().getPort())),
                    "Command - %s".formatted(jobConfiguration.getCommand()),
                    Duration.ofSeconds(60),
                    AwaitResponseStatusOkPredicate.getInstance());
        }
    }
}
