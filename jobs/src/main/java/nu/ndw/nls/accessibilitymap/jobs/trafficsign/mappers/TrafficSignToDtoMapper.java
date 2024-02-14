package nu.ndw.nls.accessibilitymap.jobs.trafficsign.mappers;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.trafficsign.mappers.signmappers.SignMapper;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignAccessibilityDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignJsonDtoV3;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TrafficSignToDtoMapper {

    private final List<SignMapper<?>> signMappers;
    private final Predicate<TrafficSignJsonDtoV3> includedTrafficSignsPredicate;

    public TrafficSignToDtoMapper(TrafficSignMapperRegistry trafficSignMapperRegistry,
            List<TrafficSignIncludedFilterPredicate> trafficSignIncludedFilterPredicates) {
        this.signMappers = trafficSignMapperRegistry.getMappers();

        this.includedTrafficSignsPredicate  = trafficSignIncludedFilterPredicates.stream()
                .map(a -> (Predicate<TrafficSignJsonDtoV3>) a)
                .reduce(Predicate::and)
                .orElseThrow(() -> new IllegalArgumentException("No traffic sign include filters found"));
    }


    public TrafficSignAccessibilityDto map(List<TrafficSignJsonDtoV3> trafficSigns) {

        Map<String, List<TrafficSignJsonDtoV3>> includedTrafficSigns  = trafficSigns.stream()
                .filter(this.includedTrafficSignsPredicate)
                .collect(Collectors.groupingBy(TrafficSignJsonDtoV3::getRvvCode));

        TrafficSignAccessibilityDto dto = new TrafficSignAccessibilityDto();
        signMappers.forEach(mapper -> mapper.addToDto(dto, includedTrafficSigns ));
        return dto;
    }


    /**
     * Used by {@link TrafficSignToDtoMapper} to determine which traffic signs to include
     */
    public interface TrafficSignIncludedFilterPredicate extends Predicate<TrafficSignJsonDtoV3> {

    }
}
