package nu.ndw.nls.accessibilitymap.accessibility.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.SortedMap;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.GraphHopperNetwork;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.network.GraphhopperMetaData;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service.NetworkMetaDataService;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.service.AccessibilityNwbRoadSectionService;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityContextProviderTest {

    private AccessibilityContextProvider accessibilityContextProvider;

    @Mock
    private GraphHopperService graphHopperService;

    @Mock
    private AccessibilityNwbRoadSectionService accessibilityNwbRoadSectionService;

    @Mock
    private NetworkMetaDataService networkMetaDataService;

    @Mock
    private SortedMap<Long, AccessibilityNwbRoadSection> roadSectionsById;

    @Mock
    private GraphhopperMetaData graphhopperMetaData;

    @Mock
    private GraphHopperNetwork graphHopperNetwork;

    @BeforeEach
    void setUp() {

        accessibilityContextProvider = new AccessibilityContextProvider(
                graphHopperService,
                accessibilityNwbRoadSectionService,
                networkMetaDataService);
    }

    @Test
    void get() {

        when(accessibilityNwbRoadSectionService.getRoadSectionsByIdForNwbVersion(1)).thenReturn(roadSectionsById);
        when(networkMetaDataService.loadMetaData()).thenReturn(graphhopperMetaData);
        when(graphhopperMetaData.nwbVersion()).thenReturn(1);
        when(graphHopperService.getNetworkGraphHopper()).thenReturn(graphHopperNetwork);

        AccessibilityContext accessibilityContext = accessibilityContextProvider.get();

        assertThat(accessibilityContext).isNotNull();
        assertThat(accessibilityContext.nwbVersionId()).isEqualTo(1);
        assertThat(accessibilityContext.graphHopperNetwork()).isEqualTo(graphHopperNetwork);
        assertThat(accessibilityContext.accessibilityNwbRoadSections()).isEqualTo(roadSectionsById);
    }
}
