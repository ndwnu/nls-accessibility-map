package nu.ndw.nls.accessibilitymap.trafficsignclient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignData;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.services.TrafficSignService;
import nu.ndw.nls.springboot.test.keycloak.KeycloakTestConfiguration;
import nu.ndw.nls.springboot.test.main.MainTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@Slf4j
@SpringBootTest
@ContextConfiguration(classes = TrafficSignClientIT.ConfigureFeignTests.class)
@ActiveProfiles({"integration-test"})
@EnableConfigurationProperties
@EnableAutoConfiguration
@Import({MainTestConfiguration.class, KeycloakTestConfiguration.class})
class TrafficSignClientIT {

    private final static Set<String> rvvCodes = Set.of("C6", "C7", "C7a", "C7b", "C8", "C9", "C10", "C11", "C12",
            "C22c", "C17", "C18", "C19", "C20", "C21");

    private static final long ROAD_SECTION_A = 600364496;
    private static final long ROAD_SECTION_B = 310326144;

    @Autowired
    private TrafficSignService trafficSignService;

    @Test
    void getTrafficSigns_ok_correctRvvAndZoneCodes() {
        TrafficSignData trafficSigns = trafficSignService.getTrafficSigns(rvvCodes, Collections.emptySet());
        assertNotNull(trafficSigns);
        assertEquals(170, trafficSigns.trafficSignsByRoadSectionId().size());
        assertEquals(LocalDate.of(2024, 7, 1), trafficSigns.maxNwbReferenceDate());

        LocalDate expectedDate = LocalDate.now();
        LocalDate actualDate = trafficSigns.maxEventTimestamp().atZone(ZoneId.of("UTC")).toLocalDate();
        assertEquals(expectedDate, actualDate);

        List<TrafficSignGeoJsonDto> trafficSignsByRoadSectionA = trafficSigns.trafficSignsByRoadSectionId().get(
                ROAD_SECTION_A);
        assertEquals(1, trafficSignsByRoadSectionA.size());

        TrafficSignGeoJsonDto firstTrafficSignRoadSectionA = trafficSignsByRoadSectionA.getFirst();
        assertEquals("C6", firstTrafficSignRoadSectionA.getProperties().getRvvCode());
        assertEquals("GM0307", firstTrafficSignRoadSectionA.getProperties().getCountyCode());

        List<TrafficSignGeoJsonDto> trafficSignsByRoadSectionB = trafficSigns.trafficSignsByRoadSectionId().get(
                ROAD_SECTION_B);

        TrafficSignGeoJsonDto firstTrafficSignRoadSectionB = trafficSignsByRoadSectionB.getFirst();
        assertEquals("C7", firstTrafficSignRoadSectionB.getProperties().getRvvCode());
        assertEquals("ZE", firstTrafficSignRoadSectionB.getProperties().getZoneCode());
    }

    @Import({TrafficSignConfiguration.class})
    public static class ConfigureFeignTests {

    }
}
