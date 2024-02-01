package nu.ndw.nls.accessibilitymap.jobs.trafficsigns.mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos.TrafficSignAccessibilityDto;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.mappers.signmappers.MaximumSignMapper;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.mappers.signmappers.NoEntrySignMapper;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.mappers.signmappers.SignMapper;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.mappers.signmappers.SignMapper.DtoSetter;
import org.springframework.stereotype.Component;

@Component
@Getter
public class TrafficSignMapperRegistry {

    public static final Map<String, DtoSetter<Boolean>> NO_ENTRY_SIGN_MAPPINGS = Map.of(
            "C6", TrafficSignAccessibilityDto::setCarAccessForbidden,
            "C7", TrafficSignAccessibilityDto::setHgvAccessForbidden,
            "C7a", TrafficSignAccessibilityDto::setBusAccessForbidden,
            "C7b", TrafficSignAccessibilityDto::setHgvAndBusAccessForbidden,
            "C8", TrafficSignAccessibilityDto::setTractorAccessForbidden,
            "C9", TrafficSignAccessibilityDto::setSlowVehicleAccessForbidden,
            "C10", TrafficSignAccessibilityDto::setTrailerAccessForbidden,
            "C11", TrafficSignAccessibilityDto::setMotorcycleAccessForbidden,
            "C12", TrafficSignAccessibilityDto::setMotorVehicleAccessForbidden,
            "C22c", TrafficSignAccessibilityDto::setLcvAndHgvAccessForbidden
    );

    public static final Map<String, DtoSetter<Double>> MAXIMUM_SIGN_MAPPINGS = Map.of(
            "C17", TrafficSignAccessibilityDto::setMaxLength,
            "C18", TrafficSignAccessibilityDto::setMaxWidth,
            "C19", TrafficSignAccessibilityDto::setMaxHeight,
            "C20", TrafficSignAccessibilityDto::setMaxAxleLoad,
            "C21", TrafficSignAccessibilityDto::setMaxWeight
    );

    private final List<SignMapper<?>> mappers;

    public TrafficSignMapperRegistry() {
        this.mappers = new ArrayList<>();
        NO_ENTRY_SIGN_MAPPINGS.forEach((rvvCode, setter) -> mappers.add(new NoEntrySignMapper(rvvCode, setter)));
        MAXIMUM_SIGN_MAPPINGS.forEach((rvvCode, setter) -> mappers.add(new MaximumSignMapper(rvvCode, setter)));
    }

}
