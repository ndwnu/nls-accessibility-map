package nu.ndw.nls.accessibilitymap.accessibility.graphhopper;

import com.graphhopper.config.Profile;
import com.graphhopper.routing.weighting.Weighting;
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

    private final NetworkGraphHopper network;
    private final VehicleRestrictionsModelFactory modelFactory;
    private final IsochroneService isochroneService;

    public List<IsochroneMatch> getAccessibleRoadSections(AccessibilityRequest accessibilityRequest) {
        Profile profile = network.getProfile(NetworkConstants.VEHICLE_NAME_CAR);
        CustomModel model = modelFactory.getModel(accessibilityRequest.vehicleProperties());
        PMap hints = createCustomModelHints(model);
        Weighting weighting = network.createWeighting(profile, hints);

        return isochroneService.getIsochroneMatchesByMunicipalityId(IsochroneArguments.builder()
                        .weighting(weighting)
                        .startPoint(accessibilityRequest.startPoint())
                        .municipalityId(accessibilityRequest.municipalityId())
                        .searchDistanceInMetres(accessibilityRequest.searchDistanceInMetres())
                        .build());
    }

    private PMap createCustomModelHints(CustomModel model) {
        return new PMap()
                .putObject(CustomModel.KEY, model);
    }
}
