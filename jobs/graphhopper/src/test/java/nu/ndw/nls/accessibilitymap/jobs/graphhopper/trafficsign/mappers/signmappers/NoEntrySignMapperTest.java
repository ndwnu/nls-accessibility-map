package nu.ndw.nls.accessibilitymap.jobs.graphhopper.trafficsign.mappers.signmappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import nu.ndw.nls.accessibilitymap.jobs.graphhopper.trafficsign.mappers.signmappers.SignMapper.DtoSetter;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.DirectionType;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignAccessibilityDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignPropertiesDto;
import nu.ndw.nls.routingmapmatcher.network.model.DirectionalDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NoEntrySignMapperTest {

    private static final String RVV_CODE = "C17";
    private static final String OTHER_RVV_CODE = "C18";
    private static final DirectionalDto<Boolean> NO_RESTRICTIONS = new DirectionalDto<>(false);
    @Mock
    private DtoSetter<Boolean> dtoSetter;
    @Mock
    private TrafficSignAccessibilityDto trafficSignAccessibilityDto;
    @Captor
    private ArgumentCaptor<DirectionalDto<Boolean>> setValueCaptor;

    private NoEntrySignMapper maximumSignToDtoMapper;

    @BeforeEach
    void setup() {
        this.maximumSignToDtoMapper = new NoEntrySignMapper(RVV_CODE, dtoSetter);
    }

    @Test
    void getRvvCodesUsed_ok_allValuesPresent() {
        assertEquals(RVV_CODE, maximumSignToDtoMapper.getRvvCode());
    }

    @Test
    void addToDto_ok_forward() {
        DirectionalDto<Boolean> expectedDirectional = DirectionalDto.<Boolean>builder()
                .forward(true)
                .reverse(false)
                .build();

        testMapping(expectedDirectional, List.of(DirectionType.FORTH));
    }

    @Test
    void addToDto_ok_reverse() {
        DirectionalDto<Boolean> expectedDirectional = DirectionalDto.<Boolean>builder()
                .forward(false)
                .reverse(true)
                .build();

        testMapping(expectedDirectional, List.of(DirectionType.BACK));
    }

    @Test
    void addToDto_ok_bothWays() {
        DirectionalDto<Boolean> expectedDirectional = new DirectionalDto<>(true);
        testMapping(expectedDirectional, List.of(DirectionType.BOTH));
    }

    @Test
    void addToDto_ok_multipleSigns() {
        DirectionalDto<Boolean> expectedDirectional = new DirectionalDto<>(true);
        testMapping(expectedDirectional, List.of(DirectionType.FORTH, DirectionType.BOTH));
    }

    @Test
    void addToDto_ok_noSigns() {
        testMapping(NO_RESTRICTIONS, Map.of(OTHER_RVV_CODE, List.of(mock(TrafficSignGeoJsonDto.class))));
    }

    private void testMapping(DirectionalDto<Boolean> expected, List<DirectionType> directions) {
        var trafficSigns = directions.stream().map(this::mockSign).toList();
        Map<String, List<TrafficSignGeoJsonDto>> trafficSignMap = Map.of(RVV_CODE, trafficSigns);
        testMapping(expected, trafficSignMap);
    }

    private void testMapping(DirectionalDto<Boolean> expected,
            Map<String, List<TrafficSignGeoJsonDto>> trafficSignMap) {
        maximumSignToDtoMapper.addToDto(trafficSignAccessibilityDto, trafficSignMap);

        verify(dtoSetter).set(eq(trafficSignAccessibilityDto), setValueCaptor.capture());
        assertEquals(expected, setValueCaptor.getValue());
    }

    private TrafficSignGeoJsonDto mockSign(DirectionType direction) {
        TrafficSignGeoJsonDto sign = mock(TrafficSignGeoJsonDto.class);
        TrafficSignPropertiesDto trafficSignPropertiesDto = mock(TrafficSignPropertiesDto.class);

        when(trafficSignPropertiesDto.getDrivingDirection()).thenReturn(direction);
        when(sign.getProperties()).thenReturn(trafficSignPropertiesDto);
        return sign;
    }

}