package nu.ndw.nls.accessibilitymap.jobs.trafficsigns.mappers;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos.TextSignJsonDtoV3;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos.TrafficSignAccessibilityDto;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos.TrafficSignJsonDtoV3;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.mappers.signmappers.SignMapper;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TrafficSignToDtoMapper {

    private static final List<String> IGNORED_TEXT_SIGN_TYPES = List.of(
            // Sign has exceptions, e.g. for local traffic.
            "UIT",
            // Sign is a pre-announcement, e.g. restriction starts in 800 metres.
            "VOOR",
            // Sign only applies between certain times.
            "TIJD");
    private static final String DUPLICATE_RVV_MSG =
            "Failed to instantiate TrafficSignDtoMapper: at least one RVV code is covered by multiple typed mappers";

    private final List<SignMapper<?>> signMappers;
    private final Set<String> allRvvCodes;

    public TrafficSignToDtoMapper(TrafficSignMapperRegistry trafficSignMapperRegistry) {
        this.signMappers = trafficSignMapperRegistry.getMappers();
        this.allRvvCodes = extractRvvCodes(signMappers);
    }

    public Set<String> getRvvCodesUsed() {
        return allRvvCodes;
    }

    public TrafficSignAccessibilityDto map(List<TrafficSignJsonDtoV3> trafficSigns) {
        List<TrafficSignJsonDtoV3> filteredTrafficSigns = trafficSigns.stream().filter(this::restrictionIsAbsolute)
                .toList();

        Map<String, List<TrafficSignJsonDtoV3>> noEntrySignMap = filteredTrafficSigns.stream()
                .filter(sign -> allRvvCodes.contains(sign.getRvvCode()))
                .collect(Collectors.groupingBy(TrafficSignJsonDtoV3::getRvvCode));

        TrafficSignAccessibilityDto dto = new TrafficSignAccessibilityDto();
        signMappers.forEach(mapper -> mapper.addToDto(dto, noEntrySignMap));
        return dto;
    }

    private boolean restrictionIsAbsolute(TrafficSignJsonDtoV3 trafficSign) {
        if (trafficSign.getTextSigns() == null) {
            return true;
        }
        return trafficSign.getTextSigns().stream()
                .map(TextSignJsonDtoV3::getType)
                .filter(Objects::nonNull)
                .noneMatch(IGNORED_TEXT_SIGN_TYPES::contains);
    }

    private Set<String> extractRvvCodes(List<SignMapper<?>> signMappers) {
        List<String> allCodes = signMappers.stream()
                .map(SignMapper::getRvvCode)
                .toList();
        Set<String> uniqueCodes = new HashSet<>(allCodes);
        if(uniqueCodes.size() < allCodes.size()) {
            throw new IllegalArgumentException(DUPLICATE_RVV_MSG);
        }
        return uniqueCodes;
    }

}
