package nu.ndw.nls.accessibilitymap.accessibility.speedlimit.dto;

import java.io.Serial;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import lombok.NoArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.speedlimit.SpeedLimit;

@NoArgsConstructor
public class SpeedLimits extends LinkedHashSet<SpeedLimit> {

    @Serial
    private static final long serialVersionUID = 1L;

    public SpeedLimits(SpeedLimit... speedLimits) {
        this(Arrays.asList(speedLimits));
    }

    public SpeedLimits(Collection<SpeedLimit> speedLimits) {
        super(speedLimits);
    }
}
