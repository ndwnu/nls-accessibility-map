package nu.ndw.nls.accessibilitymap.jobs.graphhopper.trafficsign.predicates;

import nu.ndw.nls.accessibilitymap.jobs.graphhopper.trafficsign.mappers.TrafficSignToDtoMapper.TrafficSignIncludedFilterPredicate;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.ZoneCodeType;
import org.springframework.stereotype.Component;

@Component
public class NotZoneEndsFilterPredicate implements TrafficSignIncludedFilterPredicate {

    @Override
    public boolean test(TrafficSignGeoJsonDto trafficSignJsonDto) {
        return !ZoneCodeType.END.toString().equals(trafficSignJsonDto.getProperties().getZoneCode());
    }
}
