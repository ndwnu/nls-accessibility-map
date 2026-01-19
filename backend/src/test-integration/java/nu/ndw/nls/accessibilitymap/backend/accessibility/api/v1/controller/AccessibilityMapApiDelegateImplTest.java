package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.controller;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperService;
import nu.ndw.nls.accessibilitymap.accessibility.service.AccessibilityService;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.mapper.request.AccessibilityRequestMapper;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.mapper.response.AccessibilityResponseMapper;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.mapper.response.RoadSectionFeatureCollectionMapper;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.validator.PointValidator;
import nu.ndw.nls.accessibilitymap.backend.municipality.repository.dto.Municipality;
import nu.ndw.nls.accessibilitymap.backend.municipality.service.MunicipalityService;
import nu.ndw.nls.accessibilitymap.backend.openapi.api.v1.AccessibilityMapApiController;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.AccessibilityMapResponseJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.EmissionClassJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.EmissionZoneTypeJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.FuelTypeJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.MatchedRoadSectionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RoadSectionFeatureCollectionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RoadSectionFeatureJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RoadSectionFeatureJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RoadSectionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RoadSectionPropertiesJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.VehicleTypeJson;
import nu.ndw.nls.accessibilitymap.backend.security.SecurityConfig;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import nu.ndw.nls.springboot.core.time.ClockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(controllers = AccessibilityMapApiController.class)
@Import({SecurityConfig.class, AccessibilityMapApiDelegateImpl.class, PointValidator.class})
@TestPropertySource(properties = {
        "nls.keycloak.url=http://localhost",
})
@AutoConfigureMockMvc
@ActiveProfiles("component-test")
@ExtendWith(MockitoExtension.class)
class AccessibilityMapApiDelegateImplTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GraphHopperService graphHopperService;

    @MockitoBean
    private AccessibilityResponseMapper accessibilityResponseMapper;

    @MockitoBean
    private RoadSectionFeatureCollectionMapper roadSectionFeatureCollectionMapper;

    @MockitoBean
    private MunicipalityService municipalityService;

    @MockitoBean
    private AccessibilityRequestMapper accessibilityRequestMapper;

    @MockitoBean
    private AccessibilityService accessibilityService;

    @MockitoBean
    private ClockService clockService;

    @Mock
    private NetworkGraphHopper networkGraphHopper;

    @Mock
    private Municipality municipality;

    @Mock
    private AccessibilityRequest accessibilityRequest;

    @Mock
    private Accessibility accessibility;

    @Mock
    private Collection<RoadSection> combinedAccessibility;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Include.NON_NULL);
    }

    @Test
    void getInaccessibleRoadSections() throws Exception {
        when(graphHopperService.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        when(municipalityService.getMunicipalityById("GM0001")).thenReturn(municipality);
        when(accessibilityRequestMapper.map(
                eq(municipality),
                assertArg(vehicleArguments -> {
                    assertThat(vehicleArguments.vehicleType()).isEqualTo(VehicleTypeJson.TRUCK);
                    assertThat(vehicleArguments.fuelTypes()).containsExactlyInAnyOrder(FuelTypeJson.ELECTRIC, FuelTypeJson.DIESEL);
                    assertThat(vehicleArguments.vehicleLength()).isEqualTo(5F);
                    assertThat(vehicleArguments.vehicleWidth()).isEqualTo(3F);
                    assertThat(vehicleArguments.vehicleHeight()).isEqualTo(2F);
                    assertThat(vehicleArguments.vehicleWeight()).isEqualTo(4F);
                    assertThat(vehicleArguments.vehicleAxleLoad()).isEqualTo(3F);
                    assertThat(vehicleArguments.vehicleHasTrailer()).isFalse();
                    assertThat(vehicleArguments.emissionClass()).isEqualTo(EmissionClassJson.ZERO);
                }),
                assertArg(excludes -> {
                    assertThat(excludes.emissionZoneTypes()).containsExactlyInAnyOrder(
                            EmissionZoneTypeJson.LOW_EMISSION_ZONE,
                            EmissionZoneTypeJson.ZERO_EMISSION_ZONE);
                    assertThat(excludes.emissionZoneIds()).containsExactlyInAnyOrder("zone1", "zone2");
                }),
                eq(1.1D),
                eq(2.2D)
        )).thenReturn(accessibilityRequest);
        when(accessibilityService.calculateAccessibility(networkGraphHopper, accessibilityRequest)).thenReturn(accessibility);

        AccessibilityMapResponseJson accessibilityMapResponseJson = AccessibilityMapResponseJson.builder()
                .inaccessibleRoadSections(List.of(
                        RoadSectionJson.builder()
                                .roadSectionId(1)
                                .backwardAccessible(true)
                                .forwardAccessible(false)
                                .build()
                ))
                .matchedRoadSection(MatchedRoadSectionJson.builder()
                        .forwardAccessible(true)
                        .backwardAccessible(false)
                        .roadSectionId(2)
                        .build())
                .build();
        when(accessibilityResponseMapper.map(accessibility)).thenReturn(accessibilityMapResponseJson);

        ResultActions mockMvcBuilder = mockMvc
                .perform(MockMvcRequestBuilders.get("/v1/municipalities/GM0001/road-sections")
                        .queryParam("vehicleType", "truck")
                        .queryParam("fuelTypes", "electric", "diesel")
                        .queryParam("vehicleLength", "5")
                        .queryParam("vehicleWidth", "3")
                        .queryParam("vehicleHeight", "2")
                        .queryParam("vehicleWeight", "4")
                        .queryParam("vehicleAxleLoad", "3")
                        .queryParam("vehicleHasTrailer", "false")
                        .queryParam("emissionClass", "zero")
                        .queryParam("excludeEmissionZoneIds", "zone1", "zone2")
                        .queryParam("excludeEmissionZoneTypes", "zero_emission_zone", "low_emission_zone")
                        .queryParam("latitude", "1.1")
                        .queryParam("longitude", "2.2")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.anonymous()));

        mockMvcBuilder.andExpect(status().is(HttpStatus.OK.value()));

        MvcResult response = mockMvcBuilder.andReturn();
        assertThatJson(response.getResponse()
                .getContentAsString()).isEqualTo(objectMapper.writeValueAsString(accessibilityMapResponseJson));
    }

    @Test
    void getInaccessibleRoadSections_emissionClassDefined_NoFuelTypes() throws Exception {
        when(graphHopperService.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        ResultActions mockMvcBuilder = mockMvc
                .perform(MockMvcRequestBuilders.get("/v1/municipalities/GM0001/road-sections")
                        .queryParam("vehicleType", "truck")
                        .queryParam("vehicleLength", "5")
                        .queryParam("vehicleWidth", "3")
                        .queryParam("vehicleHeight", "2")
                        .queryParam("vehicleWeight", "4")
                        .queryParam("vehicleAxleLoad", "3")
                        .queryParam("vehicleHasTrailer", "false")
                        .queryParam("emissionClass", "zero")
                        .queryParam("excludeEmissionZoneIds", "zone1", "zone2")
                        .queryParam("excludeEmissionZoneTypes", "zero_emission_zone", "low_emission_zone")
                        .queryParam("latitude", "1.1")
                        .queryParam("longitude", "2.2")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.anonymous()));

        mockMvcBuilder.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));

        MvcResult response = mockMvcBuilder.andReturn();
        assertThatJson(response.getResponse().getContentAsString()).isEqualTo("""
                {
                   "message": "If one of the environmental zone parameters is set, the other must be set as well."
                }
                """);
    }

    @Test
    void getInaccessibleRoadSections_fuelTypesDefined_noEmissionClass() throws Exception {
        when(graphHopperService.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        ResultActions mockMvcBuilder = mockMvc
                .perform(MockMvcRequestBuilders.get("/v1/municipalities/GM0001/road-sections")
                        .queryParam("vehicleType", "truck")
                        .queryParam("fuelTypes", "electric", "diesel")
                        .queryParam("vehicleLength", "5")
                        .queryParam("vehicleWidth", "3")
                        .queryParam("vehicleHeight", "2")
                        .queryParam("vehicleWeight", "4")
                        .queryParam("vehicleAxleLoad", "3")
                        .queryParam("vehicleHasTrailer", "false")
                        .queryParam("excludeEmissionZoneIds", "zone1", "zone2")
                        .queryParam("excludeEmissionZoneTypes", "zero_emission_zone", "low_emission_zone")
                        .queryParam("latitude", "1.1")
                        .queryParam("longitude", "2.2")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.anonymous()));

        mockMvcBuilder.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));

        MvcResult response = mockMvcBuilder.andReturn();
        assertThatJson(response.getResponse().getContentAsString()).isEqualTo("""
                {
                   "message": "If one of the environmental zone parameters is set, the other must be set as well."
                }
                """);
    }

    @Test
    void getInaccessibleRoadSections_invalidVehicleType() throws Exception {
        when(graphHopperService.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        ResultActions mockMvcBuilder = mockMvc
                .perform(MockMvcRequestBuilders.get("/v1/municipalities/GM0001/road-sections")
                        .queryParam("vehicleType", "invalid")
                        .queryParam("fuelTypes", "electric", "diesel")
                        .queryParam("vehicleLength", "5")
                        .queryParam("vehicleWidth", "3")
                        .queryParam("vehicleHeight", "2")
                        .queryParam("vehicleWeight", "4")
                        .queryParam("vehicleAxleLoad", "3")
                        .queryParam("vehicleHasTrailer", "false")
                        .queryParam("emissionClass", "zero")
                        .queryParam("excludeEmissionZoneIds", "zone1", "zone2")
                        .queryParam("excludeEmissionZoneTypes", "zero_emission_zone", "low_emission_zone")
                        .queryParam("latitude", "1.1")
                        .queryParam("longitude", "2.2")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.anonymous()));

        mockMvcBuilder.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));

        MvcResult response = mockMvcBuilder.andReturn();
        assertThatJson(response.getResponse().getContentAsString()).isEqualTo("""
                {
                   "message": "Argument 'vehicleType' with value 'invalid' is not valid"
                }
                """);
    }

    @Test
    void getInaccessibleRoadSections_invalidFuelType() throws Exception {
        when(graphHopperService.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        ResultActions mockMvcBuilder = mockMvc
                .perform(MockMvcRequestBuilders.get("/v1/municipalities/GM0001/road-sections")
                        .queryParam("vehicleType", "truck")
                        .queryParam("fuelTypes", "invalid")
                        .queryParam("vehicleLength", "5")
                        .queryParam("vehicleWidth", "3")
                        .queryParam("vehicleHeight", "2")
                        .queryParam("vehicleWeight", "4")
                        .queryParam("vehicleAxleLoad", "3")
                        .queryParam("vehicleHasTrailer", "false")
                        .queryParam("emissionClass", "zero")
                        .queryParam("excludeEmissionZoneIds", "zone1", "zone2")
                        .queryParam("excludeEmissionZoneTypes", "zero_emission_zone", "low_emission_zone")
                        .queryParam("latitude", "1.1")
                        .queryParam("longitude", "2.2")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.anonymous()));

        mockMvcBuilder.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));

        MvcResult response = mockMvcBuilder.andReturn();
        assertThatJson(response.getResponse().getContentAsString()).isEqualTo("""
                {
                   "message": "Argument 'fuelTypes' with value 'invalid' is not valid"
                }
                """);
    }

    @Test
    void getInaccessibleRoadSections_invalidEmissionClass() throws Exception {
        when(graphHopperService.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        ResultActions mockMvcBuilder = mockMvc
                .perform(MockMvcRequestBuilders.get("/v1/municipalities/GM0001/road-sections")
                        .queryParam("vehicleType", "truck")
                        .queryParam("fuelTypes", "electric", "diesel")
                        .queryParam("vehicleLength", "5")
                        .queryParam("vehicleWidth", "3")
                        .queryParam("vehicleHeight", "2")
                        .queryParam("vehicleWeight", "4")
                        .queryParam("vehicleAxleLoad", "3")
                        .queryParam("vehicleHasTrailer", "false")
                        .queryParam("emissionClass", "invalid")
                        .queryParam("excludeEmissionZoneIds", "zone1", "zone2")
                        .queryParam("excludeEmissionZoneTypes", "zero_emission_zone", "low_emission_zone")
                        .queryParam("latitude", "1.1")
                        .queryParam("longitude", "2.2")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.anonymous()));

        mockMvcBuilder.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));

        MvcResult response = mockMvcBuilder.andReturn();
        assertThatJson(response.getResponse().getContentAsString()).isEqualTo("""
                {
                   "message": "Argument 'emissionClass' with value 'invalid' is not valid"
                }
                """);
    }

    @Test
    void getInaccessibleRoadSections_destinationSet_missingLatitude() throws Exception {
        when(graphHopperService.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        ResultActions mockMvcBuilder = mockMvc
                .perform(MockMvcRequestBuilders.get("/v1/municipalities/GM0001/road-sections")
                        .queryParam("vehicleType", "truck")
                        .queryParam("fuelTypes", "electric", "diesel")
                        .queryParam("vehicleLength", "5")
                        .queryParam("vehicleWidth", "3")
                        .queryParam("vehicleHeight", "2")
                        .queryParam("vehicleWeight", "4")
                        .queryParam("vehicleAxleLoad", "3")
                        .queryParam("vehicleHasTrailer", "false")
                        .queryParam("emissionClass", "zero")
                        .queryParam("excludeEmissionZoneIds", "zone1", "zone2")
                        .queryParam("excludeEmissionZoneTypes", "zero_emission_zone", "low_emission_zone")
                        .queryParam("longitude", "2.2")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.anonymous()));

        mockMvcBuilder.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));

        MvcResult response = mockMvcBuilder.andReturn();
        assertThatJson(response.getResponse().getContentAsString()).isEqualTo("""
                {
                   "message": "When longitude is present, latitude must also be specified"
                }
                """);
    }

    @Test
    void getInaccessibleRoadSections_destinationSet_missingLongitude() throws Exception {
        when(graphHopperService.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        ResultActions mockMvcBuilder = mockMvc
                .perform(MockMvcRequestBuilders.get("/v1/municipalities/GM0001/road-sections")
                        .queryParam("vehicleType", "truck")
                        .queryParam("fuelTypes", "electric", "diesel")
                        .queryParam("vehicleLength", "5")
                        .queryParam("vehicleWidth", "3")
                        .queryParam("vehicleHeight", "2")
                        .queryParam("vehicleWeight", "4")
                        .queryParam("vehicleAxleLoad", "3")
                        .queryParam("vehicleHasTrailer", "false")
                        .queryParam("emissionClass", "zero")
                        .queryParam("excludeEmissionZoneIds", "zone1", "zone2")
                        .queryParam("excludeEmissionZoneTypes", "zero_emission_zone", "low_emission_zone")
                        .queryParam("latitude", "1.1")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.anonymous()));

        mockMvcBuilder.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));

        MvcResult response = mockMvcBuilder.andReturn();
        assertThatJson(response.getResponse().getContentAsString()).isEqualTo("""
                {
                   "message": "When latitude is present, longitude must also be specified"
                }
                """);
    }

    @Test
    void getInaccessibleRoadSections_invalidMunicipalityId() throws Exception {
        when(graphHopperService.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        ResultActions mockMvcBuilder = mockMvc
                .perform(MockMvcRequestBuilders.get("/v1/municipalities/INVALID_MUNICIPALITY_ID/road-sections")
                        .queryParam("vehicleType", "truck")
                        .queryParam("fuelTypes", "electric", "diesel")
                        .queryParam("vehicleLength", "5")
                        .queryParam("vehicleWidth", "3")
                        .queryParam("vehicleHeight", "2")
                        .queryParam("vehicleWeight", "4")
                        .queryParam("vehicleAxleLoad", "3")
                        .queryParam("vehicleHasTrailer", "false")
                        .queryParam("emissionClass", "zero")
                        .queryParam("excludeEmissionZoneIds", "zone1", "zone2")
                        .queryParam("excludeEmissionZoneTypes", "zero_emission_zone", "low_emission_zone")
                        .queryParam("latitude", "1.1")
                        .queryParam("longitude", "2.2")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.anonymous()));

        mockMvcBuilder.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));

        MvcResult response = mockMvcBuilder.andReturn();
        assertThatJson(response.getResponse().getContentAsString()).isEqualTo("""
                {
                   "message": "'municipalityId' must match \\"^(GM)(?=\\\\d{4}$)\\\\d*[1-9]\\\\d*\\\""
                }
                """);
    }

    @Test
    void getInaccessibleRoadSections_invalidVehicleLength() throws Exception {
        when(graphHopperService.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        ResultActions mockMvcBuilder = mockMvc
                .perform(MockMvcRequestBuilders.get("/v1/municipalities/GM0001/road-sections")
                        .queryParam("vehicleType", "truck")
                        .queryParam("fuelTypes", "electric", "diesel")
                        .queryParam("vehicleLength", "-5")
                        .queryParam("vehicleWidth", "3")
                        .queryParam("vehicleHeight", "2")
                        .queryParam("vehicleWeight", "4")
                        .queryParam("vehicleAxleLoad", "3")
                        .queryParam("vehicleHasTrailer", "false")
                        .queryParam("emissionClass", "zero")
                        .queryParam("excludeEmissionZoneIds", "zone1", "zone2")
                        .queryParam("excludeEmissionZoneTypes", "zero_emission_zone", "low_emission_zone")
                        .queryParam("latitude", "1.1")
                        .queryParam("longitude", "2.2")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.anonymous()));

        mockMvcBuilder.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));

        MvcResult response = mockMvcBuilder.andReturn();
        assertThatJson(response.getResponse().getContentAsString()).isEqualTo("""
                {
                   "message": "'vehicleLength' must be greater than or equal to 0.0"
                }
                """);
    }

    @Test
    void getInaccessibleRoadSections_invalidVehicleHasTrailer() throws Exception {
        when(graphHopperService.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        ResultActions mockMvcBuilder = mockMvc
                .perform(MockMvcRequestBuilders.get("/v1/municipalities/GM0001/road-sections")
                        .queryParam("vehicleType", "truck")
                        .queryParam("fuelTypes", "electric", "diesel")
                        .queryParam("vehicleLength", "5")
                        .queryParam("vehicleWidth", "3")
                        .queryParam("vehicleHeight", "2")
                        .queryParam("vehicleWeight", "4")
                        .queryParam("vehicleAxleLoad", "3")
                        .queryParam("vehicleHasTrailer", "invalid")
                        .queryParam("emissionClass", "zero")
                        .queryParam("excludeEmissionZoneIds", "zone1", "zone2")
                        .queryParam("excludeEmissionZoneTypes", "zero_emission_zone", "low_emission_zone")
                        .queryParam("latitude", "1.1")
                        .queryParam("longitude", "2.2")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.anonymous()));

        mockMvcBuilder.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));

        MvcResult response = mockMvcBuilder.andReturn();
        assertThatJson(response.getResponse().getContentAsString()).isEqualTo("""
                {
                   "message": "Argument 'vehicleHasTrailer' with value 'invalid' is not valid"
                }
                """);
    }

    @Test
    void getRoadSections() throws Exception {
        when(graphHopperService.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        when(municipalityService.getMunicipalityById("GM0001")).thenReturn(municipality);
        when(accessibilityRequestMapper.map(
                eq(municipality),
                assertArg(vehicleArguments -> {
                    assertThat(vehicleArguments.vehicleType()).isEqualTo(VehicleTypeJson.TRUCK);
                    assertThat(vehicleArguments.fuelTypes()).containsExactlyInAnyOrder(FuelTypeJson.ELECTRIC, FuelTypeJson.DIESEL);
                    assertThat(vehicleArguments.vehicleLength()).isEqualTo(5F);
                    assertThat(vehicleArguments.vehicleWidth()).isEqualTo(3F);
                    assertThat(vehicleArguments.vehicleHeight()).isEqualTo(2F);
                    assertThat(vehicleArguments.vehicleWeight()).isEqualTo(4F);
                    assertThat(vehicleArguments.vehicleAxleLoad()).isEqualTo(3F);
                    assertThat(vehicleArguments.vehicleHasTrailer()).isFalse();
                    assertThat(vehicleArguments.emissionClass()).isEqualTo(EmissionClassJson.ZERO);
                }),
                assertArg(excludes -> {
                    assertThat(excludes.emissionZoneTypes()).containsExactlyInAnyOrder(
                            EmissionZoneTypeJson.LOW_EMISSION_ZONE,
                            EmissionZoneTypeJson.ZERO_EMISSION_ZONE);
                    assertThat(excludes.emissionZoneIds()).containsExactlyInAnyOrder("zone1", "zone2");
                }),
                eq(1.1D),
                eq(2.2D)
        )).thenReturn(accessibilityRequest);
        when(accessibilityService.calculateAccessibility(networkGraphHopper, accessibilityRequest)).thenReturn(accessibility);
        when(accessibility.combinedAccessibility()).thenReturn(combinedAccessibility);
        when(accessibility.toRoadSection()).thenReturn(Optional.of(RoadSection.builder().id(2L).build()));
        when(accessibilityRequest.hasEndLocation()).thenReturn(true);

        RoadSectionFeatureCollectionJson roadSectionFeatureCollectionJson = RoadSectionFeatureCollectionJson.builder()
                .features(List.of(
                        RoadSectionFeatureJson.builder()
                                .id(1)
                                .type(TypeEnum.FEATURE)
                                .properties(RoadSectionPropertiesJson.builder()
                                        .accessible(true)
                                        .build())
                                .build()
                ))
                .build();

        when(roadSectionFeatureCollectionMapper.map(
                combinedAccessibility,
                true,
                2L,
                true)
        ).thenReturn(roadSectionFeatureCollectionJson);

        ResultActions mockMvcBuilder = mockMvc
                .perform(MockMvcRequestBuilders.get("/v1/municipalities/GM0001/road-sections.geojson")
                        .queryParam("vehicleType", "truck")
                        .queryParam("fuelTypes", "electric", "diesel")
                        .queryParam("vehicleLength", "5")
                        .queryParam("vehicleWidth", "3")
                        .queryParam("vehicleHeight", "2")
                        .queryParam("vehicleWeight", "4")
                        .queryParam("vehicleAxleLoad", "3")
                        .queryParam("vehicleHasTrailer", "false")
                        .queryParam("emissionClass", "zero")
                        .queryParam("excludeEmissionZoneIds", "zone1", "zone2")
                        .queryParam("excludeEmissionZoneTypes", "zero_emission_zone", "low_emission_zone")
                        .queryParam("latitude", "1.1")
                        .queryParam("longitude", "2.2")
                        .queryParam("accessible", "true")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.anonymous()));

        mockMvcBuilder.andExpect(status().is(HttpStatus.OK.value()));

        MvcResult response = mockMvcBuilder.andReturn();
        assertThatJson(response.getResponse()
                .getContentAsString()).isEqualTo(objectMapper.writeValueAsString(roadSectionFeatureCollectionJson));
    }

    @Test
    void getRoadSections_accessiblity_false() throws Exception {
        when(graphHopperService.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        when(municipalityService.getMunicipalityById("GM0001")).thenReturn(municipality);
        when(accessibilityRequestMapper.map(
                eq(municipality),
                assertArg(vehicleArguments -> {
                    assertThat(vehicleArguments.vehicleType()).isEqualTo(VehicleTypeJson.TRUCK);
                    assertThat(vehicleArguments.fuelTypes()).containsExactlyInAnyOrder(FuelTypeJson.ELECTRIC, FuelTypeJson.DIESEL);
                    assertThat(vehicleArguments.vehicleLength()).isEqualTo(5F);
                    assertThat(vehicleArguments.vehicleWidth()).isEqualTo(3F);
                    assertThat(vehicleArguments.vehicleHeight()).isEqualTo(2F);
                    assertThat(vehicleArguments.vehicleWeight()).isEqualTo(4F);
                    assertThat(vehicleArguments.vehicleAxleLoad()).isEqualTo(3F);
                    assertThat(vehicleArguments.vehicleHasTrailer()).isFalse();
                    assertThat(vehicleArguments.emissionClass()).isEqualTo(EmissionClassJson.ZERO);
                }),
                assertArg(excludes -> {
                    assertThat(excludes.emissionZoneTypes()).containsExactlyInAnyOrder(
                            EmissionZoneTypeJson.LOW_EMISSION_ZONE,
                            EmissionZoneTypeJson.ZERO_EMISSION_ZONE);
                    assertThat(excludes.emissionZoneIds()).containsExactlyInAnyOrder("zone1", "zone2");
                }),
                eq(1.1D),
                eq(2.2D)
        )).thenReturn(accessibilityRequest);
        when(accessibilityService.calculateAccessibility(networkGraphHopper, accessibilityRequest)).thenReturn(accessibility);
        when(accessibility.combinedAccessibility()).thenReturn(combinedAccessibility);
        when(accessibility.toRoadSection()).thenReturn(Optional.of(RoadSection.builder().id(2L).build()));
        when(accessibilityRequest.hasEndLocation()).thenReturn(true);

        RoadSectionFeatureCollectionJson roadSectionFeatureCollectionJson = RoadSectionFeatureCollectionJson.builder()
                .features(List.of(
                        RoadSectionFeatureJson.builder()
                                .id(1)
                                .type(TypeEnum.FEATURE)
                                .properties(RoadSectionPropertiesJson.builder()
                                        .accessible(true)
                                        .build())
                                .build()
                ))
                .build();

        when(roadSectionFeatureCollectionMapper.map(
                combinedAccessibility,
                true,
                2L,
                false)
        ).thenReturn(roadSectionFeatureCollectionJson);

        ResultActions mockMvcBuilder = mockMvc
                .perform(MockMvcRequestBuilders.get("/v1/municipalities/GM0001/road-sections.geojson")
                        .queryParam("vehicleType", "truck")
                        .queryParam("fuelTypes", "electric", "diesel")
                        .queryParam("vehicleLength", "5")
                        .queryParam("vehicleWidth", "3")
                        .queryParam("vehicleHeight", "2")
                        .queryParam("vehicleWeight", "4")
                        .queryParam("vehicleAxleLoad", "3")
                        .queryParam("vehicleHasTrailer", "false")
                        .queryParam("emissionClass", "zero")
                        .queryParam("excludeEmissionZoneIds", "zone1", "zone2")
                        .queryParam("excludeEmissionZoneTypes", "zero_emission_zone", "low_emission_zone")
                        .queryParam("latitude", "1.1")
                        .queryParam("longitude", "2.2")
                        .queryParam("accessible", "false")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.anonymous()));

        mockMvcBuilder.andExpect(status().is(HttpStatus.OK.value()));

        MvcResult response = mockMvcBuilder.andReturn();
        assertThatJson(response.getResponse()
                .getContentAsString()).isEqualTo(objectMapper.writeValueAsString(roadSectionFeatureCollectionJson));
    }

    @Test
    void getRoadSections_emissionClassDefined_NoFuelTypes() throws Exception {
        when(graphHopperService.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        ResultActions mockMvcBuilder = mockMvc
                .perform(MockMvcRequestBuilders.get("/v1/municipalities/GM0001/road-sections.geojson")
                        .queryParam("vehicleType", "truck")
                        .queryParam("vehicleLength", "5")
                        .queryParam("vehicleWidth", "3")
                        .queryParam("vehicleHeight", "2")
                        .queryParam("vehicleWeight", "4")
                        .queryParam("vehicleAxleLoad", "3")
                        .queryParam("vehicleHasTrailer", "false")
                        .queryParam("emissionClass", "zero")
                        .queryParam("excludeEmissionZoneIds", "zone1", "zone2")
                        .queryParam("excludeEmissionZoneTypes", "zero_emission_zone", "low_emission_zone")
                        .queryParam("latitude", "1.1")
                        .queryParam("longitude", "2.2")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.anonymous()));

        mockMvcBuilder.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));

        MvcResult response = mockMvcBuilder.andReturn();
        assertThatJson(response.getResponse().getContentAsString()).isEqualTo("""
                {
                   "message": "If one of the environmental zone parameters is set, the other must be set as well."
                }
                """);
    }

    @Test
    void getRoadSections_fuelTypesDefined_noEmissionClass() throws Exception {
        when(graphHopperService.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        ResultActions mockMvcBuilder = mockMvc
                .perform(MockMvcRequestBuilders.get("/v1/municipalities/GM0001/road-sections.geojson")
                        .queryParam("vehicleType", "truck")
                        .queryParam("fuelTypes", "electric", "diesel")
                        .queryParam("vehicleLength", "5")
                        .queryParam("vehicleWidth", "3")
                        .queryParam("vehicleHeight", "2")
                        .queryParam("vehicleWeight", "4")
                        .queryParam("vehicleAxleLoad", "3")
                        .queryParam("vehicleHasTrailer", "false")
                        .queryParam("excludeEmissionZoneIds", "zone1", "zone2")
                        .queryParam("excludeEmissionZoneTypes", "zero_emission_zone", "low_emission_zone")
                        .queryParam("latitude", "1.1")
                        .queryParam("longitude", "2.2")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.anonymous()));

        mockMvcBuilder.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));

        MvcResult response = mockMvcBuilder.andReturn();
        assertThatJson(response.getResponse().getContentAsString()).isEqualTo("""
                {
                   "message": "If one of the environmental zone parameters is set, the other must be set as well."
                }
                """);
    }

    @Test
    void getRoadSections_invalidVehicleType() throws Exception {
        when(graphHopperService.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        ResultActions mockMvcBuilder = mockMvc
                .perform(MockMvcRequestBuilders.get("/v1/municipalities/GM0001/road-sections.geojson")
                        .queryParam("vehicleType", "invalid")
                        .queryParam("fuelTypes", "electric", "diesel")
                        .queryParam("vehicleLength", "5")
                        .queryParam("vehicleWidth", "3")
                        .queryParam("vehicleHeight", "2")
                        .queryParam("vehicleWeight", "4")
                        .queryParam("vehicleAxleLoad", "3")
                        .queryParam("vehicleHasTrailer", "false")
                        .queryParam("emissionClass", "zero")
                        .queryParam("excludeEmissionZoneIds", "zone1", "zone2")
                        .queryParam("excludeEmissionZoneTypes", "zero_emission_zone", "low_emission_zone")
                        .queryParam("latitude", "1.1")
                        .queryParam("longitude", "2.2")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.anonymous()));

        mockMvcBuilder.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));

        MvcResult response = mockMvcBuilder.andReturn();
        assertThatJson(response.getResponse().getContentAsString()).isEqualTo("""
                {
                   "message": "Argument 'vehicleType' with value 'invalid' is not valid"
                }
                """);
    }

    @Test
    void getRoadSections_invalidFuelType() throws Exception {
        when(graphHopperService.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        ResultActions mockMvcBuilder = mockMvc
                .perform(MockMvcRequestBuilders.get("/v1/municipalities/GM0001/road-sections.geojson")
                        .queryParam("vehicleType", "truck")
                        .queryParam("fuelTypes", "invalid")
                        .queryParam("vehicleLength", "5")
                        .queryParam("vehicleWidth", "3")
                        .queryParam("vehicleHeight", "2")
                        .queryParam("vehicleWeight", "4")
                        .queryParam("vehicleAxleLoad", "3")
                        .queryParam("vehicleHasTrailer", "false")
                        .queryParam("emissionClass", "zero")
                        .queryParam("excludeEmissionZoneIds", "zone1", "zone2")
                        .queryParam("excludeEmissionZoneTypes", "zero_emission_zone", "low_emission_zone")
                        .queryParam("latitude", "1.1")
                        .queryParam("longitude", "2.2")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.anonymous()));

        mockMvcBuilder.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));

        MvcResult response = mockMvcBuilder.andReturn();
        assertThatJson(response.getResponse().getContentAsString()).isEqualTo("""
                {
                   "message": "Argument 'fuelTypes' with value 'invalid' is not valid"
                }
                """);
    }

    @Test
    void getRoadSections_invalidEmissionClass() throws Exception {
        when(graphHopperService.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        ResultActions mockMvcBuilder = mockMvc
                .perform(MockMvcRequestBuilders.get("/v1/municipalities/GM0001/road-sections.geojson")
                        .queryParam("vehicleType", "truck")
                        .queryParam("fuelTypes", "electric", "diesel")
                        .queryParam("vehicleLength", "5")
                        .queryParam("vehicleWidth", "3")
                        .queryParam("vehicleHeight", "2")
                        .queryParam("vehicleWeight", "4")
                        .queryParam("vehicleAxleLoad", "3")
                        .queryParam("vehicleHasTrailer", "false")
                        .queryParam("emissionClass", "invalid")
                        .queryParam("excludeEmissionZoneIds", "zone1", "zone2")
                        .queryParam("excludeEmissionZoneTypes", "zero_emission_zone", "low_emission_zone")
                        .queryParam("latitude", "1.1")
                        .queryParam("longitude", "2.2")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.anonymous()));

        mockMvcBuilder.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));

        MvcResult response = mockMvcBuilder.andReturn();
        assertThatJson(response.getResponse().getContentAsString()).isEqualTo("""
                {
                   "message": "Argument 'emissionClass' with value 'invalid' is not valid"
                }
                """);
    }

    @Test
    void getRoadSections_destinationSet_missingLatitude() throws Exception {
        when(graphHopperService.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        ResultActions mockMvcBuilder = mockMvc
                .perform(MockMvcRequestBuilders.get("/v1/municipalities/GM0001/road-sections.geojson")
                        .queryParam("vehicleType", "truck")
                        .queryParam("fuelTypes", "electric", "diesel")
                        .queryParam("vehicleLength", "5")
                        .queryParam("vehicleWidth", "3")
                        .queryParam("vehicleHeight", "2")
                        .queryParam("vehicleWeight", "4")
                        .queryParam("vehicleAxleLoad", "3")
                        .queryParam("vehicleHasTrailer", "false")
                        .queryParam("emissionClass", "zero")
                        .queryParam("excludeEmissionZoneIds", "zone1", "zone2")
                        .queryParam("excludeEmissionZoneTypes", "zero_emission_zone", "low_emission_zone")
                        .queryParam("longitude", "2.2")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.anonymous()));

        mockMvcBuilder.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));

        MvcResult response = mockMvcBuilder.andReturn();
        assertThatJson(response.getResponse().getContentAsString()).isEqualTo("""
                {
                   "message": "When longitude is present, latitude must also be specified"
                }
                """);
    }

    @Test
    void getRoadSections_destinationSet_missingLongitude() throws Exception {
        when(graphHopperService.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        ResultActions mockMvcBuilder = mockMvc
                .perform(MockMvcRequestBuilders.get("/v1/municipalities/GM0001/road-sections.geojson")
                        .queryParam("vehicleType", "truck")
                        .queryParam("fuelTypes", "electric", "diesel")
                        .queryParam("vehicleLength", "5")
                        .queryParam("vehicleWidth", "3")
                        .queryParam("vehicleHeight", "2")
                        .queryParam("vehicleWeight", "4")
                        .queryParam("vehicleAxleLoad", "3")
                        .queryParam("vehicleHasTrailer", "false")
                        .queryParam("emissionClass", "zero")
                        .queryParam("excludeEmissionZoneIds", "zone1", "zone2")
                        .queryParam("excludeEmissionZoneTypes", "zero_emission_zone", "low_emission_zone")
                        .queryParam("latitude", "1.1")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.anonymous()));

        mockMvcBuilder.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));

        MvcResult response = mockMvcBuilder.andReturn();
        assertThatJson(response.getResponse().getContentAsString()).isEqualTo("""
                {
                   "message": "When latitude is present, longitude must also be specified"
                }
                """);
    }

    @Test
    void getRoadSections_invalidMunicipalityId() throws Exception {
        when(graphHopperService.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        ResultActions mockMvcBuilder = mockMvc
                .perform(MockMvcRequestBuilders.get("/v1/municipalities/INVALID_MUNICIPALITY_ID/road-sections.geojson")
                        .queryParam("vehicleType", "truck")
                        .queryParam("fuelTypes", "electric", "diesel")
                        .queryParam("vehicleLength", "5")
                        .queryParam("vehicleWidth", "3")
                        .queryParam("vehicleHeight", "2")
                        .queryParam("vehicleWeight", "4")
                        .queryParam("vehicleAxleLoad", "3")
                        .queryParam("vehicleHasTrailer", "false")
                        .queryParam("emissionClass", "zero")
                        .queryParam("excludeEmissionZoneIds", "zone1", "zone2")
                        .queryParam("excludeEmissionZoneTypes", "zero_emission_zone", "low_emission_zone")
                        .queryParam("latitude", "1.1")
                        .queryParam("longitude", "2.2")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.anonymous()));

        mockMvcBuilder.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));

        MvcResult response = mockMvcBuilder.andReturn();
        assertThatJson(response.getResponse().getContentAsString()).isEqualTo("""
                {
                   "message": "'municipalityId' must match \\"^(GM)(?=\\\\d{4}$)\\\\d*[1-9]\\\\d*\\\""
                }
                """);
    }

    @Test
    void getRoadSections_invalidVehicleLength() throws Exception {
        when(graphHopperService.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        ResultActions mockMvcBuilder = mockMvc
                .perform(MockMvcRequestBuilders.get("/v1/municipalities/GM0001/road-sections.geojson")
                        .queryParam("vehicleType", "truck")
                        .queryParam("fuelTypes", "electric", "diesel")
                        .queryParam("vehicleLength", "-5")
                        .queryParam("vehicleWidth", "3")
                        .queryParam("vehicleHeight", "2")
                        .queryParam("vehicleWeight", "4")
                        .queryParam("vehicleAxleLoad", "3")
                        .queryParam("vehicleHasTrailer", "false")
                        .queryParam("emissionClass", "zero")
                        .queryParam("excludeEmissionZoneIds", "zone1", "zone2")
                        .queryParam("excludeEmissionZoneTypes", "zero_emission_zone", "low_emission_zone")
                        .queryParam("latitude", "1.1")
                        .queryParam("longitude", "2.2")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.anonymous()));

        mockMvcBuilder.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));

        MvcResult response = mockMvcBuilder.andReturn();
        assertThatJson(response.getResponse().getContentAsString()).isEqualTo("""
                {
                   "message": "'vehicleLength' must be greater than or equal to 0.0"
                }
                """);
    }

    @Test
    void getRoadSections_invalidVehicleHasTrailer() throws Exception {
        when(graphHopperService.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        ResultActions mockMvcBuilder = mockMvc
                .perform(MockMvcRequestBuilders.get("/v1/municipalities/GM0001/road-sections.geojson")
                        .queryParam("vehicleType", "truck")
                        .queryParam("fuelTypes", "electric", "diesel")
                        .queryParam("vehicleLength", "5")
                        .queryParam("vehicleWidth", "3")
                        .queryParam("vehicleHeight", "2")
                        .queryParam("vehicleWeight", "4")
                        .queryParam("vehicleAxleLoad", "3")
                        .queryParam("vehicleHasTrailer", "invalid")
                        .queryParam("emissionClass", "zero")
                        .queryParam("excludeEmissionZoneIds", "zone1", "zone2")
                        .queryParam("excludeEmissionZoneTypes", "zero_emission_zone", "low_emission_zone")
                        .queryParam("latitude", "1.1")
                        .queryParam("longitude", "2.2")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.anonymous()));

        mockMvcBuilder.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));

        MvcResult response = mockMvcBuilder.andReturn();
        assertThatJson(response.getResponse().getContentAsString()).isEqualTo("""
                {
                   "message": "Argument 'vehicleHasTrailer' with value 'invalid' is not valid"
                }
                """);
    }
}
