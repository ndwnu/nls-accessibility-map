package nu.ndw.nls.accessibilitymap.backend.services;

import static nu.ndw.nls.accessibilitymap.backend.services.TestHelper.ACCESSIBLE_MATCH;
import static nu.ndw.nls.accessibilitymap.backend.services.TestHelper.ID_1;
import static nu.ndw.nls.accessibilitymap.backend.services.TestHelper.ID_2;
import static nu.ndw.nls.accessibilitymap.backend.services.TestHelper.INACCESSIBLE_MATCH;
import static nu.ndw.nls.accessibilitymap.backend.services.TestHelper.MUNICIPALITY;
import static nu.ndw.nls.accessibilitymap.backend.services.TestHelper.MUNICIPALITY_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.SortedMap;
import nu.ndw.nls.accessibilitymap.backend.graphhopper.AccessibilityMap;
import nu.ndw.nls.accessibilitymap.backend.graphhopper.factory.AccessibilityMapFactory;
import nu.ndw.nls.accessibilitymap.backend.model.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.backend.model.RoadSection;
import nu.ndw.nls.accessibilitymap.backend.model.VehicleProperties;
import nu.ndw.nls.routingmapmatcher.model.IsochroneMatch;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityMapServiceTest {

    @Captor
    private ArgumentCaptor<AccessibilityRequest> accessibilityRequestArgumentCaptor;
    @Mock
    private AccessibilityMapFactory accessibilityMapFactory;
    @Mock
    private NetworkGraphHopper networkGraphHopper;
    @Mock
    private MunicipalityService municipalityService;
    @Mock
    private BaseAccessibleRoadsService baseIsochroneService;
    @Mock
    private AccessibilityMap accessibilityMap;

    @InjectMocks
    private AccessibilityMapService accessibilityMapService;

    @Test
    void determineAccessibilityByRoadSection_ok() {
        Set<IsochroneMatch> allIsochroneMatchSet = Set.of(ACCESSIBLE_MATCH, INACCESSIBLE_MATCH);
        Set<IsochroneMatch> restrictedIsochroneMatchSet = Set.of(INACCESSIBLE_MATCH);

        when(accessibilityMapFactory.createMapMatcher(networkGraphHopper)).thenReturn(accessibilityMap);
        when(municipalityService.getMunicipalityById(MUNICIPALITY_ID)).thenReturn(MUNICIPALITY);
        when(baseIsochroneService.getBaseAccessibleRoadsByMunicipality(MUNICIPALITY)).thenReturn(allIsochroneMatchSet);
        when(accessibilityMap.getAccessibleRoadSections(accessibilityRequestArgumentCaptor.capture()))
                .thenReturn(restrictedIsochroneMatchSet);

        VehicleProperties vehicleProperties = VehicleProperties.builder().build();

        SortedMap<Integer, RoadSection> idToRoadSections = accessibilityMapService
                .determineAccessibilityByRoadSection(vehicleProperties, MUNICIPALITY_ID);
        AccessibilityRequest accessibilityRequest = accessibilityRequestArgumentCaptor.getValue();
        AccessibilityRequest expectedAccessibilityRequest = AccessibilityRequest.builder()
                .startPoint(MUNICIPALITY.getStartPoint())
                .searchDistanceInMetres(MUNICIPALITY.getSearchDistanceInMetres())
                .municipalityId(MUNICIPALITY.getMunicipalityIdAsInteger())
                .vehicleProperties(vehicleProperties)
                .build();

        assertThat(idToRoadSections).hasSize(2)
                .containsEntry(ID_1, new RoadSection(ID_1, false, null))
                .containsEntry(ID_2, new RoadSection(ID_2, true, null));
        assertThat(accessibilityRequest)
                .hasFieldOrPropertyWithValue("startPoint", MUNICIPALITY.getStartPoint())
                .isEqualTo(expectedAccessibilityRequest);
    }
}
