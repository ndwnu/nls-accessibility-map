package nu.ndw.nls.accessibilitymap.accessibility.graphhopper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.graphhopper.config.Profile;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.storage.index.LocationIndexTree;
import com.graphhopper.storage.index.Snap;
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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
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
    private NetworkGraphHopper networkGraphHopper;

    @Mock
    private VehicleRestrictionsModelFactory modelFactory;

    @Mock
    private IsochroneService isochroneService;

    @Mock
    private QueryGraph queryGraph;

    @Mock
    private BaseGraph baseGraph;

    @Mock
    private Snap startSegment;

    @Mock
    private LocationIndexTree locationIndexTree;

    @InjectMocks
    private AccessibilityMap accessibilityMap;

    private static MockedStatic<QueryGraph> queryGraphStaticMock;

    @BeforeAll
    static void setUp() {
        queryGraphStaticMock = Mockito.mockStatic(QueryGraph.class);
    }

    @AfterAll
    static void tearDown() {
        queryGraphStaticMock.close();
    }

    @Test
    void getAccessibleRoadSections_ok() {
        when(accessibilityRequest.vehicleProperties()).thenReturn(vehicleProperties);
        when(modelFactory.getModel(vehicleProperties)).thenReturn(model);

        when(networkGraphHopper.getBaseGraph()).thenReturn(baseGraph);
        when(networkGraphHopper.getProfile(NetworkConstants.VEHICLE_NAME_CAR)).thenReturn(profile);
        when(networkGraphHopper.createWeighting(eq(profile), any(PMap.class))).thenReturn(weighting);
        when(networkGraphHopper.getLocationIndex()).thenReturn(locationIndexTree);

        when(locationIndexTree.findClosest(2d, 3d, EdgeFilter.ALL_EDGES)).thenReturn(startSegment);
        when(startPoint.getX()).thenReturn(2d);
        when(startPoint.getY()).thenReturn(3d);
        queryGraphStaticMock.when(() -> QueryGraph.create(baseGraph, startSegment)).thenReturn(queryGraph);
        when(accessibilityRequest.startPoint()).thenReturn(startPoint);
        when(accessibilityRequest.municipalityId()).thenReturn(MUNICIPALITY_ID);
        when(accessibilityRequest.searchDistanceInMetres()).thenReturn(SEARCH_DISTANCE);

        when(isochroneService.getIsochroneMatchesByMunicipalityId(IsochroneArguments.builder()
                        .startPoint(startPoint)
                        .weighting(weighting)
                        .searchDistanceInMetres(SEARCH_DISTANCE)
                        .municipalityId(MUNICIPALITY_ID)
                        .build(),
                queryGraph,
                startSegment)).thenReturn(matches);

        List<IsochroneMatch> result = accessibilityMap.getAccessibleRoadSections(accessibilityRequest);
        assertEquals(matches, result);
    }
}
