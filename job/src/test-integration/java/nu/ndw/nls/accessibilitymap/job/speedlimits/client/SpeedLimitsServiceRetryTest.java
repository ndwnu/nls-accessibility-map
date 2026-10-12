package nu.ndw.nls.accessibilitymap.job.speedlimits.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import feign.RetryableException;
import io.github.resilience4j.core.IntervalBiFunction;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import java.time.LocalDate;
import nu.ndw.nls.roadattributesapi.client.feign.RoadAttributesApiConfiguration;
import nu.ndw.nls.roadattributesapi.client.feign.generated.model.v1.RoadSectionSpeedLimitJson;
import nu.ndw.nls.roadattributesapi.client.feign.generated.model.v1.SpeedLimitsRoadSectionResponseJson;
import nu.ndw.nls.springboot.security.oauth2.client.services.OAuth2ClientCredentialsTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.wiremock.spring.EnableWireMock;

@SpringBootTest(classes = SpeedLimitsServiceRetryTest.FeignTestConfig.class)
@EnableWireMock
@TestPropertySource(properties = {
        "nu.ndw.nls.roadattributesapi.speedLimits.url=http://localhost:${wiremock.server.port}",
        "resilience4j.retry.instances.speedLimit.waitDuration=1ms",
        "spring.liquibase.enabled=false",
        "management.datadog.metrics.export.enabled=false"
})
class SpeedLimitsServiceRetryTest {

    @Autowired
    private SpeedLimitsService speedLimitsService;

    @Autowired
    private RetryRegistry retryRegistry;

    @MockitoBean
    private OAuth2ClientCredentialsTokenService oAuth2ClientCredentialsTokenService;

    @BeforeEach
    void setUp() {
        when(oAuth2ClientCredentialsTokenService.getAccessToken("sa-nls-road-attributes-api")).thenReturn("token");
    }

    @Test
    void getSpeedLimits() {
        stubFor(get(urlEqualTo("/api/rest/static-road-data/road-attributes/v1/speed-limits/1999-12-01?page=2&size=1000"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                  "speedLimits": [
                                    {
                                      "nwbRoadSectionId": 600454559
                                    }
                                  ]
                                }
                                """)));

        SpeedLimitsRoadSectionResponseJson speedLimitsResponse = speedLimitsService.getSpeedLimits(LocalDate.of(1999, 12, 1), 2, 1000)
                .getBody();

        assertThat(speedLimitsResponse.getSpeedLimits())
                .extracting(RoadSectionSpeedLimitJson::getNwbRoadSectionId)
                .containsExactly(600454559L);
        verify(1,
                getRequestedFor(urlEqualTo("/api/rest/static-road-data/road-attributes/v1/speed-limits/1999-12-01?page=2&size=1000")));
    }

    @Test
    void getSpeedLimits_retryAfterInternalServerError() {
        stubFor(get(urlEqualTo("/api/rest/static-road-data/road-attributes/v1/speed-limits/1999-12-01?page=2&size=1000"))
                .inScenario("retry")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .willSetStateTo("valid-response"));
        stubFor(get(urlEqualTo("/api/rest/static-road-data/road-attributes/v1/speed-limits/1999-12-01?page=2&size=1000"))
                .inScenario("retry")
                .whenScenarioStateIs("valid-response")
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                  "speedLimits": [
                                    {
                                      "nwbRoadSectionId": 600454559
                                    }
                                  ]
                                }
                                """)));

        SpeedLimitsRoadSectionResponseJson speedLimitsResponse = speedLimitsService.getSpeedLimits(LocalDate.of(1999, 12, 1), 2, 1000)
                .getBody();

        assertThat(speedLimitsResponse.getSpeedLimits())
                .extracting(RoadSectionSpeedLimitJson::getNwbRoadSectionId)
                .containsExactly(600454559L);
        verify(2,
                getRequestedFor(urlEqualTo("/api/rest/static-road-data/road-attributes/v1/speed-limits/1999-12-01?page=2&size=1000")));
    }

    @Test
    void getSpeedLimits_maxAttemptsExceeded() {
        stubFor(get(urlEqualTo("/api/rest/static-road-data/road-attributes/v1/speed-limits/1999-12-01?page=2&size=1000"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.SERVICE_UNAVAILABLE.value())));

        assertThatThrownBy(() -> speedLimitsService.getSpeedLimits(LocalDate.of(1999, 12, 1), 2, 1000))
                .isInstanceOf(RetryableException.class);
        verify(5,
                getRequestedFor(urlEqualTo("/api/rest/static-road-data/road-attributes/v1/speed-limits/1999-12-01?page=2&size=1000")));
    }

    @Test
    void getSpeedLimits_exponentialBackoff() {
        RetryConfig retryConfig = retryRegistry.retry("speedLimit").getRetryConfig();
        IntervalBiFunction<Object> intervalBiFunction = retryConfig.getIntervalBiFunction();

        assertThat(retryConfig.getMaxAttempts()).isEqualTo(5);
        assertThat(intervalBiFunction.apply(1, null)).isEqualTo(1L);
        assertThat(intervalBiFunction.apply(2, null)).isEqualTo(2L);
        assertThat(intervalBiFunction.apply(3, null)).isEqualTo(4L);
        assertThat(intervalBiFunction.apply(4, null)).isEqualTo(8L);
    }

    @EnableFeignClients(clients = SpeedLimitsClient.class)
    @Configuration
    @EnableAutoConfiguration
    @Import({RoadAttributesApiConfiguration.class, SpeedLimitsService.class})
    static class FeignTestConfig {

    }
}
