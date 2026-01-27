package nu.ndw.nls.accessibilitymap.accessibility.nwb.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.locationtech.jts.geom.LineString;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityNwbRoadSectionMapperTest {

    private AccessibilityNwbRoadSectionMapper accessibleRoadSectionMapper;

    @Mock
    private NwbRoadSectionDto nwbRoadSectionDto;

    @Mock
    private LineString geometry;

    @BeforeEach
    void setUp() {
        accessibleRoadSectionMapper = new AccessibilityNwbRoadSectionMapper();
    }

    @ParameterizedTest
    @CsvSource({
            "H, true, false",
            "T, false, true",
            "B, true, true",
            "O, true, true"
    })
    void map_reverseOnly(String drivingDirection, boolean forward, boolean backward) {

        when(nwbRoadSectionDto.getRoadSectionId()).thenReturn(1L);
        when(nwbRoadSectionDto.getJunctionIdFrom()).thenReturn(2L);
        when(nwbRoadSectionDto.getJunctionIdTo()).thenReturn(3L);
        when(nwbRoadSectionDto.getMunicipalityId()).thenReturn(4);
        when(nwbRoadSectionDto.getDrivingDirection()).thenReturn(drivingDirection);
        when(nwbRoadSectionDto.getGeometry()).thenReturn(geometry);

        AccessibilityNwbRoadSection accessibilityNwbRoadSection = accessibleRoadSectionMapper.map(nwbRoadSectionDto);
        assertThat(accessibilityNwbRoadSection)
                .isEqualTo(new AccessibilityNwbRoadSection(1, 2, 3, 4, geometry, forward, backward));
    }
}
