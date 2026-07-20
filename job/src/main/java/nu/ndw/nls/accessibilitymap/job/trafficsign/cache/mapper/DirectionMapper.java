package nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper;

import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TrafficSignPropertiesDtoV5Json.DrivingDirectionEnum;
import org.springframework.stereotype.Component;

@Component
public class DirectionMapper {

    public Direction map(DrivingDirectionEnum drivingDirection) {

        if (drivingDirection == null) {
            return null;
        }

        return switch (drivingDirection) {
            case FORTH -> Direction.FORWARD;
            case BACK -> Direction.BACKWARD;
        };
    }
}
