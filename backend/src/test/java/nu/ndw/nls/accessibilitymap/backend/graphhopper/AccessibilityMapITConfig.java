package nu.ndw.nls.accessibilitymap.backend.graphhopper;

import static nu.ndw.nls.accessibilitymap.shared.model.NetworkConstants.CAR;

import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import nu.ndw.nls.accessibilitymap.backend.graphhopper.factory.AccessibilityMapFactory;
import nu.ndw.nls.accessibilitymap.backend.graphhopper.factory.IsochroneServiceFactory;
import nu.ndw.nls.accessibilitymap.backend.services.RestrictionMapperProvider;
import nu.ndw.nls.accessibilitymap.backend.services.VehicleRestrictionsModelFactory;
import nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink;
import nu.ndw.nls.routingmapmatcher.RoutingMapMatcherConfiguration;
import nu.ndw.nls.routingmapmatcher.network.model.DirectionalDto;
import nu.ndw.nls.routingmapmatcher.network.model.LinkVehicleMapper;
import nu.ndw.nls.routingmapmatcher.util.CrsTransformer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Import(RoutingMapMatcherConfiguration.class)
class AccessibilityMapITConfig {

    @Bean
    public AccessibilityMapFactory accessibilityMapFactory() {
        RestrictionMapperProvider restrictionMapperProvider = new RestrictionMapperProvider();
        VehicleRestrictionsModelFactory vehicleRestrictionsModelFactory = new VehicleRestrictionsModelFactory(
                restrictionMapperProvider);
        CrsTransformer crsTransformer = new CrsTransformer();
        EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor = new EdgeIteratorStateReverseExtractor();
        IsochroneServiceFactory isochroneServiceFactory = new IsochroneServiceFactory(crsTransformer,
                edgeIteratorStateReverseExtractor);
        return new AccessibilityMapFactory(vehicleRestrictionsModelFactory, isochroneServiceFactory);
    }

    @Bean
    public LinkVehicleMapper<AccessibilityLink> linkVehicleMapper() {
        // Redefine mapper to avoid dependency on jobs module
        return new LinkVehicleMapper<>(CAR, AccessibilityLink.class) {
            @Override
            public DirectionalDto<Boolean> getAccessibility(AccessibilityLink link) {
                return link.getAccessibility();
            }

            @Override
            public DirectionalDto<Double> getSpeed(AccessibilityLink link) {
                return new DirectionalDto<>(50d);
            }
        };
    }


}