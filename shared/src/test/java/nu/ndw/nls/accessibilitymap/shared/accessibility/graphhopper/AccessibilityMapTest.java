package nu.ndw.nls.accessibilitymap.shared.accessibility.graphhopper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.graphhopper.config.Profile;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.CustomModel;
import com.graphhopper.util.PMap;
import java.util.List;
import nu.ndw.nls.accessibilitymap.shared.accessibility.model.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.shared.accessibility.graphhopper.AccessibilityMap;
import nu.ndw.nls.accessibilitymap.shared.accessibility.graphhopper.IsochroneService;
import nu.ndw.nls.accessibilitymap.shared.accessibility.model.VehicleProperties;
import nu.ndw.nls.accessibilitymap.shared.accessibility.services.VehicleRestrictionsModelFactory;
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
    private CustomModel model;
    @Captor
    private ArgumentCaptor<Profile> profile;
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
        when(network.createWeighting(profile.capture(), any(PMap.class))).thenReturn(weighting);
        when(accessibilityRequest.startPoint()).thenReturn(startPoint);
        when(accessibilityRequest.municipalityId()).thenReturn(MUNICIPALITY_ID);
        when(accessibilityRequest.searchDistanceInMetres()).thenReturn(SEARCH_DISTANCE);

        when(isochroneService.getIsochroneMatchesByMunicipalityId(weighting, startPoint, MUNICIPALITY_ID,
                SEARCH_DISTANCE)).thenReturn(matches);

        List<IsochroneMatch> result = accessibilityMap.getAccessibleRoadSections(accessibilityRequest);
        assertEquals(matches, result);
        assertEquals(model, profile.getValue().getCustomModel());
    }
}
