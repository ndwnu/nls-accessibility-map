package nu.ndw.nls.accessibilitymap.jobs.graphhopper.trafficsign.mappers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import nu.ndw.nls.accessibilitymap.jobs.graphhopper.trafficsign.mappers.signmappers.MaximumSignMapper;
import nu.ndw.nls.accessibilitymap.jobs.graphhopper.trafficsign.mappers.signmappers.NoEntrySignMapper;
import nu.ndw.nls.accessibilitymap.jobs.graphhopper.trafficsign.mappers.signmappers.SignMapper;
import nu.ndw.nls.accessibilitymap.jobs.graphhopper.trafficsign.mappers.signmappers.SignMapper.DtoSetter;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignAccessibilityDto;
import org.springframework.stereotype.Component;

@Component
@Getter
public class TrafficSignMapperRegistry {

    private static final String DUPLICATE_RVV_MSG =
            "Failed to instantiate TrafficSignDtoMapper: at least one RVV code is covered by multiple typed mappers";

    private static final Map<String, DtoSetter<Boolean>> NO_ENTRY_SIGN_MAPPINGS = Map.of(
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

    private static final Map<String, DtoSetter<Double>> MAXIMUM_SIGN_MAPPINGS = Map.of(
            "C17", TrafficSignAccessibilityDto::setMaxLength,
            "C18", TrafficSignAccessibilityDto::setMaxWidth,
            "C19", TrafficSignAccessibilityDto::setMaxHeight,
            "C20", TrafficSignAccessibilityDto::setMaxAxleLoad,
            "C21", TrafficSignAccessibilityDto::setMaxWeight
    );

    private static final Map<String, DtoSetter<Boolean>> NO_ENTRY_SIGN_MAPPINGS_WINDOWED = Map.of(
            "C6T", TrafficSignAccessibilityDto::setCarAccessForbiddenWindowed,
            "C7T", TrafficSignAccessibilityDto::setHgvAccessForbiddenWindowed,
            "C7bT", TrafficSignAccessibilityDto::setHgvAndBusAccessForbiddenWindowed,
            "C12T", TrafficSignAccessibilityDto::setMotorVehicleAccessForbiddenWindowed,
            "C22cT", TrafficSignAccessibilityDto::setLcvAndHgvAccessForbiddenWindowed
    );

    private final List<SignMapper<?>> mappers;

    private final Set<String> includedRvvCodes;

    public TrafficSignMapperRegistry() {
        this.mappers = new ArrayList<>();
        NO_ENTRY_SIGN_MAPPINGS.forEach((rvvCode, setter) -> mappers.add(new NoEntrySignMapper(rvvCode, setter)));
        MAXIMUM_SIGN_MAPPINGS.forEach((rvvCode, setter) -> mappers.add(new MaximumSignMapper(rvvCode, setter)));
        NO_ENTRY_SIGN_MAPPINGS_WINDOWED.forEach(
                (rvvCode, setter) -> mappers.add(new NoEntrySignMapper(rvvCode, setter)));

        this.includedRvvCodes = extractRvvCodes(mappers);
    }


    private Set<String> extractRvvCodes(List<SignMapper<?>> signMappers) {
        List<String> allCodes = signMappers.stream()
                .map(SignMapper::getRvvCode)
                .filter(s-> !s.endsWith("T"))
                .toList();
        Set<String> uniqueCodes = new HashSet<>(allCodes);
        if (uniqueCodes.size() < allCodes.size()) {
            throw new IllegalArgumentException(DUPLICATE_RVV_MSG);
        }
        return uniqueCodes;
    }

}
