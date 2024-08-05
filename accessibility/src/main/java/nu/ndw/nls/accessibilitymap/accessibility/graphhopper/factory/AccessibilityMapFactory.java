package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.factory;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.services.VehicleRestrictionsModelFactory;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.AccessibilityMap;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.IsochroneService;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessibilityMapFactory {

    private final VehicleRestrictionsModelFactory vehicleRestrictionsModelFactory;
    private final IsochroneServiceFactory isochroneServiceFactory;

    public AccessibilityMap createMapMatcher(NetworkGraphHopper preInitializedNetwork) {
        Preconditions.checkNotNull(preInitializedNetwork);
        IsochroneService isochroneService = isochroneServiceFactory.createService(preInitializedNetwork);
        return new AccessibilityMap(preInitializedNetwork, vehicleRestrictionsModelFactory, isochroneService);
    }
}
