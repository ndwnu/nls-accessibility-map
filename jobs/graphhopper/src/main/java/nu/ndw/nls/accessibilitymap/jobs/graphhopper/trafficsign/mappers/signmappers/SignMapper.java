package nu.ndw.nls.accessibilitymap.jobs.graphhopper.trafficsign.mappers.signmappers;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignAccessibilityDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.routingmapmatcher.network.model.DirectionalDto;

@Slf4j
@RequiredArgsConstructor
public abstract class SignMapper<T> {

    private static final String DRIVING_DIRECTION_BACKWARD = "T";
    private static final String DRIVING_DIRECTION_FORWARD = "H";

    @Getter
    private final String rvvCode;
    private final DtoSetter<T> setter;
    private final T defaultValue;
    private final BinaryOperator<T> accumulator;

    public void addToDto(TrafficSignAccessibilityDto dto, Map<String, List<TrafficSignGeoJsonDto>> noEntrySignMap) {
        List<TrafficSignGeoJsonDto> signs = noEntrySignMap.getOrDefault(rvvCode, List.of());
        DirectionalDto<T> directionalDto = DirectionalDto.<T>builder()
                .forward(getValueInDirection(signs, this::isForward))
                .reverse(getValueInDirection(signs, this::isBackward))
                .build();
        setter.set(dto, directionalDto);
    }

    private T getValueInDirection(List<TrafficSignGeoJsonDto> trafficSigns,
            Predicate<TrafficSignGeoJsonDto> directionPredicate) {
        return trafficSigns.stream()
                .filter(directionPredicate)
                .map(this::getValue)
                .flatMap(Optional::stream)
                .reduce(defaultValue, accumulator);
    }

    abstract Optional<T> getValue(TrafficSignGeoJsonDto trafficSign);

    private boolean isBackward(TrafficSignGeoJsonDto trafficSign) {
        // Driving direction null (unknown) is mapped to both directions.
        return !DRIVING_DIRECTION_FORWARD.equals(trafficSign.getProperties().getDrivingDirection());
    }

    private boolean isForward(TrafficSignGeoJsonDto trafficSign) {
        // Driving direction null (unknown) is mapped to both directions.
        return !DRIVING_DIRECTION_BACKWARD.equals(trafficSign.getProperties().getDrivingDirection());
    }

    public interface DtoSetter<T> {
        void set(TrafficSignAccessibilityDto dto, DirectionalDto<T> value);
    }

}
