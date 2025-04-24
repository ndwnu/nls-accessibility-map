package nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.github.resilience4j.springboot3.retry.autoconfigure.RetryAutoConfiguration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.client.configuration.EmissionZoneOAuthConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto.EmissionZone;
import nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto.EmissionZoneStatus;
import nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto.EmissionZoneType;
import nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto.EuroClassification;
import nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto.Exemption;
import nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto.FuelType;
import nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto.VehicleCategory;
import nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto.VehicleType;
import nu.ndw.nls.springboot.security.oauth2.client.services.OAuth2ClientCredentialsTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = {EmissionZoneClientTest.FeignTestConfig.class, RetryAutoConfiguration.class, EmissionZoneOAuthConfiguration.class})
@EnableConfigurationProperties
@AutoConfigureWireMock(port = 0)
@TestPropertySource(properties = {
        "nu.ndw.nls.accessibilitymap.trafficsigns.emission-zone.client.url=http://localhost:${wiremock.server.port}",
        "resilience4j.retry.instances.emissionZone.maxAttempts=2",
        "resilience4j.retry.instances.emissionZone.waitDuration=1ms"
})
class EmissionZoneClientTest {

    @Autowired
    private EmissionZoneClient emissionZoneClient;

    @MockitoBean
    private OAuth2ClientCredentialsTokenService oAuth2ClientCredentialsTokenService;

    private String accessToken;

    private String nextScenarioState;

    @BeforeEach
    void setUp() {

        accessToken = "token";
        nextScenarioState = STARTED;

        when(oAuth2ClientCredentialsTokenService.getAccessToken("traffic-sign-area-backend")).thenReturn(accessToken);
    }

    @Test
    void findAll() {

        stubValidResponse();

        List<EmissionZone> emissionZones = emissionZoneClient.findAll();
        assertThat(emissionZones).hasSize(1);

        validateResponse(emissionZones);
    }

    @Test
    void findAll_decodeExceptionRetry() {

        stubInvalidResponseDecodeException();
        stubValidResponse();

        List<EmissionZone> emissionZones = emissionZoneClient.findAll();
        assertThat(emissionZones).hasSize(1);

        validateResponse(emissionZones);
    }

    private void stubInvalidResponseDecodeException() {

        stubFor(get(
                urlEqualTo("/"))
                .inScenario("retry")
                .whenScenarioStateIs(nextScenarioState)
                .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo(format("Bearer %s", accessToken)))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, "text/html")
                        .withBody("<html><head><title>error</title></head><body>Error!</body></html>")
                )
                .willSetStateTo("valid-response"));
        nextScenarioState = "valid-response";
    }

    private void stubValidResponse() {

        stubFor(get(
                urlEqualTo("/"))
                .inScenario("retry")
                .whenScenarioStateIs(nextScenarioState)
                .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo(format("Bearer %s", accessToken)))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                [
                                    {
                                       "id":"NDW11_63a0104e-0b70-4b01-ad72-1ec692b41c47",
                                       "trafficRegulationOrderId":"stcrt-2017-72736",
                                       "type":"EmissionZoneEvent",
                                       "creationTime":"2024-06-10T14:03:58.208050Z",
                                       "startTime":"2020-01-01T00:00:00Z",
                                       "endTime":"2024-01-01T00:00:00Z",
                                       "version":2,
                                       "versionTime":"2024-08-29T13:14:12.399473Z",
                                       "emissionZoneType":"LOW_EMISSION_ZONE",
                                       "eventStatus":"ACTIVE",
                                       "euVehicleCategoryAndEmissionClassificationRestrictions":[],
                                       "euVehicleCategoryAndEmissionClassificationRestrictionExemptions":[
                                          {
                                             "emissionClassificationEuros":[
                                                "EURO_1",
                                                "EURO_2",
                                                "EURO_3",
                                                "EURO_4",
                                                "EURO_5",
                                                "EURO_6"
                                             ],
                                             "euVehicleCategories":[
                                                "M",
                                                "M_1",
                                                "M_2",
                                                "M_3",
                                                "N",
                                                "N_1",
                                                "N_2",
                                                "N_3"
                                             ],
                                             "id":"NDW11_63a0104e-0b70-4b01-ad72-1ec692b41c47_euro6Exemption",
                                             "maximalVehicleWeightAllowed":3500,
                                             "overallEndTime":"2027-12-31T23:00:00Z",
                                             "overallStartTime":"2025-06-01T00:00:00Z"
                                          }
                                       ],
                                       "genericVehicleRestriction":{
                                          "type":"FuelAndVehicleTypeRestriction",
                                          "id":"NDW11_63a0104e-0b70-4b01-ad72-1ec692b41c47_restriction",
                                          "version":2,
                                          "fuelType":"DIESEL",
                                          "vehicleType":"ANY_VEHICLE",
                                          "euVehicleCategories": [
                                            "M",
                                            "M_1",
                                            "M_2",
                                            "M_3",
                                            "N",
                                            "N_1",
                                            "N_2",
                                            "N_3"
                                          ]
                                       }
                                    }
                                ]
                                """)));
        nextScenarioState = "final-state";
    }

    private static void validateResponse(List<EmissionZone> emissionZones) {

        EmissionZone emissionZone = emissionZones.getFirst();
        assertThat(emissionZone.id()).isEqualTo("stcrt-2017-72736");
        assertThat(emissionZone.type()).isEqualTo(EmissionZoneType.LOW_EMISSION_ZONE);
        assertThat(emissionZone.status()).isEqualTo(EmissionZoneStatus.ACTIVE);
        assertThat(emissionZone.startTime())
                .isEqualTo(OffsetDateTime.parse("2020-01-01T00:00:00Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        assertThat(emissionZone.endTime())
                .isEqualTo(OffsetDateTime.parse("2024-01-01T00:00:00Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        assertThat(emissionZone.endTime())
                .isEqualTo(OffsetDateTime.parse("2024-01-01T00:00:00Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        assertThat(emissionZone.restriction().id()).isEqualTo("NDW11_63a0104e-0b70-4b01-ad72-1ec692b41c47_restriction");
        assertThat(emissionZone.restriction().fuelType()).isEqualTo(FuelType.DIESEL);
        assertThat(emissionZone.restriction().vehicleCategories()).containsExactlyInAnyOrderElementsOf(
                Arrays.stream(VehicleCategory.values())
                        .filter(vehicleCategory -> vehicleCategory != VehicleCategory.UNKNOWN)
                        .toList());
        assertThat(emissionZone.restriction().vehicleType()).isEqualTo(VehicleType.ANY_VEHICLE);

        assertThat(emissionZone.exemptions()).hasSize(1);
        Exemption exemption = emissionZone.exemptions().getFirst();
        assertThat(exemption.id()).isEqualTo("NDW11_63a0104e-0b70-4b01-ad72-1ec692b41c47_euro6Exemption");
        assertThat(exemption.euroClassifications()).containsExactlyInAnyOrderElementsOf(
                Arrays.stream(EuroClassification.values())
                        .filter(euroClassification -> euroClassification != EuroClassification.UNKNOWN)
                        .toList());
        assertThat(exemption.vehicleCategories()).containsExactlyInAnyOrderElementsOf(
                Arrays.stream(VehicleCategory.values())
                        .filter(vehicleCategory -> vehicleCategory != VehicleCategory.UNKNOWN)
                        .toList());
        assertThat(exemption.startTime())
                .isEqualTo(OffsetDateTime.parse("2025-06-01T00:00:00Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        assertThat(exemption.endTime())
                .isEqualTo(OffsetDateTime.parse("2027-12-31T23:00:00Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }

    @EnableFeignClients(clients = EmissionZoneClient.class)
    @Configuration
    @EnableAutoConfiguration
    static class FeignTestConfig {

    }
}