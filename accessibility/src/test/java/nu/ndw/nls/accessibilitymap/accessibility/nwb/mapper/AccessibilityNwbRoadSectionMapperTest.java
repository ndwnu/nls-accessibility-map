package nu.ndw.nls.accessibilitymap.accessibility.nwb.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode;
import nu.ndw.nls.routingmapmatcher.network.model.DirectionalDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.LineString;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityNwbRoadSectionMapperTest {

    private AccessibilityNwbRoadSectionMapper accessibleRoadSectionMapper;

    @Mock
    private DrivingDirectionMapper drivingDirectionMapper;

    @Mock
    private NwbRoadSectionDto nwbRoadSectionDto;

    @Mock
    private LineString geometry;

    @BeforeEach
    void setUp() {
        accessibleRoadSectionMapper = new AccessibilityNwbRoadSectionMapper(drivingDirectionMapper);
    }

    @Test
    void map() {
        when(drivingDirectionMapper.map("H"))
                .thenReturn(DirectionalDto.<Boolean>builder()
                        .forward(true)
                        .reverse(false)
                        .build());
        when(nwbRoadSectionDto.getRoadSectionId()).thenReturn(1L);
        when(nwbRoadSectionDto.getJunctionIdFrom()).thenReturn(2L);
        when(nwbRoadSectionDto.getJunctionIdTo()).thenReturn(3L);
        when(nwbRoadSectionDto.getMunicipalityId()).thenReturn(4);
        when(nwbRoadSectionDto.getDrivingDirection()).thenReturn("H");
        when(nwbRoadSectionDto.getGeometry()).thenReturn(geometry);
        when(nwbRoadSectionDto.getFunctionalRoadClass()).thenReturn("1");
        when(nwbRoadSectionDto.getCarriagewayTypeCode()).thenReturn("RB");
        AccessibilityNwbRoadSection accessibilityNwbRoadSection = accessibleRoadSectionMapper.map(nwbRoadSectionDto);
        assertThat(accessibilityNwbRoadSection)
                .isEqualTo(new AccessibilityNwbRoadSection(1, 2, 3, 4, geometry, true, false, CarriagewayTypeCode.RB, "1"));
    }
}
