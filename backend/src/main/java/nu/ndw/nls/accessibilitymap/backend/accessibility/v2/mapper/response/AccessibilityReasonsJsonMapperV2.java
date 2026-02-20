package nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.response;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason.ReasonType;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReasonGroup;
import nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.response.reason.AccessibilityReasonJsonMapperV2;
import nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.response.reason.restriction.AccessibilityRestrictionJsonMapperV2;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.ReasonJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionJson;
import org.springframework.stereotype.Component;

@Component
public class AccessibilityReasonsJsonMapperV2 {

    @SuppressWarnings("java:S3740")
    private final Map<ReasonType, AccessibilityReasonJsonMapperV2> reasonMapper;

    @SuppressWarnings("java:S3740")
    private final Map<Class<? extends Restriction>, AccessibilityRestrictionJsonMapperV2> restrictionsMapper;

    @SuppressWarnings("java:S3740")
    public AccessibilityReasonsJsonMapperV2(
            List<AccessibilityReasonJsonMapperV2<?>> restrictionMapperList,
            List<AccessibilityRestrictionJsonMapperV2<?>> restrictionsMapperList) {

        reasonMapper = restrictionMapperList.stream()
                .collect(Collectors.toMap(AccessibilityReasonJsonMapperV2::getReasonType, Function.identity()));

        restrictionsMapper = restrictionsMapperList.stream()
                .collect(Collectors.toMap(AccessibilityRestrictionJsonMapperV2::getRestrictionType, Function.identity()));
    }

    public List<List<ReasonJson>> map(List<AccessibilityReasonGroup> reasonGroups) {
        return reasonGroups.stream()
                .map(reasonsList -> reasonsList.stream()
                        .map(this::mapReasonToReasonJson)
                        .filter(Objects::nonNull)
                        .toList())
                .toList();
    }

    private ReasonJson mapReasonToReasonJson(AccessibilityReason<?> reason) {
        return reasonMapper.containsKey(reason.getReasonType())
                ? reasonMapper.get(reason.getReasonType()).map(reason, mapReasonRestrictionsToRestrictionJson(reason.getRestrictions()))
                : null;
    }

    private List<RestrictionJson> mapReasonRestrictionsToRestrictionJson(Set<Restriction> restrictions) {
        return restrictions.stream()
                .map(restriction -> restrictionsMapper.containsKey(restriction.getClass())
                        ? restrictionsMapper.get(restriction.getClass()).map(restriction)
                        : null)
                .filter(Objects::nonNull)
                .toList();
    }
}
