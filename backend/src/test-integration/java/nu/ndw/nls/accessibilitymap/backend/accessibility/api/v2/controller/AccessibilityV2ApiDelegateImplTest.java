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
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.mapper.request.AccessibilityRequestMapperV2;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.mapper.response.AccessibilityResponseGeoJsonMapperV2;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.mapper.response.AccessibilityResponseMapperV2;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.validator.AccessibilityRequestValidator;
import nu.ndw.nls.accessibilitymap.backend.openapi.api.v2.AccessibilityV2ApiController;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.AccessibilityRequestJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.AccessibilityResponseGeoJsonJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.AccessibilityResponseJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.DestinationFeaturePropertiesJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.DestinationJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.DirectionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.EmissionClassJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.EmissionZoneTypeJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.FeatureJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.FuelTypeJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.MunicipalityAreaRequestJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.ReasonJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionConditionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionUnitSymbolJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RoadSectionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RoadSectionSegmentFeaturePropertiesJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RoadSectionSegmentJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.TrafficSignTypeJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.VehicleTypeJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.VehicleTypeRestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.security.SecurityConfig;
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

@WebMvcTest(controllers = AccessibilityV2ApiController.class)
@Import({SecurityConfig.class, AccessibilityV2ApiDelegateImpl.class})
@TestPropertySource(properties = {
        "nls.keycloak.url=http://localhost",
})
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class AccessibilityV2ApiDelegateImplTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GraphHopperService graphHopperService;

    @MockitoBean
    private AccessibilityRequestMapperV2 accessibilityRequestMapperV2;

    @MockitoBean
    private AccessibilityResponseMapperV2 accessibilityResponseMapperV2;

    @MockitoBean
    private AccessibilityResponseGeoJsonMapperV2 accessibilityResponseGeoJsonMapperV2;

    @MockitoBean
    private AccessibilityService accessibilityService;

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
        when(accessibilityRequestMapperV2.map(assertArg(AccessibilityV2ApiDelegateImplTest::assertAccessibilityReqeustJson)))
                .thenReturn(accessibilityRequest);
        when(accessibilityService.calculateAccessibility(networkGraphHopper, accessibilityRequest)).thenReturn(accessibility);

        AccessibilityResponseJson accessibilityMapResponseJson = AccessibilityResponseJson.builder()
                .roadSections(List.of(
                        RoadSectionJson.builder()
                                .id(1L)
                                .roadSectionSegments(List.of(RoadSectionSegmentJson.builder()
                                        .accessible(true)
                                        .geometry(new LineStringJson(List.of(List.of(1D, 2D)), TypeEnum.LINE_STRING))
                                        .direction(DirectionJson.BACKWARD)
                                        .build()))
                                .build()
                ))
                .destination(DestinationJson.builder()
                        .roadSectionId(2L)
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
                .build();
        when(accessibilityResponseMapperV2.map(
                assertArg(AccessibilityV2ApiDelegateImplTest::assertAccessibilityReqeustJson),
                eq(accessibility)))
                .thenReturn(accessibilityMapResponseJson);

        ResultActions mockMvcBuilder = mockMvc
                .perform(MockMvcRequestBuilders.post("/v2/accessiblility")
                        .accept(MediaType.APPLICATION_JSON)
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
                                  "area": {
                                    "type": "municipality",
                                    "id": "GM0001"
                                  },
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
                                  "roadSections" : [ {
                                    "id" : 1,
                                    "roadSectionSegments" : [ {
                                      "direction" : "backward",
                                      "accessible" : true,
                                      "geometry" : {
                                        "type" : "LineString",
                                        "coordinates" : [ [ 1.0, 2.0 ] ]
                                      }
                                    } ]
                                  } ],
                                  "destination" : {
                                    "roadSectionId" : 2,
                                    "accessible" : true,
                                    "reasons" : [ [ {
                                      "trafficSignId" : "71332fe6-fb88-4a91-8b72-eefc3c37c713",
                                      "trafficSignType" : "C1",
                                      "restrictions" : [ {
                                        "type" : "vehicleTypeRestriction",
                                        "unitSymbol" : "enum",
                                        "condition" : "equals",
                                        "values" : [ "car" ]
                                      } ]
                                    } ] ]
                                  }
                                }
                                """);
                break;
            default:
                fail("Status '%s' is not expected".formatted(expectedHttpStatus));
        }
    }

    @ParameterizedTest
    @CsvSource({
            "200",
            "401"
    })
    void getAccessibilityAsGeoJson(int expectedHttpStatusCode) throws Exception {

        HttpStatus expectedHttpStatus = HttpStatus.valueOf(expectedHttpStatusCode);

        when(graphHopperService.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        when(accessibilityRequestMapperV2.map(assertArg(AccessibilityV2ApiDelegateImplTest::assertAccessibilityReqeustJson)))
                .thenReturn(accessibilityRequest);
        when(accessibilityService.calculateAccessibility(networkGraphHopper, accessibilityRequest)).thenReturn(accessibility);

        AccessibilityResponseGeoJsonJson accessibilityResponseGeoJsonJson = AccessibilityResponseGeoJsonJson.builder()
                .features(List.of(
                        FeatureJson.builder()
                                .id(1)
                                .type(FeatureJson.TypeEnum.FEATURE)
                                .geometry(new LineStringJson(List.of(List.of(1.1D, 1.2D), List.of(2.1D, 2.2D)), TypeEnum.LINE_STRING))
                                .properties(RoadSectionSegmentFeaturePropertiesJson.builder()
                                        .roadSectionId(2L)
                                        .accessible(true)
                                        .direction(DirectionJson.BACKWARD)
                                        .build())
                                .build(),
                        FeatureJson.builder()
                                .id(3)
                                .type(FeatureJson.TypeEnum.FEATURE)
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
                assertArg(AccessibilityV2ApiDelegateImplTest::assertAccessibilityReqeustJson),
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
                                  "area": {
                                    "type": "municipality",
                                    "id": "GM0001"
                                  },
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
                                      "type": "roadSectionSegment",
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
                                      "type": "destination",
                                      "reasons" : [ [ {
                                        "trafficSignId" : "71332fe6-fb88-4a91-8b72-eefc3c37c713",
                                        "trafficSignType" : "C1",
                                        "restrictions" : [ {
                                          "type" : "vehicleTypeRestriction",
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

        assertThat(accessibilityRequestJson.getArea().getType()).isEqualTo(MunicipalityAreaRequestJson.TypeEnum.MUNICIPALITY);
        assertThat(accessibilityRequestJson.getArea()).isInstanceOf(MunicipalityAreaRequestJson.class);
        if (accessibilityRequestJson.getArea() instanceof MunicipalityAreaRequestJson municipalityAreaRequestJson) {
            assertThat(municipalityAreaRequestJson.getId()).isEqualTo("GM0001");
        }

        assertThat(accessibilityRequestJson.getDestination().getLatitude()).isEqualTo(1.1D);
        assertThat(accessibilityRequestJson.getDestination().getLongitude()).isEqualTo(2.2D);

        assertThat(accessibilityRequestJson.getIncludeAccessibleRoadSections()).isTrue();
        assertThat(accessibilityRequestJson.getIncludeInaccessibleRoadSections()).isTrue();
    }
}
