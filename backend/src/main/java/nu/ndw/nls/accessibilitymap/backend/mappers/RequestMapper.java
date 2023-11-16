package nu.ndw.nls.accessibilitymap.backend.mappers;

import nu.ndw.nls.accessibilitymap.backend.controllers.AccessibilityMapApiDelegateImpl.RequestArguments;
import nu.ndw.nls.routingmapmatcher.domain.model.accessibility.VehicleProperties;
import org.springframework.stereotype.Component;

@Component
public class RequestMapper {

    public VehicleProperties maptoVehicleProperties(RequestArguments requestArguments) {
        return VehicleProperties
                .builder()
                .hgvAccessForbidden(true)
                .build();
    }
}
