package nu.ndw.nls.accessibilitymap.trafficsignclient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignData;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignJsonDtoV3;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.ZoneCode;
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

    private static final long ROAD_SECTION_A = 1;
    private static final long ROAD_SECTION_B = 2;
    @Autowired
    private TrafficSignService trafficSignService;


    @Test
    void getTrafficSigns_ok_correctRvvAndZoneCodes() {
        TrafficSignData trafficSigns = trafficSignService.getTrafficSigns(rvvCodes);
        assertNotNull(trafficSigns);
        assertEquals(141, trafficSigns.trafficSignsByRoadSectionId().size());
        assertEquals(LocalDate.of(2024, 1, 1), trafficSigns.maxNwbReferenceDate());
        assertEquals(Instant.parse("2024-01-30T15:23:14.605Z"), trafficSigns.maxEventTimestamp());

        List<TrafficSignJsonDtoV3> trafficSignsByRoadSectionA = trafficSigns.trafficSignsByRoadSectionId().get(
                ROAD_SECTION_A);
        assertEquals(1, trafficSignsByRoadSectionA.size());

        TrafficSignJsonDtoV3 firstTrafficSignRoadSectionA = trafficSignsByRoadSectionA.iterator().next();
        assertEquals("C12", firstTrafficSignRoadSectionA.getRvvCode());
        assertEquals(ZoneCode.BEGIN, firstTrafficSignRoadSectionA.getZoneCode());

        List<TrafficSignJsonDtoV3> trafficSignsByRoadSectionB = trafficSigns.trafficSignsByRoadSectionId().get(
                ROAD_SECTION_B);

        TrafficSignJsonDtoV3 firstTrafficSignRoadSectionB = trafficSignsByRoadSectionB.iterator().next();
        assertEquals("C12", firstTrafficSignRoadSectionB.getRvvCode());
        assertEquals(ZoneCode.END, firstTrafficSignRoadSectionB.getZoneCode());
    }

    @Import({TrafficSignConfiguration.class})
    public static class ConfigureFeignTests {

    }
}