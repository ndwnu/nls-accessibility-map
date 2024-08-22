package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.trafficsignapi.mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.TrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.TrafficSignType;
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
class TrafficSignMapperTest {

    private static final String TRAFFIC_SIGN_API_RVV_CODE = "C6";
    private static final long ROAD_SECTION_ID = 124L;
    private static final TrafficSignType TRAFFIC_SIGN_TYPE = TrafficSignType.C6;
    private static final String TEXT = "TEXT";

    @Mock
    private TrafficSignApiRvvCodeMapper trafficSignApiRvvCodeMapper;

    @Mock
    private TextSignFilterService textSignFilterService;

    @InjectMocks
    private TrafficSignMapper trafficSignMapper;

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

        when(trafficSignApiRvvCodeMapper.map(TRAFFIC_SIGN_API_RVV_CODE)).thenReturn(TRAFFIC_SIGN_TYPE);
        when(textSignFilterService.findFirstWindowTimeTextSign(textSignDtos))
                .thenReturn(Optional.of(firstWindowSignTextSignDto));

        when(firstWindowSignTextSignDto.getText()).thenReturn(TEXT);

        assertThat(trafficSignMapper.map(trafficSignGeoJsonDto)).isEqualTo(TrafficSign.builder()
                .roadSectionId(ROAD_SECTION_ID)
                .trafficSignType(TRAFFIC_SIGN_TYPE)
                .windowTimes(TEXT)
                .build());
    }
}