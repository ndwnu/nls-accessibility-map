package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.mapper.response;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityRestriction.RestrictionType;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.mapper.response.restriction.AccessibilityRestrictionJsonMapperV2;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.ReasonJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.TrafficSignTypeJson;
import org.springframework.stereotype.Component;

@Component
public class AccessibilityReasonsJsonMapperV2 {

    @SuppressWarnings("java:S3740")
    private final Map<RestrictionType, AccessibilityRestrictionJsonMapperV2> restrictionMapperMap;

    @SuppressWarnings("java:S3740")
    public AccessibilityReasonsJsonMapperV2(
            List<AccessibilityRestrictionJsonMapperV2> restrictionMapperList) {

        restrictionMapperMap = restrictionMapperList.stream()
                .collect(Collectors.toMap(AccessibilityRestrictionJsonMapperV2::getRestrictionType, Function.identity()));
    }

    public List<List<ReasonJson>> map(List<List<AccessibilityReason>> reasons) {
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
                                ? restrictionMapperMap.get(restriction.getTypeOfRestriction()).map(restriction)
                                : null)
                .filter(Objects::nonNull).toList();
    }
}
