package nu.ndw.nls.accessibilitymap.test.acceptance.driver.speedlimit.dto;

import java.util.Objects;
import lombok.Builder;

@Builder
public record SpeedLimit(
        long roadSectionId,
        Integer forwardAverageSpeedLimit,
        Integer backwardAverageSpeedLimit) {

    public boolean hasForwardDirection() {
        return Objects.nonNull(forwardAverageSpeedLimit);
    }

    public boolean hasBackwardDirection() {
        return Objects.nonNull(backwardAverageSpeedLimit);
    }
}
