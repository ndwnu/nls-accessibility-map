package nu.ndw.nls.accessibilitymap.jobs.graphhopper.trafficsign.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSign;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignType;
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
        testMapping("C6", "C6T", TextSignType.TIME_PERIOD);
        testMapping("C7", "C7T", TextSignType.TIME_PERIOD);
        testMapping("C7b", "C7bT", TextSignType.TIME_PERIOD);
        testMapping("C12", "C12T", TextSignType.TIME_PERIOD);
        testMapping("C22c", "C22cT", TextSignType.TIME_PERIOD);
        testMapping("C20", "C20", TextSignType.EMISSION_ZONE);
        testMapping("C66", "C66T", TextSignType.TIME_PERIOD);
    }

    private void testMapping(String inputRvvCode, String expectedRvvCode, TextSignType signType) {
        TextSign textSignDto = TextSign.builder()
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