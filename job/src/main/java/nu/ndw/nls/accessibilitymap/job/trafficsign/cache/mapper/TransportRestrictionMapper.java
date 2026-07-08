package nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZone;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TransportRestrictions;
import nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper.emission.EmissionZoneMapper;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.ConditionsDtoV5Json;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransportRestrictionMapper {

    private final TransportConditionsMapper transportConditionsMapper;

    private final EmissionZoneMapper emissionZoneMapper;
    public TransportRestrictions map(ConditionsDtoV5Json conditionsDtoV5Json, String trafficRegulationOrderId) {

        EmissionZone emissionZone;
        if ( StringUtils.isNotBlank(trafficRegulationOrderId)) {
            emissionZone = emissionZoneMapper.map(trafficRegulationOrderId);
        } else {
            emissionZone = null;
        }

        if (conditionsDtoV5Json.getRestrictions() == null) {
            return TransportRestrictions.builder()
                    .emissionZone(emissionZone)
                    .build();
        }

        return TransportRestrictions.builder()
                .emissionZone(emissionZone)
                .restrictions(transportConditionsMapper.map(conditionsDtoV5Json.getRestrictions()))
                .exemptions(conditionsDtoV5Json.getExemptions()
                        .stream()
                        .map(transportConditionsMapper::map)
                        .toList())
                .build();
    }
}
