package nu.ndw.nls.accessibilitymap.accessibility.nwb.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.mapper.AccessibilityNwbRoadSectionMapper;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityRoadSectionsServiceTest {

    private static final int VERSION = 20241231;

    private static final int MUNICIPALITY_ID = 1234;

    private AccessibilityRoadSectionsService accessibilityRoadSectionsService;

    @Mock
    private NwbRoadSectionService nwbRoadSectionService;

    @Mock
    private AccessibilityNwbRoadSectionMapper accessibleRoadSectionMapper;

    @Mock
    private NwbRoadSectionDto nwbRoadSectionDto;

    @Mock
    private AccessibilityNwbRoadSection accessibilityRoadSection;

    @BeforeEach
    void setUp() {

        accessibilityRoadSectionsService = new AccessibilityRoadSectionsService(nwbRoadSectionService, accessibleRoadSectionMapper);
    }

    @Test
    void getRoadSections() {

        when(nwbRoadSectionService.findLazyCar(VERSION, Collections.emptySet())).thenReturn(Stream.of(nwbRoadSectionDto));
        when(accessibleRoadSectionMapper.map(nwbRoadSectionDto)).thenReturn(accessibilityRoadSection);

        assertEquals(List.of(accessibilityRoadSection),
                accessibilityRoadSectionsService.getRoadSections(VERSION));

        // Second time to test if the response is cached
        assertEquals(List.of(accessibilityRoadSection),
                accessibilityRoadSectionsService.getRoadSections(VERSION));

        // Response should be cached by municipality id, actual database call should only occur once
        verify(nwbRoadSectionService).findLazyCar(VERSION, Collections.emptySet());
    }

    @Test
    void getRoadSectionsByMunicipalityId() {

        when(nwbRoadSectionService.findLazyCar(VERSION, Collections.singleton(MUNICIPALITY_ID))).thenReturn(Stream.of(nwbRoadSectionDto));
        when(accessibleRoadSectionMapper.map(nwbRoadSectionDto)).thenReturn(accessibilityRoadSection);

        assertEquals(List.of(accessibilityRoadSection),
                accessibilityRoadSectionsService.getRoadSectionsByMunicipalityId(VERSION, MUNICIPALITY_ID));

        // Second time to test if the response is cached
        assertEquals(List.of(accessibilityRoadSection),
                accessibilityRoadSectionsService.getRoadSectionsByMunicipalityId(VERSION, MUNICIPALITY_ID));

        // Response should be cached by municipality id, actual database call should only occur once
        verify(nwbRoadSectionService).findLazyCar(VERSION, Collections.singleton(MUNICIPALITY_ID));
    }
}