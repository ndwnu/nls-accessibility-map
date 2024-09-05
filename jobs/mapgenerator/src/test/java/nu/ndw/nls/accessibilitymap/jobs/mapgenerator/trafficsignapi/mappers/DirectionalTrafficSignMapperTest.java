package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.trafficsignapi.mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.TrafficSignType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.directional.Direction;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.directional.DirectionalTrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.services.TextSignFilterService;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignPropertiesDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DirectionalTrafficSignMapperTest {

    private static final String TRAFFIC_SIGN_API_RVV_CODE = "C6";
    private static final long ROAD_SECTION_ID = 124L;
    private static final TrafficSignType TRAFFIC_SIGN_TYPE = TrafficSignType.C6;
    private static final String TEXT = "TEXT";
    private static final double FRACTION_FORWARD = 0.2;

    @Mock
    private TrafficSignApiRvvCodeMapper trafficSignApiRvvCodeMapper;

    @Mock
    private TextSignFilterService textSignFilterService;

    @InjectMocks
    private DirectionalTrafficSignMapper directionalTrafficSignMapper;

    @Mock
    private TrafficSignGeoJsonDto trafficSignGeoJsonDto;

    @Mock
    private TrafficSignPropertiesDto trafficSignPropertiesDto;

    @Mock
    private List<TextSignDto> textSignDtos;

    @Mock
    private TextSignDto firstWindowSignTextSignDto;


    @Test
    void map_ok() {
        when(trafficSignGeoJsonDto.getProperties()).thenReturn(trafficSignPropertiesDto);
        when(trafficSignPropertiesDto.getRoadSectionId()).thenReturn(ROAD_SECTION_ID);
        when(trafficSignPropertiesDto.getRvvCode()).thenReturn(TRAFFIC_SIGN_API_RVV_CODE);
        when(trafficSignPropertiesDto.getTextSigns()).thenReturn(textSignDtos);
        when(trafficSignPropertiesDto.getFraction()).thenReturn(FRACTION_FORWARD);


        when(trafficSignApiRvvCodeMapper.map(TRAFFIC_SIGN_API_RVV_CODE)).thenReturn(TRAFFIC_SIGN_TYPE);
        when(textSignFilterService.findFirstWindowTimeTextSign(textSignDtos))
                .thenReturn(Optional.of(firstWindowSignTextSignDto));

        when(firstWindowSignTextSignDto.getText()).thenReturn(TEXT);

        assertThat(directionalTrafficSignMapper.map(trafficSignGeoJsonDto, Direction.FORWARD))
                .isEqualTo(DirectionalTrafficSign.builder()
                    .direction(Direction.FORWARD)
                    .nwbRoadSectionId(ROAD_SECTION_ID)
                    .trafficSignType(TRAFFIC_SIGN_TYPE)
                    .windowTimes(TEXT)
                    .nwbFraction(FRACTION_FORWARD)
                    .direction(Direction.FORWARD)
                    .build());
    }


}