package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.predicates;

import java.util.function.Predicate;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.ZoneCodeType;
import org.springframework.stereotype.Component;

@Component
public class NotZoneEndsFilterPredicate implements Predicate<TrafficSign> {

    @Override
    public boolean test(TrafficSign trafficSign) {
        return !ZoneCodeType.END.getValue().equals(trafficSign.zoneCode());
    }
}
