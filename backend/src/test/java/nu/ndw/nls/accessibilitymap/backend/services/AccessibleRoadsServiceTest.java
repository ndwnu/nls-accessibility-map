package nu.ndw.nls.accessibilitymap.backend.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import nu.ndw.nls.accessibilitymap.backend.graphhopper.AccessibilityMap;
import nu.ndw.nls.accessibilitymap.backend.model.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.backend.model.Municipality;
import nu.ndw.nls.accessibilitymap.backend.model.MunicipalityBoundingBox;
import nu.ndw.nls.accessibilitymap.backend.model.VehicleProperties;
import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
import nu.ndw.nls.routingmapmatcher.model.IsochroneMatch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibleRoadsServiceTest {


    private static final String MUNICIPALITY_ID = "GM0307";
    private static final Municipality MUNICIPALITY;
    private static final LocalDate DATE_LAST_CHECK = LocalDate.of(2024, 7, 11);


    static {
        try {
            MUNICIPALITY = new Municipality(new GeometryFactoryWgs84().createPoint(
                    new Coordinate(5.0, 52.0)),
                    50000,
                    MUNICIPALITY_ID,
                    307,
                    "Test",
                    new URL("http://iets-met-vergunningen.nl"),
                    new MunicipalityBoundingBox(1.0, 1.1, 2.1, 2.2),
                    DATE_LAST_CHECK);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }


    @Mock
    private AccessibilityMap accessibilityMap;
    @Mock
    private List<IsochroneMatch> accessibleRoadSections;
    @Mock
    private VehicleProperties vehicleProperties;

    @InjectMocks
    private AccessibleRoadsService accessibleRoadsService;

    @Test
    void getVehicleAccessibleRoadsByMunicipality_ok() {
        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .startPoint(MUNICIPALITY.getStartPoint())
                .vehicleProperties(vehicleProperties)
                .searchDistanceInMetres(MUNICIPALITY.getSearchDistanceInMetres())
                .municipalityId(MUNICIPALITY.getMunicipalityIdInteger())
                .build();
        when(accessibilityMap.getAccessibleRoadSections(accessibilityRequest)).thenReturn(accessibleRoadSections);

        List<IsochroneMatch> isochroneMatches = accessibleRoadsService.getVehicleAccessibleRoadsByMunicipality(
                accessibilityMap, vehicleProperties, MUNICIPALITY);

        assertThat(isochroneMatches).isEqualTo(accessibleRoadSections);
    }
}
