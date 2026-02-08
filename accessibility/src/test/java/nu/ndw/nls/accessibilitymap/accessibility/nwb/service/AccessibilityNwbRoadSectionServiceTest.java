package nu.ndw.nls.accessibilitymap.accessibility.nwb.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.mapper.AccessibilityNwbRoadSectionMapper;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode;
import nu.ndw.nls.db.nwb.jooq.services.NwbRoadSectionCrudService;
import nu.ndw.nls.db.nwb.jooq.services.NwbVersionCrudService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityNwbRoadSectionServiceTest {

    private AccessibilityNwbRoadSectionService accessibilityNwbRoadSectionService;

    @Mock
    private NwbRoadSectionCrudService nwbRoadSectionCrudService;

    @Mock
    private NwbVersionCrudService nwbVersionCrudService;

    @Mock
    private AccessibilityNwbRoadSectionMapper accessibilityNwbRoadSectionMapper;

    @Mock
    private NwbRoadSectionDto nwbRoadSectionDto;

    @Mock
    private AccessibilityNwbRoadSection accessibilityNwbRoadSection;

    private Set<CarriagewayTypeCode> carriageWayTypeCodeInclusions;

    @BeforeEach
    void setUp() {

        accessibilityNwbRoadSectionService = new AccessibilityNwbRoadSectionService(
                nwbRoadSectionCrudService,
                nwbVersionCrudService,
                accessibilityNwbRoadSectionMapper);

        carriageWayTypeCodeInclusions = Stream.of(CarriagewayTypeCode.values())
                .filter(carriagewayTypeCode -> !EnumSet.of(
                        CarriagewayTypeCode.BU,
                        CarriagewayTypeCode.BUS,
                        CarriagewayTypeCode.FP,
                        CarriagewayTypeCode.VP,
                        CarriagewayTypeCode.VZ,
                        CarriagewayTypeCode.OVB,
                        CarriagewayTypeCode.CADO,
                        CarriagewayTypeCode.RP,
                        CarriagewayTypeCode.VV,
                        CarriagewayTypeCode.VDF,
                        CarriagewayTypeCode.VDV).contains(carriagewayTypeCode))
                .collect(Collectors.toSet());
        carriageWayTypeCodeInclusions.add(null);
    }

    @Test
    void getLatestNwbData() {

        when(nwbVersionCrudService.findLatestVersionId()).thenReturn(2);
        when(nwbRoadSectionCrudService.findLazyByVersionIdAndCarriageWayTypeCodeAndMunicipality(
                2,
                carriageWayTypeCodeInclusions,
                null,
                250
        )).thenReturn(Stream.of(nwbRoadSectionDto));

        when(accessibilityNwbRoadSectionMapper.map(nwbRoadSectionDto)).thenReturn(accessibilityNwbRoadSection);

        var nwbData = accessibilityNwbRoadSectionService.getLatestNwbData();

        assertThat(nwbData.getNwbVersionId()).isEqualTo(2);
        assertThat(nwbData.findAllAccessibilityNwbRoadSections()).containsExactlyInAnyOrder(accessibilityNwbRoadSection);

        verify(nwbRoadSectionCrudService).findLazyByVersionIdAndCarriageWayTypeCodeAndMunicipality(
                2,
                carriageWayTypeCodeInclusions,
                null,
                250);
    }
}
