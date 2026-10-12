package nu.ndw.nls.accessibilitymap.job.trafficsign.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import feign.RetryableException;
import io.github.resilience4j.core.IntervalBiFunction;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import nu.ndw.nls.accessibilitymap.trafficsignclient.TrafficSignClientConfiguration;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TrafficSignGeoJsonDtoV5Json;
import nu.ndw.nls.accessibilitymap.trafficsignclient.services.TrafficSignService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.wiremock.spring.EnableWireMock;

@SpringBootTest(classes = TrafficSignServiceRetryTest.FeignTestConfig.class)
@EnableWireMock
@TestPropertySource(properties = {
        "nu.ndw.nls.accessibilitymap.trafficsignclient.currentStateControllerV5.url=http://localhost:${wiremock.server.port}",
        "resilience4j.retry.instances.trafficSign.waitDuration=1ms",
        "spring.liquibase.enabled=false",
        "management.datadog.metrics.export.enabled=false"
})
class TrafficSignServiceRetryTest {

    public static final String API_ENDPOINT = "/api/rest/static-road-data/traffic-signs/v5/current-state";

    @Autowired
    private TrafficSignService trafficSignService;

    @Autowired
    private RetryRegistry retryRegistry;

    @Test
    void getTrafficSigns() {
        stubFor(get(urlEqualTo(API_ENDPOINT + "?rvvCode=C6&rvvCode=C7&status=PLACED"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                  "type": "FeatureCollection",
                                  "features": [
                                    {
                                      "type": "Feature",
                                      "id": "05e7fe7b-2bee-4ba1-8f41-3d2cd156084a"
                                    }
                                  ]
                                }
                                """)));

        List<TrafficSignGeoJsonDtoV5Json> trafficSigns = trafficSignService.getTrafficSigns(Set.of("C7", "C6"));

        assertThat(trafficSigns)
                .extracting(TrafficSignGeoJsonDtoV5Json::getId)
                .containsExactly(UUID.fromString("05e7fe7b-2bee-4ba1-8f41-3d2cd156084a"));
        verify(1, getRequestedFor(urlEqualTo(API_ENDPOINT + "?rvvCode=C6&rvvCode=C7&status=PLACED")));
    }

    @Test
    void getTrafficSigns_retryAfterInternalServerError() {
        stubFor(get(urlEqualTo(API_ENDPOINT + "?rvvCode=C6&rvvCode=C7&status=PLACED"))
                .inScenario("retry")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .willSetStateTo("valid-response"));
        stubFor(get(urlEqualTo(API_ENDPOINT + "?rvvCode=C6&rvvCode=C7&status=PLACED"))
                .inScenario("retry")
                .whenScenarioStateIs("valid-response")
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                  "type": "FeatureCollection",
                                  "features": [
                                    {
                                      "type": "Feature",
                                      "id": "05e7fe7b-2bee-4ba1-8f41-3d2cd156084a"
                                    }
                                  ]
                                }
                                """)));

        List<TrafficSignGeoJsonDtoV5Json> trafficSigns = trafficSignService.getTrafficSigns(Set.of("C7", "C6"));

        assertThat(trafficSigns)
                .extracting(TrafficSignGeoJsonDtoV5Json::getId)
                .containsExactly(UUID.fromString("05e7fe7b-2bee-4ba1-8f41-3d2cd156084a"));
        verify(2, getRequestedFor(urlEqualTo(API_ENDPOINT + "?rvvCode=C6&rvvCode=C7&status=PLACED")));
    }

    @Test
    @SuppressWarnings("java:S5778")
    void getTrafficSigns_maxAttemptsExceeded() {
        stubFor(get(urlEqualTo(API_ENDPOINT + "?rvvCode=C6&rvvCode=C7&status=PLACED"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.SERVICE_UNAVAILABLE.value())));

        assertThatThrownBy(() -> trafficSignService.getTrafficSigns(Set.of("C7", "C6"))).isInstanceOf(RetryableException.class);
        verify(5, getRequestedFor(urlEqualTo(API_ENDPOINT + "?rvvCode=C6&rvvCode=C7&status=PLACED")));
    }

    @Test
    void getTrafficSigns_exponentialBackoff() {
        RetryConfig retryConfig = retryRegistry.retry("trafficSign").getRetryConfig();
        IntervalBiFunction<Object> intervalBiFunction = retryConfig.getIntervalBiFunction();

        assertThat(retryConfig.getMaxAttempts()).isEqualTo(5);
        assertThat(intervalBiFunction.apply(1, null)).isEqualTo(1L);
        assertThat(intervalBiFunction.apply(2, null)).isEqualTo(2L);
        assertThat(intervalBiFunction.apply(3, null)).isEqualTo(4L);
        assertThat(intervalBiFunction.apply(4, null)).isEqualTo(8L);
    }

    @Configuration
    @EnableAutoConfiguration
    @Import(TrafficSignClientConfiguration.class)
    static class FeignTestConfig {

    }
}
