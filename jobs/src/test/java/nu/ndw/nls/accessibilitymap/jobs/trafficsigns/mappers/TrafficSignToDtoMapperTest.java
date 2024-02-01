package nu.ndw.nls.accessibilitymap.jobs.trafficsigns.mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos.TextSignJsonDtoV3;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos.TrafficSignAccessibilityDto;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos.TrafficSignJsonDtoV3;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.mappers.signmappers.SignMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrafficSignToDtoMapperTest {

    private static final String CODE_A = "A";
    private static final String CODE_B = "B1";
    private static final String CODE_C = "C";

    @Captor
    ArgumentCaptor<TrafficSignAccessibilityDto> dtoCaptor;
    @Captor
    ArgumentCaptor<Map<String,List<TrafficSignJsonDtoV3>>> trafficSignCaptor;

    @Mock
    private SignMapper<String> mapperA;
    @Mock
    private SignMapper<String> mapperB;
    @Mock
    private SignMapper<String> mapperC;
    @Mock
    private TrafficSignMapperRegistry mapperRegistry;

    @Test
    void getRvvCodesUsed_ok() {
        when(mapperA.getRvvCode()).thenReturn(CODE_A);
        when(mapperB.getRvvCode()).thenReturn(CODE_B);
        when(mapperC.getRvvCode()).thenReturn(CODE_C);

        when(mapperRegistry.getMappers()).thenReturn(List.of(mapperA, mapperB, mapperC));
        TrafficSignToDtoMapper trafficSignToDtoMapper = new TrafficSignToDtoMapper(mapperRegistry);
        Set<String> result = trafficSignToDtoMapper.getRvvCodesUsed();

        assertThat(result).containsExactlyInAnyOrder(CODE_A, CODE_B, CODE_C);
    }

    @Test
    void constructor_exception_duplicateCodes() {
        when(mapperA.getRvvCode()).thenReturn(CODE_A);
        when(mapperB.getRvvCode()).thenReturn(CODE_B);
        when(mapperC.getRvvCode()).thenReturn(CODE_A);

        when(mapperRegistry.getMappers()).thenReturn(List.of(mapperA, mapperB, mapperC));
        assertThrows(IllegalArgumentException.class, () -> new TrafficSignToDtoMapper(mapperRegistry));
    }

    @Test
    void map_ok() {
        when(mapperA.getRvvCode()).thenReturn(CODE_A);
        when(mapperB.getRvvCode()).thenReturn(CODE_B);
        when(mapperC.getRvvCode()).thenReturn(CODE_C);

        TrafficSignJsonDtoV3 excludedSignA = mockSign("UIT");
        TrafficSignJsonDtoV3 excludedSignB = mockSign("VOOR");
        TrafficSignJsonDtoV3 excludedSignC = mockSign("TIJD");
        TrafficSignJsonDtoV3 includedSignA = mockSign("VRIJ");
        TrafficSignJsonDtoV3 includedSignB = mockSign(null);
        TrafficSignJsonDtoV3 includedSignC = Mockito.mock(TrafficSignJsonDtoV3.class);

        when(includedSignA.getRvvCode()).thenReturn(CODE_A);
        when(includedSignB.getRvvCode()).thenReturn(CODE_B);
        when(includedSignC.getRvvCode()).thenReturn(CODE_B);

        doNothing().when(mapperA).addToDto(dtoCaptor.capture(), trafficSignCaptor.capture());
        doNothing().when(mapperB).addToDto(dtoCaptor.capture(), trafficSignCaptor.capture());
        doNothing().when(mapperC).addToDto(dtoCaptor.capture(), trafficSignCaptor.capture());

        when(mapperRegistry.getMappers()).thenReturn(List.of(mapperA, mapperB, mapperC));
        TrafficSignToDtoMapper trafficSignToDtoMapper = new TrafficSignToDtoMapper(mapperRegistry);
        TrafficSignAccessibilityDto result = trafficSignToDtoMapper.map(
                List.of(excludedSignA, excludedSignB, excludedSignC, includedSignA, includedSignB, includedSignC));

        assertThat(dtoCaptor.getAllValues()).allMatch(result::equals);
        trafficSignCaptor.getAllValues()
                .forEach(capturedSigns -> assertThat(capturedSigns).hasSize(2)
                        .containsEntry(CODE_A, List.of(includedSignA))
                        .containsEntry(CODE_B, List.of(includedSignB, includedSignC)));
    }

    private TrafficSignJsonDtoV3 mockSign(String textSignType) {
        TrafficSignJsonDtoV3 mockSign = Mockito.mock(TrafficSignJsonDtoV3.class);
        TextSignJsonDtoV3 mockTextSign = Mockito.mock(TextSignJsonDtoV3.class);
        when(mockSign.getTextSigns()).thenReturn(List.of(mockTextSign));
        when(mockTextSign.getType()).thenReturn(textSignType);
        return mockSign;
    }

}
