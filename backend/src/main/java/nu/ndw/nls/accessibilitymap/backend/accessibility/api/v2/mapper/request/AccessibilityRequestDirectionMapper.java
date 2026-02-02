package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.mapper.request;

import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.DirectionJson;
import org.springframework.stereotype.Component;

@Component
public class AccessibilityRequestDirectionMapper {

    public Direction map(DirectionJson directionJson) {
        return switch (directionJson) {
            case FORWARD -> Direction.FORWARD;
            case BACKWARD -> Direction.BACKWARD;
        };
    }
}
