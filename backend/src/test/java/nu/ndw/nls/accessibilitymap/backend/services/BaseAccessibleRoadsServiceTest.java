package nu.ndw.nls.accessibilitymap.backend.services;

import static nu.ndw.nls.accessibilitymap.backend.services.TestHelper.ACCESSIBLE_MATCH;
import static nu.ndw.nls.accessibilitymap.backend.services.TestHelper.MUNICIPALITY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Set;
import nu.ndw.nls.routingmapmatcher.domain.AccessibilityMap;
import nu.ndw.nls.routingmapmatcher.domain.MapMatcherFactory;
import nu.ndw.nls.routingmapmatcher.domain.model.IsochroneMatch;
import nu.ndw.nls.routingmapmatcher.domain.model.accessibility.AccessibilityRequest;
import nu.ndw.nls.routingmapmatcher.graphhopper.NetworkGraphHopper;
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
    private MapMatcherFactory<AccessibilityMap> accessibilityMapFactory;
    @Mock
    private NetworkGraphHopper networkGraphHopper;
    @Mock
    private AccessibilityMap accessibilityMap;

    @InjectMocks
    private BaseAccessibleRoadsService baseAccessibleRoadsService;

    @Test
    void getBaseAccessibleRoadsByMunicipality_ok() {
        when(accessibilityMapFactory.createMapMatcher(networkGraphHopper))
                .thenReturn(accessibilityMap);

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
