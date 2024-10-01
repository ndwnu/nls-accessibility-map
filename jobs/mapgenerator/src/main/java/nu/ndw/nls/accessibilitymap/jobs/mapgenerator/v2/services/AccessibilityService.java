package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.services;

import com.graphhopper.config.Profile;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.CustomModel;
import com.graphhopper.util.PMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.IsochroneService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.factory.IsochroneServiceFactory;
import nu.ndw.nls.accessibilitymap.accessibility.model.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.model.IsochroneArguments;
import nu.ndw.nls.accessibilitymap.accessibility.services.VehicleRestrictionsModelFactory;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.services.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.services.dto.AdditionalSnap;
import nu.ndw.nls.accessibilitymap.shared.model.NetworkConstants;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessibilityService {

    private final IsochroneServiceFactory isochroneServiceFactory;

    private final NetworkGraphHopper network;

    private final VehicleRestrictionsModelFactory modelFactory;

    private final NetworkGraphHopper networkGraphHopper;

    private Accessibility calculateAccessibility(AccessibilityRequest accessibilityRequest,
            List<AdditionalSnap> additionalSnaps) {
        IsochroneService isochroneService = isochroneServiceFactory.createService(networkGraphHopper);

        List<Snap> snaps = additionalSnaps.stream()
                .map(additionalSnap -> additionalSnap.snap())
                .toList();

        Snap startSegment = networkGraphHopper.getLocationIndex()
                .findClosest(accessibilityRequest.startPoint().getX(), accessibilityRequest.startPoint().getY(),
                        EdgeFilter.ALL_EDGES);
        snaps.add(startSegment);

        QueryGraph queryGraph = QueryGraph.create(networkGraphHopper.getBaseGraph(), snaps);

        //TODO loop through snaps and check all virtual nodes and update properties.

        IsochroneArguments arguments = IsochroneArguments.builder()
                .weighting(buildWeightingWithoutRestrictions(accessibilityRequest))
                .startPoint(accessibilityRequest.startPoint())
                .municipalityId(accessibilityRequest.municipalityId())
                .searchDistanceInMetres(accessibilityRequest.searchDistanceInMetres())
                .build();

        IsochroneArguments argumentsWithRestrictionsApplied = IsochroneArguments.builder()
                .weighting(buildWeightingWithRestrictions(accessibilityRequest))
                .startPoint(accessibilityRequest.startPoint())
                .municipalityId(accessibilityRequest.municipalityId())
                .searchDistanceInMetres(accessibilityRequest.searchDistanceInMetres())
                .build();

        // Todo create diff?

        return Accessibility.builder()
                .noAppliedRestrictions(
                        isochroneService.getIsochroneMatchesByMunicipalityId(
                                arguments,
                                queryGraph,
                                startSegment)
                )
                .accessibilityWithRestrictions(
                        isochroneService.getIsochroneMatchesByMunicipalityId(
                                argumentsWithRestrictionsApplied,
                                queryGraph,
                                startSegment)
                )
                .mergedAccessibility(null) // add diff
                .build();
    }

    private Weighting buildWeightingWithoutRestrictions(AccessibilityRequest accessibilityRequest) {
        accessibilityRequest = accessibilityRequest.withVehicleProperties(null);
        Profile profile = networkGraphHopper.getProfile(NetworkConstants.VEHICLE_NAME_CAR);
        CustomModel model = modelFactory.getModel(accessibilityRequest.vehicleProperties());
        PMap hints = new PMap().putObject(CustomModel.KEY, model);

        return network.createWeighting(profile, hints);
    }

    private Weighting buildWeightingWithRestrictions(AccessibilityRequest accessibilityRequest) {
        Profile profile = networkGraphHopper.getProfile(NetworkConstants.VEHICLE_NAME_CAR);
        CustomModel model = modelFactory.getModel(accessibilityRequest.vehicleProperties());
        PMap hints = new PMap().putObject(CustomModel.KEY, model);

        return network.createWeighting(profile, hints);
    }
}
