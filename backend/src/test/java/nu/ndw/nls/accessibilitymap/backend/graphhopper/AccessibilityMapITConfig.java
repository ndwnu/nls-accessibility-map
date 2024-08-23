package nu.ndw.nls.accessibilitymap.backend.graphhopper;

import static nu.ndw.nls.accessibilitymap.shared.model.NetworkConstants.VEHICLE_NAME_CAR;

import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.factory.AccessibilityMapFactory;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.factory.IsochroneServiceFactory;
import nu.ndw.nls.accessibilitymap.accessibility.services.RestrictionMapperProvider;
import nu.ndw.nls.accessibilitymap.accessibility.services.VehicleRestrictionsModelFactory;
import nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink;
import nu.ndw.nls.geometry.bearing.BearingCalculator;
import nu.ndw.nls.geometry.distance.FractionAndDistanceCalculator;
import nu.ndw.nls.geometry.factories.GeodeticCalculatorFactory;
import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
import nu.ndw.nls.routingmapmatcher.RoutingMapMatcherConfiguration;
import nu.ndw.nls.routingmapmatcher.network.model.DirectionalDto;
import nu.ndw.nls.routingmapmatcher.network.model.LinkVehicleMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Import(RoutingMapMatcherConfiguration.class)
class AccessibilityMapITConfig {

    @Bean
    public AccessibilityMapFactory accessibilityMapFactory() {
        RestrictionMapperProvider restrictionMapperProvider = new RestrictionMapperProvider();
        VehicleRestrictionsModelFactory vehicleRestrictionsModelFactory = new VehicleRestrictionsModelFactory(
                restrictionMapperProvider);
        EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor = new EdgeIteratorStateReverseExtractor();
        GeodeticCalculatorFactory geodeticCalculatorFactory = new GeodeticCalculatorFactory();
        IsochroneServiceFactory isochroneServiceFactory = new IsochroneServiceFactory(
                edgeIteratorStateReverseExtractor, new FractionAndDistanceCalculator(geodeticCalculatorFactory,
                List.of(new GeometryFactoryWgs84()), new BearingCalculator(geodeticCalculatorFactory)));
        return new AccessibilityMapFactory(vehicleRestrictionsModelFactory, isochroneServiceFactory);
    }

    @Bean
    public LinkVehicleMapper<AccessibilityLink> linkVehicleMapper() {
        // Redefine mapper to avoid dependency on jobs module
        return new LinkVehicleMapper<>(VEHICLE_NAME_CAR, AccessibilityLink.class) {
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
