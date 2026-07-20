package nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper;

import io.micrometer.common.util.StringUtils;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZone;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TransportConditions;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TransportRestrictions;
import nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper.emission.EmissionZoneMapper;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.ConditionPropertiesDtoV5Json;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.ConditionsDtoV5Json;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransportRestrictionMapper {

    private final TransportConditionsMapper transportConditionsMapper;

    private final EmissionZoneMapper emissionZoneMapper;

    public TransportRestrictions map(ConditionsDtoV5Json conditionsDtoV5Json, String trafficRegulationOrderId) {

        EmissionZone emissionZone;
        if (StringUtils.isNotBlank(trafficRegulationOrderId)) {
            emissionZone = emissionZoneMapper.map(trafficRegulationOrderId);
        } else {
            emissionZone = null;
        }

        return TransportRestrictions.builder()
                .emissionZone(emissionZone)
                .restrictions(transportConditionsMapper.map(conditionsDtoV5Json.getRestrictions()))
                .exemptions(mapExemptions(conditionsDtoV5Json.getExemptions()))
                .build();
    }

    private List<TransportConditions> mapExemptions(List<@Valid ConditionPropertiesDtoV5Json> exemptions) {
        if (CollectionUtils.isEmpty(exemptions)) {
            return Collections.emptyList();
        }

        return exemptions
                .stream()
                .map(transportConditionsMapper::map)
                .toList();
    }
}
