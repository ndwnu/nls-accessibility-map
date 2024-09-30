package nu.ndw.nls.accessibilitymap.accessibility.graphhopper;

import com.graphhopper.config.Profile;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.CustomModel;
import com.graphhopper.util.PMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.model.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.model.IsochroneArguments;
import nu.ndw.nls.accessibilitymap.accessibility.services.VehicleRestrictionsModelFactory;
import nu.ndw.nls.accessibilitymap.shared.model.NetworkConstants;
import nu.ndw.nls.routingmapmatcher.model.IsochroneMatch;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;

@RequiredArgsConstructor
public class AccessibilityMap {

    private final NetworkGraphHopper networkGraphHopper;
    private final VehicleRestrictionsModelFactory modelFactory;
    private final IsochroneService isochroneService;

    public List<IsochroneMatch> getAccessibleRoadSections(AccessibilityRequest accessibilityRequest) {
        Profile profile = networkGraphHopper.getProfile(NetworkConstants.VEHICLE_NAME_CAR);
        CustomModel model = modelFactory.getModel(accessibilityRequest.vehicleProperties());
        PMap hints = createCustomModelHints(model);
        Weighting weighting = networkGraphHopper.createWeighting(profile, hints);

        Snap startSegment = networkGraphHopper.getLocationIndex()
                .findClosest(accessibilityRequest.startPoint().getX(), accessibilityRequest.startPoint().getY(), EdgeFilter.ALL_EDGES);
        /*
            Lookup will create virtual edges based on the snapped point, thereby cutting the segment in 2 line strings.
            It also sets the closestNode of the matchedQueryResult to the virtual node id. In this way it creates a
            start point for isochrone calculation based on the snapped point coordinates.
        */
        QueryGraph queryGraph = QueryGraph.create(networkGraphHopper.getBaseGraph(), startSegment);

        return isochroneService.getIsochroneMatchesByMunicipalityId(
                IsochroneArguments.builder()
                        .weighting(weighting)
                        .startPoint(accessibilityRequest.startPoint())
                        .municipalityId(accessibilityRequest.municipalityId())
                        .searchDistanceInMetres(accessibilityRequest.searchDistanceInMetres())
                        .build(),
                queryGraph,
                startSegment);
    }

    private PMap createCustomModelHints(CustomModel model) {
        return new PMap()
                .putObject(CustomModel.KEY, model);
    }
}
