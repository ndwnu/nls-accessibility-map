package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper.mapper;

import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.AccessibilityLink;
import nu.ndw.nls.routingmapmatcher.network.model.DirectionalDto;
import nu.ndw.nls.routingmapmatcher.network.model.LinkVehicleMapper;
import org.springframework.stereotype.Component;

@Component
public class AccessibilityLinkCarMapper extends LinkVehicleMapper<AccessibilityLink> {

    private static final DirectionalDto<Double> DEFAULT_SPEED = new DirectionalDto<>(50.0);

    public AccessibilityLinkCarMapper() {
        super(NetworkConstants.VEHICLE_NAME_CAR, AccessibilityLink.class);
    }

    @Override
    public DirectionalDto<Boolean> getAccessibility(AccessibilityLink link) {
        return link.getAccessibility();
    }

    @Override
    public DirectionalDto<Double> getSpeed(AccessibilityLink link) {
        return DEFAULT_SPEED;
    }
}
