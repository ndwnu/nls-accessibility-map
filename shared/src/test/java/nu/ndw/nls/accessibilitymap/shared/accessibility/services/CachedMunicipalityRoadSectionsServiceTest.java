package nu.ndw.nls.accessibilitymap.shared.accessibility.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import nu.ndw.nls.accessibilitymap.shared.accessibility.mappers.CachedRoadSectionMapper;
import nu.ndw.nls.accessibilitymap.shared.accessibility.model.CachedRoadSection;
import nu.ndw.nls.accessibilitymap.shared.network.dtos.AccessibilityGraphhopperMetaData;
import nu.ndw.nls.accessibilitymap.shared.nwb.services.NwbRoadSectionService;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CachedMunicipalityRoadSectionsServiceTest {

    private static final int VERSION = 20241231;
    private static final int MUNICIPALITY_ID = 1234;
    @Mock
    private NwbRoadSectionService nwbRoadSectionService;
    @Mock
    private CachedRoadSectionMapper cachedRoadSectionMapper;
    @Mock
    private AccessibilityGraphhopperMetaData accessibilityGraphhopperMetaData;

    @InjectMocks
    private CachedMunicipalityRoadSectionsService cachedMunicipalityRoadSectionsService;

    @Mock
    private NwbRoadSectionDto nwbRoadSectionDtoA;
    @Mock
    private NwbRoadSectionDto nwbRoadSectionDtoB;
    @Mock
    private CachedRoadSection cachedRoadSectionA;
    @Mock
    private CachedRoadSection cachedRoadSectionB;

    @Test
    void getRoadSectionIdToRoadSection() {
        when(accessibilityGraphhopperMetaData.nwbVersion()).thenReturn(VERSION);
        when(nwbRoadSectionService.findLazyCar(VERSION, Collections.singleton(MUNICIPALITY_ID)))
                .thenReturn(Stream.of(nwbRoadSectionDtoA, nwbRoadSectionDtoB));
        when(cachedRoadSectionMapper.map(nwbRoadSectionDtoA)).thenReturn(cachedRoadSectionA);
        when(cachedRoadSectionMapper.map(nwbRoadSectionDtoB)).thenReturn(cachedRoadSectionB);

        assertEquals(List.of(cachedRoadSectionA, cachedRoadSectionB),
                cachedMunicipalityRoadSectionsService.getRoadSectionIdToRoadSection(MUNICIPALITY_ID));

        // Second time to test if the response is cached
        assertEquals(List.of(cachedRoadSectionA, cachedRoadSectionB),
                cachedMunicipalityRoadSectionsService.getRoadSectionIdToRoadSection(MUNICIPALITY_ID));

        // Response should be cached by municipality id, actual database call should only occur once
        verify(nwbRoadSectionService, times(1)).findLazyCar(VERSION, Collections.singleton(MUNICIPALITY_ID));
    }
}