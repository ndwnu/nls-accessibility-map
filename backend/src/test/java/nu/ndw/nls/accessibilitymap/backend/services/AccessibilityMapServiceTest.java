package nu.ndw.nls.accessibilitymap.backend.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.SortedMap;
import nu.ndw.nls.accessibilitymap.backend.graphhopper.AccessibilityMap;
import nu.ndw.nls.accessibilitymap.backend.graphhopper.factory.AccessibilityMapFactory;
import nu.ndw.nls.accessibilitymap.backend.model.Municipality;
import nu.ndw.nls.accessibilitymap.backend.model.RoadSection;
import nu.ndw.nls.accessibilitymap.backend.model.VehicleProperties;
import nu.ndw.nls.routingmapmatcher.model.IsochroneMatch;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityMapServiceTest {

    private static final String MUNICIPALITY_ID_STRING = "GM307";

    private static final int ID_1 = 1;
    private static final int ID_2 = 2;

    private static final IsochroneMatch INACCESSIBLE_MATCH = IsochroneMatch.builder()
            .reversed(false)
            .matchedLinkId(ID_2)
            .build();
    private static final IsochroneMatch ACCESSIBLE_MATCH = IsochroneMatch.builder()
            .reversed(false)
            .matchedLinkId(ID_1)
            .build();

    @Mock
    private AccessibilityMapFactory accessibilityMapFactory;
    @Mock
    private NetworkGraphHopper networkGraphHopper;
    @Mock
    private MunicipalityService municipalityService;
    @Mock
    private AccessibleRoadsService accessibleRoadsService;

    @Mock
    private AccessibilityMap accessibilityMap;
    @Mock
    private Municipality municipality;
    @Mock
    private VehicleProperties vehicleProperties;

    @InjectMocks
    private AccessibilityMapService accessibilityMapService;

    @Test
    void determineAccessibilityByRoadSection_ok() {
        Set<IsochroneMatch> allIsochroneMatchSet = Set.of(ACCESSIBLE_MATCH, INACCESSIBLE_MATCH);
        Set<IsochroneMatch> restrictedIsochroneMatchSet = Set.of(INACCESSIBLE_MATCH);

        when(accessibilityMapFactory.createMapMatcher(networkGraphHopper)).thenReturn(accessibilityMap);
        when(municipalityService.getMunicipalityById(MUNICIPALITY_ID_STRING)).thenReturn(municipality);
        when(accessibleRoadsService.getBaseAccessibleRoadsByMunicipality(accessibilityMap, municipality))
                .thenReturn(allIsochroneMatchSet);
        when(accessibleRoadsService.getVehicleAccessibleRoadsByMunicipality(accessibilityMap, vehicleProperties,
                municipality)).thenReturn(restrictedIsochroneMatchSet);

        SortedMap<Integer, RoadSection> idToRoadSections = accessibilityMapService
                .determineAccessibilityByRoadSection(vehicleProperties, MUNICIPALITY_ID_STRING);

        assertThat(idToRoadSections).hasSize(2)
                .containsEntry(ID_1, new RoadSection(ID_1, null, false, null))
                .containsEntry(ID_2, new RoadSection(ID_2, null, true, null));
    }
}
