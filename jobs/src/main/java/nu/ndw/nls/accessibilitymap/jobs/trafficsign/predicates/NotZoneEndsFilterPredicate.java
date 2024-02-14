package nu.ndw.nls.accessibilitymap.jobs.trafficsign.predicates;

import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignJsonDtoV3;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.ZoneCode;
import nu.ndw.nls.accessibilitymap.jobs.trafficsign.mappers.TrafficSignToDtoMapper;
import org.springframework.stereotype.Component;

@Component
public class NotZoneEndsFilterPredicate implements TrafficSignToDtoMapper.TrafficSignIncludedFilterPredicate {
    @Override
    public boolean test(TrafficSignJsonDtoV3 trafficSignJsonDtoV3) {
        return trafficSignJsonDtoV3.getZoneCode() != ZoneCode.END;
    }
}
