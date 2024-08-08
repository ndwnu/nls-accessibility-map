package nu.ndw.nls.accessibilitymap.jobs.graphhopper.trafficsign.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignPropertiesDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NoEntrySignWindowedMapperTest {

    @InjectMocks
    NoEntrySignWindowedMapper noEntrySignWindowedMapper;

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
        TextSignDto textSignDto = TextSignDto.builder()
                .type(signType)
                .build();

        TrafficSignPropertiesDto trafficSignPropertiesDto = TrafficSignPropertiesDto.builder()
                .rvvCode(inputRvvCode)
                .textSigns(List.of(textSignDto))
                .build();

        TrafficSignGeoJsonDto trafficSignGeoJsonDto = TrafficSignGeoJsonDto.builder()
                .properties(trafficSignPropertiesDto)
                .build();

        TrafficSignGeoJsonDto actual = noEntrySignWindowedMapper.map(trafficSignGeoJsonDto);

        assertEquals(actual.getProperties().getRvvCode(), expectedRvvCode);
    }
}