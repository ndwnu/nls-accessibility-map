package nu.ndw.nls.accessibilitymap.trafficsignclient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.ConditionPropertiesDtoV5Json;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.ConditionsDtoV5Json;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TextSignDtoV5Json;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TrafficSignGeoJsonDtoV5Json;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TrafficSignPropertiesDtoV5Json;
import nu.ndw.nls.accessibilitymap.trafficsignclient.services.TrafficSignService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.wiremock.spring.EnableWireMock;

@Slf4j
@SpringBootTest(classes = TrafficSignClientIT.ConfigureFeignTests.class)
@EnableConfigurationProperties
@EnableAutoConfiguration
@EnableWireMock
@TestPropertySource(properties = {
        "nu.ndw.nls.accessibilitymap.trafficsignclient.currentStateControllerV5.url: http://localhost:${wiremock.server.port}",})
class TrafficSignClientIT {

    private static final Set<String> rvvCodes = Set.of(
            "C6",
            "C7",
            "C7a",
            "C7b",
            "C8",
            "C9",
            "C10",
            "C11",
            "C12",
            "C22c",
            "C17",
            "C18",
            "C19",
            "C20",
            "C21");

    @Autowired
    private TrafficSignService trafficSignService;

    @Test
    void getTrafficSigns_correctRvvAndZoneCodes() {

        stubFor(get(urlEqualTo("/api/rest/static-road-data/traffic-signs/v5/current-state?%s%s".formatted(
                rvvCodes.stream()
                        .map("rvvCode=%s&"::formatted)
                        .sorted()
                        .collect(Collectors.joining()),
                "status=PLACED")))
                .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.ALL_VALUE))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(buildFeatureCollectionResponse())));

        List<TrafficSignGeoJsonDtoV5Json> trafficSigns = trafficSignService.getTrafficSigns(rvvCodes);
        assertThat(trafficSigns)
                .hasSize(2)
                .satisfies(trafficSignGeoJsonDtoV5Jsons -> {

                    // Briefly assert the first result
                    TrafficSignGeoJsonDtoV5Json first = trafficSignGeoJsonDtoV5Jsons.getFirst();
                    assertThat(first.getType()).isEqualTo("Feature");
                    assertThat(first.getId()).hasToString("3722943b-ba5d-48de-8d34-3d8a4725073b");
                    assertThat(first.getProperties().getRvvCode()).isEqualTo("C6");

                    TrafficSignGeoJsonDtoV5Json second = trafficSignGeoJsonDtoV5Jsons.getLast();
                    TrafficSignPropertiesDtoV5Json secondProperties = second.getProperties();
                    ConditionsDtoV5Json secondConditions = secondProperties.getConditions();

                    assertThat(second.getType()).isEqualTo("Feature");
                    assertThat(second.getId()).hasToString("05e7fe7b-2bee-4ba1-8f41-3d2cd156084a");
                    assertThat(second.getGeometry().getType()).isEqualTo("Point");
                    assertThat(second.getGeometry().getCoordinates()).containsExactly(6.4712785413943, 52.280951617613);
                    assertThat(secondProperties.getExternalReferences()).isEmpty();
                    assertThat(second.getProperties().getRvvCode()).isEqualTo("C12");
                    assertThat(secondProperties.getZoneCode()).isNull();
                    assertThat(secondProperties.getSupplementarySigns())
                            .satisfiesExactly(
                                    firstSupplementarySign -> {
                                        assertThat(firstSupplementarySign.getSignCode())
                                                .isEqualTo(TextSignDtoV5Json.SignCodeEnum.OB254);
                                        assertThat(firstSupplementarySign.getText())
                                                .isEqualTo("ma t/m vr van 6-9h en 16-19h");
                                        assertThat(firstSupplementarySign.getOpeningHours())
                                                .isEqualTo("Mo-Fr 06:00-09:00, 16:00-19:00");
                                        assertThat(firstSupplementarySign.getExternalReferences()).isEmpty();
                                        assertThat(firstSupplementarySign.getIndex()).isEqualTo(0);
                                    },
                                    secondSupplementarySign -> {
                                        assertThat(secondSupplementarySign.getSignCode())
                                                .isEqualTo(TextSignDtoV5Json.SignCodeEnum.OTHER);
                                        assertThat(secondSupplementarySign.getText())
                                                .isEqualTo("uitgezonderd aanwonenden en exploitatie aanliggende percelen");
                                        assertThat(secondSupplementarySign.getOpeningHours()).isNull();
                                        assertThat(secondSupplementarySign.getExternalReferences()).isEmpty();
                                        assertThat(secondSupplementarySign.getIndex()).isEqualTo(1);
                                    });
                    assertThat(secondProperties.getPlacement()).isEqualTo("ALONG");
                    assertThat(secondProperties.getBearing()).isEqualTo(180);
                    assertThat(secondProperties.getRoadSectionId()).isEqualTo(600454559);
                    assertThat(secondProperties.getCountyCode()).isEqualTo("GM1742");
                    assertThat(secondProperties.getCountyName()).isEqualTo("Rijssen-Holten");
                    assertThat(secondProperties.getNwbVersion()).isEqualTo(LocalDate.of(2026, 2, 1));
                    assertThat(secondProperties.getPrivateProperty()).isFalse();
                    assertThat(secondProperties.getMissingRoadSection()).isFalse();
                    assertThat(secondProperties.getFraction()).isEqualTo(0.0095045d);
                    assertThat(secondProperties.getDrivingDirection())
                            .isEqualTo(TrafficSignPropertiesDtoV5Json.DrivingDirectionEnum.FORTH);
                    assertThat(secondConditions).isNotNull();
                    assertThat(secondConditions.getRestrictions()).isNotNull();
                    assertThat(secondConditions.getRestrictions().getVehicleType())
                            .containsExactlyInAnyOrder(
                                    ConditionPropertiesDtoV5Json.VehicleTypeEnum.CAR,
                                    ConditionPropertiesDtoV5Json.VehicleTypeEnum.MICROCAR,
                                    ConditionPropertiesDtoV5Json.VehicleTypeEnum.TRUCK,
                                    ConditionPropertiesDtoV5Json.VehicleTypeEnum.BUS,
                                    ConditionPropertiesDtoV5Json.VehicleTypeEnum.MOPED,
                                    ConditionPropertiesDtoV5Json.VehicleTypeEnum.MOTORCYCLE,
                                    ConditionPropertiesDtoV5Json.VehicleTypeEnum.CARAVAN,
                                    ConditionPropertiesDtoV5Json.VehicleTypeEnum.TRAILER,
                                    ConditionPropertiesDtoV5Json.VehicleTypeEnum.DELIVERY_VAN,
                                    ConditionPropertiesDtoV5Json.VehicleTypeEnum.TAXI,
                                    ConditionPropertiesDtoV5Json.VehicleTypeEnum.AGRICULTURAL_VEHICLE);
                    assertThat(secondConditions.getRestrictions().getCategory()).isEmpty();
                    assertThat(secondConditions.getRestrictions().getTimeValidity())
                            .isEqualTo("Mo-Fr 06:00-09:00, 16:00-19:00");
                    assertThat(secondConditions.getRestrictions().getEmissionClass()).isNull();
                    assertThat(secondConditions.getRestrictions().getFuelType()).isNull();
                    assertThat(secondConditions.getRestrictions().getAxleWeight()).isNull();
                    assertThat(secondConditions.getRestrictions().getHeight()).isNull();
                    assertThat(secondConditions.getRestrictions().getLength()).isNull();
                    assertThat(secondConditions.getRestrictions().getWeight()).isNull();
                    assertThat(secondConditions.getRestrictions().getWidth()).isNull();
                    assertThat(secondConditions.getExemptions()).isEmpty();
                    assertThat(secondProperties.getRegisteredOn()).isEqualTo(LocalDate.of(2016, 3, 16));
                    assertThat(secondProperties.getLastModifiedOn()).isEqualTo(LocalDate.of(2026, 2, 20));
                    assertThat(secondProperties.getValidatedOn()).isNull();
                    assertThat(secondProperties.getBlackCode()).isNull();
                    assertThat(secondProperties.getComments()).isEmpty();
                });
    }


    private String buildFeatureCollectionResponse() {
        return """
                {
                  "type": "FeatureCollection",
                  "features": [
                    {
                      "type": "Feature",
                      "id": "3722943b-ba5d-48de-8d34-3d8a4725073b",
                      "geometry": {
                        "type": "Point",
                        "coordinates": [
                          5.3844458417424,
                          52.157320095061
                        ]
                      },
                      "properties": {
                        "externalReferences": [],
                        "rvvCode": "C6",
                        "zoneCode": "END",
                        "supplementarySigns": [],
                        "placement": "ALONG",
                        "bearing": 0,
                        "roadSectionId": 600364496,
                        "countyCode": "GM0307",
                        "countyName": "Amersfoort",
                        "nwbVersion": "2024-03-01",
                        "privateProperty": false,
                        "missingRoadSection": false,
                        "fraction": 0.27672621607780457,
                        "drivingDirection": "FORTH",
                        "registeredOn": "2021-04-16",
                        "lastModifiedOn": "2024-02-19"
                      }
                    },
                    {
                      "type": "Feature",
                      "id": "05e7fe7b-2bee-4ba1-8f41-3d2cd156084a",
                      "geometry": {
                        "type": "Point",
                        "coordinates": [
                          6.4712785413943,
                          52.280951617613
                        ]
                      },
                      "properties": {
                        "externalReferences": [],
                        "rvvCode": "C12",
                        "supplementarySigns": [
                          {
                            "signCode": "OB254",
                            "text": "ma t/m vr van 6-9h en 16-19h",
                            "openingHours": "Mo-Fr 06:00-09:00, 16:00-19:00",
                            "externalReferences": [],
                            "index": 0
                          },
                          {
                            "signCode": "OTHER",
                            "text": "uitgezonderd aanwonenden en exploitatie aanliggende percelen",
                            "externalReferences": [],
                            "index": 1
                          }
                        ],
                        "placement": "ALONG",
                        "bearing": 180,
                        "roadSectionId": 600454559,
                        "countyCode": "GM1742",
                        "countyName": "Rijssen-Holten",
                        "nwbVersion": "2026-02-01",
                        "privateProperty": false,
                        "missingRoadSection": false,
                        "fraction": 0.0095045,
                        "drivingDirection": "FORTH",
                        "conditions": {
                          "restrictions": {
                            "vehicleType": [
                              "car",
                              "microcar",
                              "truck",
                              "bus",
                              "moped",
                              "motorcycle",
                              "caravan",
                              "trailer",
                              "deliveryVan",
                              "taxi",
                              "agriculturalVehicle"
                            ],
                            "timeValidity": "Mo-Fr 06:00-09:00, 16:00-19:00"
                          },
                          "exemptions": []
                        },
                        "registeredOn": "2016-03-16",
                        "lastModifiedOn": "2026-02-20"
                      }
                    }
                  ]
                }
                """;
    }

    @Import({TrafficSignClientConfiguration.class})
    public static class ConfigureFeignTests {

    }
}
