package nu.ndw.nls.accessibilitymap.backend.services;

import static nu.ndw.nls.accessibilitymap.backend.services.TestHelper.MUNICIPALITY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Set;
import nu.ndw.nls.accessibilitymap.backend.graphhopper.AccessibilityMap;
import nu.ndw.nls.accessibilitymap.backend.model.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.backend.model.VehicleProperties;
import nu.ndw.nls.routingmapmatcher.model.IsochroneMatch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibleRoadsServiceTest {

    @Mock
    private AccessibilityMap accessibilityMap;
    @Mock
    private Set<IsochroneMatch> accessibleRoadSections;
    @Mock
    private VehicleProperties vehicleProperties;

    @InjectMocks
    private AccessibleRoadsService accessibleRoadsService;

    @Test
    void getBaseAccessibleRoadsByMunicipality_ok() {
        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .startPoint(MUNICIPALITY.getStartPoint())
                .searchDistanceInMetres(MUNICIPALITY.getSearchDistanceInMetres())
                .municipalityId(MUNICIPALITY.getMunicipalityIdAsInteger())
                .build();
        when(accessibilityMap.getAccessibleRoadSections(accessibilityRequest)).thenReturn(accessibleRoadSections);

        Set<IsochroneMatch> isochroneMatches = accessibleRoadsService.getBaseAccessibleRoadsByMunicipality(
                accessibilityMap, MUNICIPALITY);

        assertThat(isochroneMatches).isEqualTo(accessibleRoadSections);
    }

    @Test
    void getVehicleAccessibleRoadsByMunicipality_ok() {
        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .startPoint(MUNICIPALITY.getStartPoint())
                .vehicleProperties(vehicleProperties)
                .searchDistanceInMetres(MUNICIPALITY.getSearchDistanceInMetres())
                .municipalityId(MUNICIPALITY.getMunicipalityIdAsInteger())
                .build();
        when(accessibilityMap.getAccessibleRoadSections(accessibilityRequest)).thenReturn(accessibleRoadSections);

        Set<IsochroneMatch> isochroneMatches = accessibleRoadsService.getVehicleAccessibleRoadsByMunicipality(
                accessibilityMap, vehicleProperties, MUNICIPALITY);

        assertThat(isochroneMatches).isEqualTo(accessibleRoadSections);
    }
}
