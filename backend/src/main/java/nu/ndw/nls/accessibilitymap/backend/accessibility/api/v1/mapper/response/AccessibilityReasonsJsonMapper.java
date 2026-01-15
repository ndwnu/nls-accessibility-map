package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.mapper.response;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction.RestrictionType;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.mapper.response.restriction.AccessibilityRestrictionJsonMapper;
import nu.ndw.nls.accessibilitymap.generated.model.v1.ReasonJson;
import nu.ndw.nls.accessibilitymap.generated.model.v1.RestrictionJson;
import nu.ndw.nls.accessibilitymap.generated.model.v1.TrafficSignTypeJson;
import org.springframework.stereotype.Component;

@Component
public class AccessibilityReasonsJsonMapper {

    @SuppressWarnings("java:S3740")
    private final Map<RestrictionType, AccessibilityRestrictionJsonMapper> restrictionMapperMap;

    @SuppressWarnings("java:S3740")
    public AccessibilityReasonsJsonMapper(
            List<AccessibilityRestrictionJsonMapper> restrictionMapperList) {

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

    @SuppressWarnings("java:S3740")
    private List<RestrictionJson> mapToRestrictionsJson(List<AccessibilityRestriction> accessibilityRestrictions) {
        return accessibilityRestrictions.stream()
                .map(restriction ->
                        restrictionMapperMap.containsKey(restriction.getTypeOfRestriction())
                                ? restrictionMapperMap.get(restriction.getTypeOfRestriction()).mapToRestrictionJson(restriction)
                                : null)
                .filter(Objects::nonNull).toList();
    }
}
