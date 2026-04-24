package nu.ndw.nls.accessibilitymap.backend.nwb.messaging.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import nu.ndw.nls.accessibilitymap.accessibility.network.NetworkDataService;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSectionUpdate;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbData;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.messaging.dto.DrivingDirection;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.messaging.dto.NwbRoadSectionUpdate;
import nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NwbRoadSectionUpdateMapperTest {

    private static final int ROAD_SECTION_ID = 1;

    @Mock
    private NetworkDataService networkDataService;

    @Mock
    private NetworkData networkData;

    @Mock
    private NwbData nwbData;

    @Mock
    private AccessibilityNwbRoadSection accessibilityNwbRoadSection;

    private NwbRoadSectionUpdateMapper nwbRoadSectionUpdateMapper;

    @BeforeEach
    void setUp() {
        nwbRoadSectionUpdateMapper = new NwbRoadSectionUpdateMapper(networkDataService);
    }

    @Test
    void map_all_values_are_present() {
        NwbRoadSectionUpdate nwbRoadSectionUpdate = NwbRoadSectionUpdate.builder()
                .roadSectionId(ROAD_SECTION_ID)
                .carriagewayTypeCode(CarriagewayTypeCode.RB)
                .drivingDirection(DrivingDirection.BOTH)
                .build();
        when(networkDataService.get()).thenReturn(networkData);
        when(networkData.getNwbData()).thenReturn(nwbData);
        when(nwbData.findAccessibilityNwbRoadSectionById(ROAD_SECTION_ID)).thenReturn(java.util.Optional.of(accessibilityNwbRoadSection));

        AccessibilityNwbRoadSectionUpdate expected = new AccessibilityNwbRoadSectionUpdate(ROAD_SECTION_ID,
                true,
                true,
                CarriagewayTypeCode.RB);

        verifyNoInteractions(accessibilityNwbRoadSection);
        assertThat(nwbRoadSectionUpdateMapper.map(nwbRoadSectionUpdate)).isEqualTo(expected);
    }

    @Test
    void map_optional_values_are_not_present() {
        NwbRoadSectionUpdate nwbRoadSectionUpdate = NwbRoadSectionUpdate.builder()
                .roadSectionId(ROAD_SECTION_ID)
                .build();
        when(networkDataService.get()).thenReturn(networkData);
        when(networkData.getNwbData()).thenReturn(nwbData);
        when(nwbData.findAccessibilityNwbRoadSectionById(ROAD_SECTION_ID)).thenReturn(java.util.Optional.of(accessibilityNwbRoadSection));
        when(accessibilityNwbRoadSection.carriagewayTypeCode()).thenReturn(CarriagewayTypeCode.RB);
        when(accessibilityNwbRoadSection.backwardAccessible()).thenReturn(true);
        when(accessibilityNwbRoadSection.forwardAccessible()).thenReturn(false);

        AccessibilityNwbRoadSectionUpdate expected = new AccessibilityNwbRoadSectionUpdate(ROAD_SECTION_ID,
                false,
                true,
                CarriagewayTypeCode.RB);
        assertThat(nwbRoadSectionUpdateMapper.map(nwbRoadSectionUpdate)).isEqualTo(expected);
    }

    @Test
    void map_no_road_section_found_exception() {
        NwbRoadSectionUpdate nwbRoadSectionUpdate = NwbRoadSectionUpdate.builder()
                .roadSectionId(ROAD_SECTION_ID)
                .build();
        when(networkDataService.get()).thenReturn(networkData);
        when(networkData.getNwbData()).thenReturn(nwbData);
        when(nwbData.findAccessibilityNwbRoadSectionById(ROAD_SECTION_ID)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> nwbRoadSectionUpdateMapper.map(nwbRoadSectionUpdate)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Road update road-section: 1 is not present is active version");
    }
}
