package nu.ndw.nls.accessibilitymap.backend.services;

import static nu.ndw.nls.accessibilitymap.backend.services.TestHelper.ACCESSIBLE_MATCH;
import static nu.ndw.nls.accessibilitymap.backend.services.TestHelper.MUNICIPALITY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Set;
import nu.ndw.nls.accessibilitymap.backend.graphhopper.AccessibilityMap;
import nu.ndw.nls.accessibilitymap.backend.graphhopper.factory.AccessibilityMapFactory;
import nu.ndw.nls.accessibilitymap.backend.model.AccessibilityRequest;
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
class BaseAccessibleRoadsServiceTest {

    @Captor
    private ArgumentCaptor<AccessibilityRequest> accessibilityRequestArgumentCaptor;

    @Mock
    private AccessibilityMapFactory accessibilityMapFactory;
    @Mock
    private NetworkGraphHopper networkGraphHopper;
    @Mock
    private AccessibilityMap accessibilityMap;

    @InjectMocks
    private BaseAccessibleRoadsService baseAccessibleRoadsService;

    @Test
    void getBaseAccessibleRoadsByMunicipality_ok() {

        when(accessibilityMapFactory.createMapMatcher(networkGraphHopper)).thenReturn(accessibilityMap);

        when(accessibilityMap
                .getAccessibleRoadSections(accessibilityRequestArgumentCaptor.capture()))
                .thenReturn(Set.of(ACCESSIBLE_MATCH));

        Set<IsochroneMatch> isochroneMatches = baseAccessibleRoadsService.getBaseAccessibleRoadsByMunicipality(
                MUNICIPALITY);

        AccessibilityRequest expectedAccessibilityRequest = AccessibilityRequest
                .builder()
                .startPoint(MUNICIPALITY.getStartPoint())
                .searchDistanceInMetres(MUNICIPALITY.getSearchDistanceInMetres())
                .municipalityId(MUNICIPALITY.getMunicipalityIdAsInteger())
                .build();
        AccessibilityRequest accessibilityRequest = accessibilityRequestArgumentCaptor.getValue();
        assertThat(isochroneMatches).isEqualTo(Set.of(ACCESSIBLE_MATCH));
        assertThat(accessibilityRequest).isEqualTo(expectedAccessibilityRequest);
    }
}
