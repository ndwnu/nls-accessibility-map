package nu.ndw.nls.accessibilitymap.jobs.graphhopper.trafficsign.mappers;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.graphhopper.trafficsign.mappers.signmappers.SignMapper;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignAccessibilityDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TrafficSignToDtoMapper {

    private final List<SignMapper<?>> signMappers;
    private final Predicate<TrafficSignGeoJsonDto> includedTrafficSignsPredicate;
    private final NoEntrySignWindowedMapper noEntrySignWindowedMapper;

    public TrafficSignToDtoMapper(
            TrafficSignMapperRegistry trafficSignMapperRegistry,
            List<TrafficSignIncludedFilterPredicate> trafficSignIncludedFilterPredicates,
            NoEntrySignWindowedMapper noEntrySignWindowedMapper) {
        this.signMappers = trafficSignMapperRegistry.getMappers();

        this.includedTrafficSignsPredicate = trafficSignIncludedFilterPredicates.stream()
                .map(a -> (Predicate<TrafficSignGeoJsonDto>) a)
                .reduce(Predicate::and)
                .orElseThrow(() -> new IllegalArgumentException("No traffic sign include filters found"));
        this.noEntrySignWindowedMapper = noEntrySignWindowedMapper;
    }

    public TrafficSignAccessibilityDto map(List<TrafficSignGeoJsonDto> trafficSigns) {
        Map<String, List<TrafficSignGeoJsonDto>> includedTrafficSigns = trafficSigns.stream()
                .filter(this.includedTrafficSignsPredicate)
                .map(noEntrySignWindowedMapper::map)
                .collect(Collectors.groupingBy(ts -> ts.getProperties().getRvvCode()));

        TrafficSignAccessibilityDto dto = new TrafficSignAccessibilityDto();
        signMappers.forEach(mapper -> mapper.addToDto(dto, includedTrafficSigns));
        return dto;
    }

    /**
     * Used by {@link TrafficSignToDtoMapper} to determine which traffic signs to include
     */
    public interface TrafficSignIncludedFilterPredicate extends Predicate<TrafficSignGeoJsonDto> {

    }
}