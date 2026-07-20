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
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.ConditionPropertiesDtoV5Json.CategoryEnum;
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
                .hasSize(1)
                .satisfies(trafficSignGeoJsonDtoV5Jsons -> {

                    // Briefly assert the first result
                    TrafficSignGeoJsonDtoV5Json trafficSignGeoJsonDtoV5Json = trafficSignGeoJsonDtoV5Jsons.getFirst();
                    TrafficSignPropertiesDtoV5Json trafficSignPropertiesDtoV5 = trafficSignGeoJsonDtoV5Json.getProperties();
                    ConditionsDtoV5Json conditionsDtoV5 = trafficSignPropertiesDtoV5.getConditions();

                    assertThat(trafficSignGeoJsonDtoV5Json.getType()).isEqualTo("Feature");
                    assertThat(trafficSignGeoJsonDtoV5Json.getId()).hasToString("05e7fe7b-2bee-4ba1-8f41-3d2cd156084a");
                    assertThat(trafficSignGeoJsonDtoV5Json.getGeometry().getType()).isEqualTo("Point");
                    assertThat(trafficSignGeoJsonDtoV5Json.getGeometry().getCoordinates()).containsExactly(
                            6.4712785413943,
                            52.280951617613);
                    assertThat(trafficSignPropertiesDtoV5.getExternalReferences()).isEmpty();
                    assertThat(trafficSignGeoJsonDtoV5Json.getProperties().getRvvCode()).isEqualTo("C12");
                    assertThat(trafficSignPropertiesDtoV5.getZoneCode()).isNull();
                    assertThat(trafficSignPropertiesDtoV5.getSupplementarySigns())
                            .satisfiesExactly(
                                    firstSupplementarySign -> {
                                        assertThat(firstSupplementarySign.getSignCode())
                                                .isEqualTo(TextSignDtoV5Json.SignCodeEnum.OB254);
                                        assertThat(firstSupplementarySign.getText())
                                                .isEqualTo("ma t/m vr van 6-9h en 16-19h");
                                        assertThat(firstSupplementarySign.getOpeningHours())
                                                .isEqualTo("Mo-Fr 06:00-09:00, 16:00-19:00");
                                        assertThat(firstSupplementarySign.getExternalReferences()).isEmpty();
                                        assertThat(firstSupplementarySign.getIndex()).isZero();
                                    },
                                    secondSupplementarySign -> {
                                        assertThat(secondSupplementarySign.getSignCode())
                                                .isEqualTo(TextSignDtoV5Json.SignCodeEnum.OTHER);
                                        assertThat(secondSupplementarySign.getText())
                                                .isEqualTo("uitgezonderd aanwonenden en exploitatie aanliggende percelen");
                                        assertThat(secondSupplementarySign.getOpeningHours()).isEmpty();
                                        assertThat(secondSupplementarySign.getExternalReferences()).isEmpty();
                                        assertThat(secondSupplementarySign.getIndex()).isOne();
                                    });
                    assertThat(trafficSignPropertiesDtoV5.getPlacement()).isEqualTo("ALONG");
                    assertThat(trafficSignPropertiesDtoV5.getBearing()).isEqualTo(180);
                    assertThat(trafficSignPropertiesDtoV5.getRoadSectionId()).isEqualTo(600454559);
                    assertThat(trafficSignPropertiesDtoV5.getCountyCode()).isEqualTo("GM1742");
                    assertThat(trafficSignPropertiesDtoV5.getCountyName()).isEqualTo("Rijssen-Holten");
                    assertThat(trafficSignPropertiesDtoV5.getNwbVersion()).isEqualTo(LocalDate.of(2026, 2, 1));
                    assertThat(trafficSignPropertiesDtoV5.getPrivateProperty()).isFalse();
                    assertThat(trafficSignPropertiesDtoV5.getMissingRoadSection()).isFalse();
                    assertThat(trafficSignPropertiesDtoV5.getFraction()).isEqualTo(0.0095045d);
                    assertThat(trafficSignPropertiesDtoV5.getDrivingDirection())
                            .isEqualTo(TrafficSignPropertiesDtoV5Json.DrivingDirectionEnum.FORTH);
                    assertThat(conditionsDtoV5).isNotNull();
                    assertThat(conditionsDtoV5.getRestrictions()).isNotNull();
                    assertThat(conditionsDtoV5.getRestrictions().getVehicleType())
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
                    assertThat(conditionsDtoV5.getRestrictions().getCategory()).isEmpty();
                    assertThat(conditionsDtoV5.getRestrictions().getTimeValidity())
                            .isEqualTo("Mo-Fr 06:00-09:00, 16:00-19:00");
                    assertThat(conditionsDtoV5.getRestrictions().getEmissionClass()).isNull();
                    assertThat(conditionsDtoV5.getRestrictions().getFuelType()).isNull();
                    assertThat(conditionsDtoV5.getRestrictions().getAxleWeight()).isNull();
                    assertThat(conditionsDtoV5.getRestrictions().getHeight()).isNull();
                    assertThat(conditionsDtoV5.getRestrictions().getLength()).isNull();
                    assertThat(conditionsDtoV5.getRestrictions().getWeight()).isNull();
                    assertThat(conditionsDtoV5.getRestrictions().getWidth()).isNull();
                    assertThat(conditionsDtoV5.getExemptions()).satisfies(
                            conditionPropertiesDtoV5Json -> {
                                ConditionPropertiesDtoV5Json conditionPropertiesDtoV5 = conditionPropertiesDtoV5Json.getFirst();
                                assertThat(conditionPropertiesDtoV5.getVehicleType())
                                        .containsExactly(ConditionPropertiesDtoV5Json.VehicleTypeEnum.CAR);
                                assertThat(conditionPropertiesDtoV5.getCategory())
                                        .containsExactly(CategoryEnum.LOCAL_TRAFFIC);
                                assertThat(conditionPropertiesDtoV5.getEmissionClass()).isOne();
                                assertThat(conditionPropertiesDtoV5.getAxleWeight())
                                        .isEqualTo(2D);
                                assertThat(conditionPropertiesDtoV5.getHeight())
                                        .isEqualTo(3D);
                                assertThat(conditionPropertiesDtoV5.getLength())
                                        .isEqualTo(4D);
                                assertThat(conditionPropertiesDtoV5.getWeight())
                                        .isEqualTo(5D);
                                assertThat(conditionPropertiesDtoV5.getWidth())
                                        .isEqualTo(6D);


                            });
                    assertThat(trafficSignPropertiesDtoV5.getRegisteredOn()).isEqualTo(LocalDate.of(2016, 3, 16));
                    assertThat(trafficSignPropertiesDtoV5.getLastModifiedOn()).isEqualTo(LocalDate.of(2026, 2, 20));
                    assertThat(trafficSignPropertiesDtoV5.getValidatedOn()).isNull();
                    assertThat(trafficSignPropertiesDtoV5.getBlackCode()).isNull();
                    assertThat(trafficSignPropertiesDtoV5.getComments()).isEmpty();
                });
    }

    private String buildFeatureCollectionResponse() {
        return """
                  {
                    "type": "FeatureCollection",
                    "features": [
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
                            "exemptions": [
                              {
                                "vehicleType": [
                                  "car"
                                ],
                                "timeValidity": "Wo",
                                "category": ["localTraffic"],
                                "emissionClass": 1,
                                "axleWeight": 2,
                                "height": 3,
                                "length": 4,
                                "weight": 5,
                                "width": 6
                              }
                            ]
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
