package nu.ndw.nls.accessibilitymap.accessibility.speedlimit.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serial;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.speedlimit.SpeedLimit;
import org.jspecify.annotations.NonNull;

@SuppressWarnings("java:S2160")
public class SpeedLimits extends LinkedHashSet<SpeedLimit> {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private transient Map<Integer, List<SpeedLimit>> speedLimitsByRoadSectionId;

    private final transient boolean immutable;

    public SpeedLimits() {
        immutable = true;
    }

    public SpeedLimits(SpeedLimit... speedLimits) {
        this(Arrays.asList(speedLimits));
    }

    public SpeedLimits(Collection<SpeedLimit> speedLimits) {
        super(speedLimits);
        immutable = true;
    }

    public Optional<SpeedLimit> findByRoadSectionId(int roadSectionId, Direction direction) {
        if (Objects.isNull(speedLimitsByRoadSectionId)) {
            speedLimitsByRoadSectionId = this.stream().collect(Collectors.groupingBy(SpeedLimit::roadSectionId));
        }

        return speedLimitsByRoadSectionId.getOrDefault(roadSectionId, List.of()).stream()
                .filter(speedLimit -> speedLimit.direction() == direction)
                .findFirst();
    }

    @Override
    public SpeedLimit removeFirst() {
        throw new UnsupportedOperationException("SpeedLimits is immutable");
    }

    @Override
    public SpeedLimit removeLast() {
        throw new UnsupportedOperationException("SpeedLimits is immutable");
    }

    @Override
    public boolean add(SpeedLimit speedLimit) {
        if (immutable) {
            throw new UnsupportedOperationException("SpeedLimits is immutable");
        }
        return super.add(speedLimit);
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("SpeedLimits is immutable");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("SpeedLimits is immutable");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("SpeedLimits is immutable");
    }

    @Override
    public boolean removeIf(@NonNull Predicate<? super SpeedLimit> filter) {
        throw new UnsupportedOperationException("SpeedLimits is immutable");
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> c) {
        throw new UnsupportedOperationException("SpeedLimits is immutable");
    }
}
