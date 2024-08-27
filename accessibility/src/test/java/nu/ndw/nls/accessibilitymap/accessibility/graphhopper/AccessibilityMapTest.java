package nu.ndw.nls.accessibilitymap.accessibility.graphhopper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.graphhopper.config.Profile;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.CustomModel;
import com.graphhopper.util.PMap;
import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.model.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.model.IsochroneArguments;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import nu.ndw.nls.accessibilitymap.accessibility.services.VehicleRestrictionsModelFactory;
import nu.ndw.nls.accessibilitymap.shared.model.NetworkConstants;
import nu.ndw.nls.routingmapmatcher.model.IsochroneMatch;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Point;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityMapTest {

    private static final int MUNICIPALITY_ID = 3;
    private static final double SEARCH_DISTANCE = 500d;

    @Mock
    private Weighting weighting;
    @Mock
    private AccessibilityRequest accessibilityRequest;
    @Mock
    private VehicleProperties vehicleProperties;
    @Mock
    private Profile profile;
    @Mock
    private CustomModel model;
    @Mock
    private Point startPoint;
    @Mock
    private List<IsochroneMatch> matches;

    @Mock
    private NetworkGraphHopper network;
    @Mock
    private VehicleRestrictionsModelFactory modelFactory;
    @Mock
    private IsochroneService isochroneService;

    @InjectMocks
    private AccessibilityMap accessibilityMap;

    @Test
    void getAccessibleRoadSections_ok() {
        when(accessibilityRequest.vehicleProperties()).thenReturn(vehicleProperties);
        when(modelFactory.getModel(vehicleProperties)).thenReturn(model);
        when(network.getProfile(NetworkConstants.VEHICLE_NAME_CAR)).thenReturn(profile);
        when(network.createWeighting(eq(profile), any(PMap.class))).thenReturn(weighting);
        when(accessibilityRequest.startPoint()).thenReturn(startPoint);
        when(accessibilityRequest.municipalityId()).thenReturn(MUNICIPALITY_ID);
        when(accessibilityRequest.searchDistanceInMetres()).thenReturn(SEARCH_DISTANCE);

        when(isochroneService.getIsochroneMatchesByMunicipalityId(IsochroneArguments.builder()
                        .startPoint(startPoint)
                        .weighting(weighting)
                        .searchDistanceInMetres(SEARCH_DISTANCE)
                        .municipalityId(MUNICIPALITY_ID)
                        .build())).thenReturn(matches);

        List<IsochroneMatch> result = accessibilityMap.getAccessibleRoadSections(accessibilityRequest);
        assertEquals(matches, result);
    }
}
