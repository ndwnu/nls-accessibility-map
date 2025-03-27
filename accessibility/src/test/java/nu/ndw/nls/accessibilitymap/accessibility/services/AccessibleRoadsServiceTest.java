package nu.ndw.nls.accessibilitymap.accessibility.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.AccessibilityMap;
import nu.ndw.nls.accessibilitymap.accessibility.model.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import nu.ndw.nls.routingmapmatcher.model.IsochroneMatch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibleRoadsServiceTest {
    private static final double SEARCH_DISTANCE_IN_METERS = 1000D;
    private static final int MUNICIPALITY_ID = 123;

    @Mock
    private AccessibilityMap accessibilityMap;
    @Mock
    private List<IsochroneMatch> accessibleRoadSections;
    @Mock
    private VehicleProperties vehicleProperties;
    @Mock
    private Point startPoint;

    @InjectMocks
    private AccessibleRoadsService accessibleRoadsService;


    @Test
    void getBaseAccessibleRoads() {
        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .startPoint(startPoint)
                .searchDistanceInMetres(SEARCH_DISTANCE_IN_METERS)
                .build();
        when(accessibilityMap.getAccessibleRoadSections(accessibilityRequest)).thenReturn(accessibleRoadSections);

        List<IsochroneMatch> isochroneMatches = accessibleRoadsService.getBaseAccessibleRoads(
                accessibilityMap, startPoint, SEARCH_DISTANCE_IN_METERS);

        assertThat(isochroneMatches).isEqualTo(accessibleRoadSections);
    }


    @Test
    void getBaseAccessibleRoadsByMunicipality() {
        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .startPoint(startPoint)
                .searchDistanceInMetres(SEARCH_DISTANCE_IN_METERS)
                .municipalityId(MUNICIPALITY_ID)
                .build();
        when(accessibilityMap.getAccessibleRoadSections(accessibilityRequest)).thenReturn(accessibleRoadSections);

        List<IsochroneMatch> isochroneMatches = accessibleRoadsService.getBaseAccessibleRoadsByMunicipality(
                accessibilityMap, startPoint, SEARCH_DISTANCE_IN_METERS, MUNICIPALITY_ID);

        assertThat(isochroneMatches).isEqualTo(accessibleRoadSections);
    }


    @Test
    void getVehicleAccessibleRoadsByMunicipality() {
        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .startPoint(startPoint)
                .vehicleProperties(vehicleProperties)
                .searchDistanceInMetres(SEARCH_DISTANCE_IN_METERS)
                .municipalityId(MUNICIPALITY_ID)
                .build();
        when(accessibilityMap.getAccessibleRoadSections(accessibilityRequest)).thenReturn(accessibleRoadSections);

        List<IsochroneMatch> isochroneMatches = accessibleRoadsService.getVehicleAccessibleRoadsByMunicipality(
                accessibilityMap, vehicleProperties, startPoint, SEARCH_DISTANCE_IN_METERS, MUNICIPALITY_ID);

        assertThat(isochroneMatches).isEqualTo(accessibleRoadSections);
    }

    @Test
    void getVehicleAccessibleRoads() {
        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .startPoint(startPoint)
                .vehicleProperties(vehicleProperties)
                .searchDistanceInMetres(SEARCH_DISTANCE_IN_METERS)
                .build();
        when(accessibilityMap.getAccessibleRoadSections(accessibilityRequest)).thenReturn(accessibleRoadSections);

        List<IsochroneMatch> isochroneMatches = accessibleRoadsService.getVehicleAccessibleRoads(
                accessibilityMap, vehicleProperties, startPoint, SEARCH_DISTANCE_IN_METERS);

        assertThat(isochroneMatches).isEqualTo(accessibleRoadSections);
    }
}
