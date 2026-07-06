package nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper;

import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TransportRestrictions;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.ConditionsDtoV5Json;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransportRestrictionMapper {

    private final TransportConditionsMapper transportConditionsMapper;

    public TransportRestrictions map(ConditionsDtoV5Json conditionsDtoV5Json) {
        if (conditionsDtoV5Json.getRestrictions() == null) {
            return TransportRestrictions.builder()
                    .build();
        }

        return TransportRestrictions.builder()
                .restrictions(transportConditionsMapper.map(conditionsDtoV5Json.getRestrictions()))
                .exemptions(conditionsDtoV5Json.getExemptions().stream().map(transportConditionsMapper::map).toList())
                .build();
    }
}
