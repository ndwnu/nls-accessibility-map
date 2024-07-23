package nu.ndw.nls.accessibilitymap.jobs.trafficsign.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.LocationJsonDtoV3;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignJsonDtoV3;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignDetailsJsonDtoV3;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignJsonDtoV3;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.ZoneCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NoEntrySignWindowedMapperTest {

    @Mock
    LocationJsonDtoV3 location;
    @Mock
    TrafficSignDetailsJsonDtoV3 details;
    @Mock
    TextSignJsonDtoV3 textSign;

    NoEntrySignWindowedMapper noEntrySignWindowedMapper;

    @BeforeEach
    void setUp() {
        noEntrySignWindowedMapper = new NoEntrySignWindowedMapper();
    }

    @Test
    void map_ok() {
        testMapping("C6", "C6T", "TIJD");
        testMapping("C7", "C7T", "TIJD");
        testMapping("C7b", "C7bT", "TIJD");
        testMapping("C12", "C12T", "TIJD");
        testMapping("C22c", "C22cT", "TIJD");
        testMapping("C20", "C20", "OTHER");
        testMapping("C66", "C66T", "TIJD");
    }

    private void testMapping(String inputRvvCode, String expectedRvvCode, String signType) {
        UUID uuid = UUID.randomUUID();
        LocalDate localDateNow = LocalDate.now();
        Instant instantNow = Instant.now();

        when(textSign.getType()).thenReturn(signType);

        TrafficSignJsonDtoV3 originalSign = TrafficSignJsonDtoV3.builder()
                .id(1)
                .ndwId(uuid)
                .type("type")
                .schemaVersion("schemaVersion")
                .validated("true")
                .validatedOn(localDateNow)
                .userId(1)
                .organisationId(1)
                .zoneCode(ZoneCode.BEGIN)
                .blackCode("blackCode")
                .textSigns(Collections.singletonList(textSign))
                .location(location)
                .details(details)
                .rvvCode(inputRvvCode)
                .publicationTimestamp(instantNow)
                .build();

        TrafficSignJsonDtoV3 expectedSign = originalSign.toBuilder()
                .rvvCode(expectedRvvCode)
                .build();

        TrafficSignJsonDtoV3 actual = noEntrySignWindowedMapper.map(originalSign);
        assertEquals(expectedSign, actual);
    }
}