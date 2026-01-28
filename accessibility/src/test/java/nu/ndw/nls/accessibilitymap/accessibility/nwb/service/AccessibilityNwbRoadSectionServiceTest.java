package nu.ndw.nls.accessibilitymap.accessibility.nwb.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperService;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.mapper.AccessibilityNwbRoadSectionMapper;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode;
import nu.ndw.nls.db.nwb.jooq.services.NwbRoadSectionCrudService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityNwbRoadSectionServiceTest {

    private AccessibilityNwbRoadSectionService accessibilityNwbRoadSectionService;

    @Mock
    private NwbRoadSectionCrudService nwbRoadSectionCrudService;

    @Mock
    private AccessibilityNwbRoadSectionMapper accessibilityNwbRoadSectionMapper;

    @Mock
    private GraphHopperService graphHopperService;

    @Mock
    private NwbRoadSectionDto nwbRoadSectionDto1;

    @Mock
    private AccessibilityNwbRoadSection accessibilityNwbRoadSection1;

    @Mock
    private NwbRoadSectionDto nwbRoadSectionDto2;

    @Mock
    private AccessibilityNwbRoadSection accessibilityNwbRoadSection2;

    @Captor
    private ArgumentCaptor<Runnable> updateListenerCaptor;

    private Set<CarriagewayTypeCode> carriageWayTypeCodeInclusions;

    private Runnable clearCache;

    @BeforeEach
    void setUp() {

        accessibilityNwbRoadSectionService = new AccessibilityNwbRoadSectionService(
                nwbRoadSectionCrudService,
                accessibilityNwbRoadSectionMapper,
                graphHopperService);

        verify(graphHopperService).registerUpdateListener(updateListenerCaptor.capture());
        clearCache = updateListenerCaptor.getValue();

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
    void findAllByVersion() {

        when(nwbRoadSectionCrudService.findLazyByVersionIdAndCarriageWayTypeCodeAndMunicipality(
                2,
                carriageWayTypeCodeInclusions,
                null,
                250
        )).thenReturn(Stream.of(nwbRoadSectionDto1));
        when(accessibilityNwbRoadSectionMapper.map(nwbRoadSectionDto1)).thenReturn(accessibilityNwbRoadSection1);

        var accessibilityNwbRoadSections = accessibilityNwbRoadSectionService.findAllByVersion(2);
        assertThat(accessibilityNwbRoadSections).containsExactlyInAnyOrder(accessibilityNwbRoadSection1);

        var accessibilityNwbRoadSections2 = accessibilityNwbRoadSectionService.findAllByVersion(2);
        assertThat(accessibilityNwbRoadSections2).containsExactlyInAnyOrder(accessibilityNwbRoadSection1);

        verify(nwbRoadSectionCrudService).findLazyByVersionIdAndCarriageWayTypeCodeAndMunicipality(
                2,
                carriageWayTypeCodeInclusions,
                null,
                250);
    }

    @Test
    void findAllByVersionAndMunicipalityId() {

        when(nwbRoadSectionCrudService.findLazyByVersionIdAndCarriageWayTypeCodeAndMunicipality(
                2,
                carriageWayTypeCodeInclusions,
                null,
                250
        )).thenReturn(Stream.of(nwbRoadSectionDto1, nwbRoadSectionDto2));
        when(accessibilityNwbRoadSectionMapper.map(nwbRoadSectionDto1)).thenReturn(accessibilityNwbRoadSection1);
        when(accessibilityNwbRoadSection1.municipalityId()).thenReturn(null);
        when(accessibilityNwbRoadSectionMapper.map(nwbRoadSectionDto2)).thenReturn(accessibilityNwbRoadSection2);
        when(accessibilityNwbRoadSection2.municipalityId()).thenReturn(3);

        var accessibilityNwbRoadSections = accessibilityNwbRoadSectionService.findAllByVersionAndMunicipalityId(2, 3);
        assertThat(accessibilityNwbRoadSections).containsExactlyInAnyOrder(accessibilityNwbRoadSection2);

        var accessibilityNwbRoadSections2 = accessibilityNwbRoadSectionService.findAllByVersionAndMunicipalityId(2, 3);
        assertThat(accessibilityNwbRoadSections2).containsExactlyInAnyOrder(accessibilityNwbRoadSection2);

        verify(nwbRoadSectionCrudService).findLazyByVersionIdAndCarriageWayTypeCodeAndMunicipality(
                2,
                carriageWayTypeCodeInclusions,
                null,
                250);
    }

    @Test
    void clearCache() {

        when(nwbRoadSectionCrudService.findLazyByVersionIdAndCarriageWayTypeCodeAndMunicipality(
                2,
                carriageWayTypeCodeInclusions,
                null,
                250))
                .thenReturn(Stream.of(nwbRoadSectionDto1, nwbRoadSectionDto2))
                .thenReturn(Stream.of(nwbRoadSectionDto1, nwbRoadSectionDto2));

        when(accessibilityNwbRoadSectionMapper.map(nwbRoadSectionDto1)).thenReturn(accessibilityNwbRoadSection1);
        when(accessibilityNwbRoadSectionMapper.map(nwbRoadSectionDto2)).thenReturn(accessibilityNwbRoadSection2);

        when(accessibilityNwbRoadSection1.municipalityId()).thenReturn(2);
        when(accessibilityNwbRoadSection2.municipalityId()).thenReturn(3);

        assertThat(accessibilityNwbRoadSectionService.findAllByVersionAndMunicipalityId(2, 3))
                .containsExactlyInAnyOrder(accessibilityNwbRoadSection2);
        assertThat(accessibilityNwbRoadSectionService.findAllByVersion(2))
                .containsExactlyInAnyOrder(accessibilityNwbRoadSection1, accessibilityNwbRoadSection2);

        verify(nwbRoadSectionCrudService).findLazyByVersionIdAndCarriageWayTypeCodeAndMunicipality(
                2,
                carriageWayTypeCodeInclusions,
                null,
                250);

        clearCache.run();

        assertThat(accessibilityNwbRoadSectionService.findAllByVersionAndMunicipalityId(2, 3))
                .containsExactlyInAnyOrder(accessibilityNwbRoadSection2);
        assertThat(accessibilityNwbRoadSectionService.findAllByVersion(2))
                .containsExactlyInAnyOrder(accessibilityNwbRoadSection1, accessibilityNwbRoadSection2);

        verify(nwbRoadSectionCrudService, times(2)).findLazyByVersionIdAndCarriageWayTypeCodeAndMunicipality(
                2,
                carriageWayTypeCodeInclusions,
                null,
                250);
    }
}
