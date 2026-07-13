package nu.ndw.nls.accessibilitymap.accessibility.core.dto.speedlimit;

import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import org.springframework.validation.annotation.Validated;

@Validated
public record SpeedLimit(
        int roadSectionId,
        Direction direction,
        double speedInKmPerHour) {

}
