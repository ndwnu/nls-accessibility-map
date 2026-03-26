package nu.ndw.nls.accessibilitymap.accessibility.nwb.mapper;

import nu.ndw.nls.routingmapmatcher.network.model.DirectionalDto;
import org.springframework.stereotype.Component;

@Component
public class DrivingDirectionMapper {

    private static final String ROAD_SECTION_DRIVING_DIRECTION_FORWARD = "H";

    private static final String ROAD_SECTION_DRIVING_DIRECTION_BACKWARD = "T";

    public DirectionalDto<Boolean> map(String drivingDirection) {
        // We only check H and T values, all other values mean accessible.
        return DirectionalDto.<Boolean>builder()
                .forward(!ROAD_SECTION_DRIVING_DIRECTION_BACKWARD.equals(drivingDirection))
                .reverse(!ROAD_SECTION_DRIVING_DIRECTION_FORWARD.equals(drivingDirection))
                .build();
    }
}
