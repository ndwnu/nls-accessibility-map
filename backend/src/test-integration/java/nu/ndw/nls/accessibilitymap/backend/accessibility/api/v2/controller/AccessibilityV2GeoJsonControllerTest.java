package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.controller;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperService;
import nu.ndw.nls.accessibilitymap.accessibility.service.AccessibilityService;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.dto.AccessibilityGeoJsonResponse;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.mapper.request.AccessibilityRequestMapperV2;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.mapper.response.AccessibilityResponseGeoJsonMapperV2;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.validator.AccessibilityRequestValidator;
import nu.ndw.nls.accessibilitymap.backend.security.SecurityConfig;
import nu.ndw.nls.accessibilitymap.generated.model.v2.AccessibilityRequestJson;
import nu.ndw.nls.accessibilitymap.generated.model.v2.DestinationFeatureJson;
import nu.ndw.nls.accessibilitymap.generated.model.v2.DestinationFeaturePropertiesJson;
import nu.ndw.nls.accessibilitymap.generated.model.v2.DirectionJson;
import nu.ndw.nls.accessibilitymap.generated.model.v2.EmissionClassJson;
import nu.ndw.nls.accessibilitymap.generated.model.v2.EmissionZoneTypeJson;
import nu.ndw.nls.accessibilitymap.generated.model.v2.FuelTypeJson;
import nu.ndw.nls.accessibilitymap.generated.model.v2.ReasonJson;
import nu.ndw.nls.accessibilitymap.generated.model.v2.RestrictionConditionJson;
import nu.ndw.nls.accessibilitymap.generated.model.v2.RestrictionJson;
import nu.ndw.nls.accessibilitymap.generated.model.v2.RestrictionUnitSymbolJson;
import nu.ndw.nls.accessibilitymap.generated.model.v2.RoadSectionSegmentFeatureJson;
import nu.ndw.nls.accessibilitymap.generated.model.v2.RoadSectionSegmentPropertiesJson;
import nu.ndw.nls.accessibilitymap.generated.model.v2.TrafficSignTypeJson;
import nu.ndw.nls.accessibilitymap.generated.model.v2.VehicleTypeJson;
import nu.ndw.nls.accessibilitymap.generated.model.v2.VehicleTypeRestrictionJson;
import nu.ndw.nls.geojson.geometry.model.GeometryJson.TypeEnum;
import nu.ndw.nls.geojson.geometry.model.LineStringJson;
import nu.ndw.nls.geojson.geometry.model.PointJson;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import nu.ndw.nls.springboot.core.time.ClockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(controllers = AccessibilityV2GeoJsonController.class)
@Import({SecurityConfig.class})
@TestPropertySource(properties = {
        "nls.keycloak.url=http://localhost",
})
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class AccessibilityV2GeoJsonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GraphHopperService graphHopperService;

    @MockitoBean
    private AccessibilityService accessibilityService;

    @MockitoBean
    private AccessibilityResponseGeoJsonMapperV2 accessibilityResponseGeoJsonMapperV2;

    @MockitoBean
    private AccessibilityRequestMapperV2 accessibilityRequestMapperV2;

    @MockitoBean
    private AccessibilityRequestValidator accessibilityRequestValidator;

    @MockitoBean
    private ClockService clockService;

    @Mock
    private NetworkGraphHopper networkGraphHopper;

    @Mock
    private AccessibilityRequest accessibilityRequest;

    @Mock
    private Accessibility accessibility;

    private Jwt jwt;

    @BeforeEach
    void setUp() {

        jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("azp", "clientId")
                .build();
    }

    @ParameterizedTest
    @CsvSource({
            "200",
            "401"
    })
    void getAccessibility(int expectedHttpStatusCode) throws Exception {

        HttpStatus expectedHttpStatus = HttpStatus.valueOf(expectedHttpStatusCode);

        when(graphHopperService.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        when(accessibilityRequestMapperV2.map(assertArg(AccessibilityV2GeoJsonControllerTest::assertAccessibilityReqeustJson)))
                .thenReturn(accessibilityRequest);
        when(accessibilityService.calculateAccessibility(networkGraphHopper, accessibilityRequest)).thenReturn(accessibility);

        AccessibilityGeoJsonResponse accessibilityResponseGeoJsonJson = AccessibilityGeoJsonResponse.builder()
                .features(List.of(
                        RoadSectionSegmentFeatureJson.builder()
                                .id(1)
                                .type(RoadSectionSegmentFeatureJson.TypeEnum.FEATURE)
                                .geometry(new LineStringJson(List.of(List.of(1.1D, 1.2D), List.of(2.1D, 2.2D)), TypeEnum.LINE_STRING))
                                .properties(RoadSectionSegmentPropertiesJson.builder()
                                        .roadSectionId(2L)
                                        .accessible(true)
                                        .direction(DirectionJson.BACKWARD)
                                        .build())
                                .build(),
                        DestinationFeatureJson.builder()
                                .id(3)
                                .type(DestinationFeatureJson.TypeEnum.FEATURE)
                                .geometry(new PointJson(List.of(3.1D, 3.2D), TypeEnum.POINT))
                                .properties(DestinationFeaturePropertiesJson.builder()
                                        .roadSectionId(4L)
                                        .accessible(true)
                                        .reasons(List.of(List.of(ReasonJson.builder()
                                                .trafficSignId(UUID.fromString("71332fe6-fb88-4a91-8b72-eefc3c37c713"))
                                                .trafficSignType(TrafficSignTypeJson.C1)
                                                .restrictions(List.of(VehicleTypeRestrictionJson.builder()
                                                        .type(RestrictionJson.TypeEnum.VEHICLE_TYPE_RESTRICTION)
                                                        .unitSymbol(RestrictionUnitSymbolJson.ENUM)
                                                        .values(List.of(VehicleTypeJson.CAR))
                                                        .condition(RestrictionConditionJson.EQUALS)
                                                        .build()))
                                                .build())))
                                        .build())
                                .build()
                ))
                .build();
        when(accessibilityResponseGeoJsonMapperV2.map(
                assertArg(AccessibilityV2GeoJsonControllerTest::assertAccessibilityReqeustJson),
                eq(accessibility)))
                .thenReturn(accessibilityResponseGeoJsonJson);

        ResultActions mockMvcBuilder = mockMvc
                .perform(MockMvcRequestBuilders.post("/v2/accessiblility.geojson")
                        .accept("application/geo+json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with((expectedHttpStatus == HttpStatus.UNAUTHORIZED)
                                ? SecurityMockMvcRequestPostProcessors.anonymous()
                                : SecurityMockMvcRequestPostProcessors.jwt()
                                        .jwt(jwt))
                        .content("""
                                {
                                  "includeAccessibleRoadSections": true,
                                  "includeInaccessibleRoadSections": true,
                                  "destination": {
                                    "latitude": 1.1,
                                    "longitude": 2.2
                                  },
                                  "municipalityId": "GM0001",
                                  "vehicle": {
                                    "type": "truck",
                                    "width": 3.0,
                                    "height": 2.0,
                                    "length": 5.0,
                                    "weight": 4.0,
                                    "axleLoad": 6.0,
                                    "hasTrailer": true,
                                    "emissionClass": "zero",
                                    "fuelTypes": [
                                      "electric",
                                      "diesel"
                                    ]
                                  },
                                  "exclusions": {
                                     "emissionZoneTypes": ["low_emission_zone", "zero_emission_zone"],
                                     "emissionZoneIds": ["zone1","zone2"]
                                  }
                                }
                                
                                """));

        switch (expectedHttpStatus) {
            case UNAUTHORIZED:
                mockMvcBuilder.andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
                break;
            case OK:
                mockMvcBuilder.andExpect(status().is(HttpStatus.OK.value()));
                assertThatJson(mockMvcBuilder.andReturn().getResponse().getContentAsString())
                        .isEqualTo("""
                        {
                          "features" : [ {
                            "type" : "Feature",
                            "id" : 1,
                            "geometry" : {
                              "type" : "LineString",
                              "coordinates" : [ [ 1.1, 1.2 ], [ 2.1, 2.2 ] ]
                            },
                            "properties" : {
                              "roadSectionId" : 2,
                              "accessible" : true,
                              "direction" : "backward"
                            }
                          }, {
                            "type" : "Feature",
                            "id" : 3,
                            "geometry" : {
                              "type" : "Point",
                              "coordinates" : [ 3.1, 3.2 ]
                            },
                            "properties" : {
                              "roadSectionId" : 4,
                              "accessible" : true,
                              "reasons" : [ [ {
                                "trafficSignId" : "71332fe6-fb88-4a91-8b72-eefc3c37c713",
                                "trafficSignType" : "C1",
                                "restrictions" : [ {
                                  "type" : "VehicleTypeRestriction",
                                  "unitSymbol" : "enum",
                                  "condition" : "equals",
                                  "values" : [ "car" ]
                                } ]
                              } ] ]
                            }
                          } ]
                        }
                        """);
                break;
            default:
                fail("Status '%s' is not expected".formatted(expectedHttpStatus));
        }
    }

    private static void assertAccessibilityReqeustJson(AccessibilityRequestJson accessibilityRequestJson) {
        assertThat(accessibilityRequestJson.getVehicle().getType()).isEqualTo(VehicleTypeJson.TRUCK);
        assertThat(accessibilityRequestJson.getVehicle().getFuelTypes()).containsExactlyInAnyOrder(
                FuelTypeJson.ELECTRIC,
                FuelTypeJson.DIESEL);
        assertThat(accessibilityRequestJson.getVehicle().getLength()).isEqualTo(5F);
        assertThat(accessibilityRequestJson.getVehicle().getWidth()).isEqualTo(3F);
        assertThat(accessibilityRequestJson.getVehicle().getHeight()).isEqualTo(2F);
        assertThat(accessibilityRequestJson.getVehicle().getWeight()).isEqualTo(4F);
        assertThat(accessibilityRequestJson.getVehicle().getAxleLoad()).isEqualTo(6F);
        assertThat(accessibilityRequestJson.getVehicle().getHasTrailer()).isTrue();
        assertThat(accessibilityRequestJson.getVehicle().getEmissionClass()).isEqualTo(EmissionClassJson.ZERO);

        assertThat(accessibilityRequestJson.getExclusions().getEmissionZoneTypes()).containsExactlyInAnyOrder(
                EmissionZoneTypeJson.LOW_EMISSION_ZONE,
                EmissionZoneTypeJson.ZERO_EMISSION_ZONE);
        assertThat(accessibilityRequestJson.getExclusions().getEmissionZoneIds()).containsExactlyInAnyOrder("zone1", "zone2");

        assertThat(accessibilityRequestJson.getMunicipalityId()).isEqualTo("GM0001");

        assertThat(accessibilityRequestJson.getDestination().getLatitude()).isEqualTo(1.1D);
        assertThat(accessibilityRequestJson.getDestination().getLongitude()).isEqualTo(2.2D);

        assertThat(accessibilityRequestJson.getIncludeAccessibleRoadSections()).isTrue();
        assertThat(accessibilityRequestJson.getIncludeInaccessibleRoadSections()).isTrue();
    }
}
