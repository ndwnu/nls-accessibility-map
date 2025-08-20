package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.response;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction.RestrictionType;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.ReasonJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.TrafficSignTypeJson;
import org.springframework.stereotype.Component;

@Component
public class AccessibilityReasonsJsonMapper {

    private final Map<RestrictionType, AccessibilityRestrictionJsonMapper> restrictionMapperMap;

    public AccessibilityReasonsJsonMapper(List<AccessibilityRestrictionJsonMapper> restrictionMapperList) {

        restrictionMapperMap = restrictionMapperList.stream()
                .collect(Collectors.toMap(AccessibilityRestrictionJsonMapper::mapperForType, Function.identity()));
    }

    public List<List<ReasonJson>> mapToReasonJson(List<List<AccessibilityReason>> reasons) {

        return reasons.stream()
                .map(reasonsList -> reasonsList.stream()
                        .map(reason -> new ReasonJson()
                                .trafficSignId(UUID.fromString(reason.trafficSignExternalId()))
                                .trafficSignType(TrafficSignTypeJson.fromValue(reason.trafficSignType().getRvvCode()))
                                .restrictions(mapToRestrictionsJson(reason.restrictions()))
                        ).toList())
                .toList();
    }

    private List<RestrictionJson> mapToRestrictionsJson(List<AccessibilityRestriction> accessibilityRestrictions) {

        return accessibilityRestrictions.stream()
                .map(restriction ->
                        restrictionMapperMap.containsKey(restriction.getTypeOfRestriction())
                                ? restrictionMapperMap.get(restriction.getTypeOfRestriction()).mapToRestrictionJson(restriction)
                                : null)
                .filter(Objects::nonNull).toList();
    }
}
