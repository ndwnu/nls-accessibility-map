package nu.ndw.nls.accessibilitymap.accessibility.speedlimit.dto;

import java.io.Serial;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.speedlimit.SpeedLimit;

@NoArgsConstructor
public class SpeedLimits extends LinkedHashSet<SpeedLimit> {

    @Serial
    private static final long serialVersionUID = 1L;

    private Map<Integer, List<SpeedLimit>> speedLimitsByRoadSectionId;

    public SpeedLimits(SpeedLimit... speedLimits) {
        this(Arrays.asList(speedLimits));
    }

    public SpeedLimits(Collection<SpeedLimit> speedLimits) {
        super(speedLimits);
    }

    public Optional<SpeedLimit> findByRoadSectionId(int roadSectionId, Direction direction) {
        if(Objects.isNull(speedLimitsByRoadSectionId)) {
            speedLimitsByRoadSectionId = this.stream().collect(Collectors.groupingBy(SpeedLimit::roadSectionId));
        }

        return speedLimitsByRoadSectionId.getOrDefault(roadSectionId, List.of()).stream()
                .filter(speedLimit -> speedLimit.direction() == direction)
                .findFirst();
    }
}
