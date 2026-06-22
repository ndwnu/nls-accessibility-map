package nu.ndw.nls.accessibilitymap.trafficsignclient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignData;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TrafficSignGeoJsonDtoV5Json;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TrafficSignPropertiesDtoV5Json.ZoneCodeEnum;
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

    private static final long ROAD_SECTION_A = 600364496;

    private static final long ROAD_SECTION_B = 310326144;

    @Autowired
    private TrafficSignService trafficSignService;

    @Test
    void lala() {

        TrafficSignData trafficSigns = trafficSignService.getTrafficSigns(rvvCodes);


    }

    @Test
    void getTrafficSigns_correctRvvAndZoneCodes() {

        stubFor(get(urlEqualTo("/api/rest/static-road-data/traffic-signs/v5/current-state%s%s%s".formatted(
                "?countyCode=GM0307",
                rvvCodes.stream().map("&rvvCode=%s"::formatted).sorted().collect(Collectors.joining()),
                "&status=PLACED"))).withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.ALL_VALUE))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(buildFeatureCollectionResponse())));

        TrafficSignData trafficSigns = trafficSignService.getTrafficSigns(rvvCodes);
        assertNotNull(trafficSigns);
        assertEquals(2, trafficSigns.trafficSignsByRoadSectionId().size());
        assertEquals(LocalDate.of(2025, 2, 1), trafficSigns.maxNwbReferenceDate());

        LocalDate expectedDate = LocalDate.now();
        LocalDate actualDate = trafficSigns.maxEventTimestamp().atZone(ZoneId.of("UTC")).toLocalDate();
        assertEquals(expectedDate, actualDate);

        List<TrafficSignGeoJsonDtoV5Json> trafficSignsByRoadSectionA = trafficSigns.trafficSignsByRoadSectionId().get(ROAD_SECTION_A);
        assertEquals(1, trafficSignsByRoadSectionA.size());

        TrafficSignGeoJsonDtoV5Json firstTrafficSignRoadSectionA = trafficSignsByRoadSectionA.getFirst();
        assertEquals("C6", firstTrafficSignRoadSectionA.getProperties().getRvvCode());
        assertEquals("GM0307", firstTrafficSignRoadSectionA.getProperties().getCountyCode());

        List<TrafficSignGeoJsonDtoV5Json> trafficSignsByRoadSectionB = trafficSigns.trafficSignsByRoadSectionId().get(ROAD_SECTION_B);

        TrafficSignGeoJsonDtoV5Json firstTrafficSignRoadSectionB = trafficSignsByRoadSectionB.getFirst();
        assertEquals("C7", firstTrafficSignRoadSectionB.getProperties().getRvvCode());
        assertEquals(ZoneCodeEnum.END, firstTrafficSignRoadSectionB.getProperties().getZoneCode());
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
                         "nwbVersion": "2025-01-01",
                         "privateProperty": false,
                         "missingRoadSection": false,
                         "conditions": {
                           "restrictions": {
                             "vehicleType": [
                               "car",
                               "truck",
                               "bus",
                               "caravan",
                               "trailer",
                               "deliveryVan",
                               "taxi",
                               "agriculturalVehicle"
                             ]
                           },
                           "exemptions": []
                         },
                         "registeredOn": "2021-04-16",
                         "lastModifiedOn": "2025-01-29"
                       }
                     },
                     {
                       "type": "Feature",
                       "id": "a8b0fd05-5c2a-474e-8aae-ab4bd25d362f",
                       "geometry": {
                         "type": "Point",
                         "coordinates": [
                           5.3944671175717,
                           52.155548118045
                         ]
                       },
                       "properties": {
                         "externalReferences": [],
                         "rvvCode": "C7",
                         "zoneCode": "END",
                         "supplementarySigns": [],
                         "placement": "ALONG",
                         "bearing": 90,
                         "roadSectionId": 310326144,
                         "countyCode": "GM0307",
                         "countyName": "Amersfoort",
                         "nwbVersion": "2025-02-01",
                         "privateProperty": false,
                         "missingRoadSection": false,
                         "conditions": {
                           "restrictions": {
                             "vehicleType": [
                               "truck"
                             ]
                           },
                           "exemptions": []
                         },
                         "registeredOn": "2020-04-18",
                         "lastModifiedOn": "2025-01-29"
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

