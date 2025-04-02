package nu.ndw.nls.accessibilitymap.trafficsignclient.dtos;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrafficSignDataTest {

    private static final long ROAD_SECTION_ID_PRESENT = 1L;
    private static final long ROAD_SECTION_ID_NOT_PRESENT = 2L;

    @Mock
    private TrafficSignGeoJsonDto trafficSign;

    @Test
    void getTrafficSignsByRoadSectionId() {
        Map<Long, List<TrafficSignGeoJsonDto>> map = Map.of(ROAD_SECTION_ID_PRESENT, List.of(trafficSign));

        TrafficSignData data = new TrafficSignData(map, null, null);

        assertThat(data.getTrafficSignsByRoadSectionId(ROAD_SECTION_ID_PRESENT)).containsExactly(trafficSign);
        assertThat(data.getTrafficSignsByRoadSectionId(ROAD_SECTION_ID_NOT_PRESENT)).isNotNull().isEmpty();
    }

}