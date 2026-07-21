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
import nu.ndw.nls.accessibilitymap.backend.roadoperator.repository.dto.RoadOperator;
import nu.ndw.nls.accessibilitymap.backend.roadoperator.service.RoadOperatorService;
import org.springframework.stereotype.Component;

@Component
public class AccessibilityReasonsJsonMapperV2 {

    @SuppressWarnings({"java:S3740", "java:S6411"})
    private final Map<ReasonType, AccessibilityReasonJsonMapperV2> reasonMapper;

    @SuppressWarnings({"java:S3740", "java:S6411"})
    private final Map<Class<? extends Restriction>, AccessibilityRestrictionJsonMapperV2> restrictionsMapper;

    private final RoadOperatorService roadOperatorService;

    @SuppressWarnings("java:S3740")
    public AccessibilityReasonsJsonMapperV2(
            List<AccessibilityReasonJsonMapperV2<?>> restrictionMapperList,
            List<AccessibilityRestrictionJsonMapperV2<?>> restrictionsMapperList,
            RoadOperatorService roadOperatorService) {

        reasonMapper = restrictionMapperList.stream()
                .collect(Collectors.toMap(AccessibilityReasonJsonMapperV2::getReasonType, Function.identity()));

        restrictionsMapper = restrictionsMapperList.stream()
                .collect(Collectors.toMap(AccessibilityRestrictionJsonMapperV2::getRestrictionType, Function.identity()));

        this.roadOperatorService = roadOperatorService;
    }

    public List<List<ReasonJson>> map(List<AccessibilityReasonGroup> reasonGroups) {
        Map<String, String> codeToExemptionUrl = roadOperatorService.findAll().stream()
                .filter(roadOperator -> roadOperator.requestExemptionUrl() != null)
                .collect(Collectors.toMap(
                        RoadOperator::roadOperatorCode,
                        roadOperator -> roadOperator.requestExemptionUrl().toString(),
                        (a, b) -> a));

        return reasonGroups.stream()
                .map(reasonsList -> reasonsList.stream()
                        .map(reason -> mapReasonToReasonJson(reason, codeToExemptionUrl))
                        .filter(Objects::nonNull)
                        .toList())
                .toList();
    }

    @SuppressWarnings("unchecked")
    private ReasonJson mapReasonToReasonJson(AccessibilityReason<?> reason, Map<String, String> codeToExemptionUrl) {
        if (!reasonMapper.containsKey(reason.getReasonType())) {
            return null;
        }

        ReasonJson reasonJson = reasonMapper.get(reason.getReasonType())
                .map(reason, mapReasonRestrictionsToRestrictionJson(reason.getRestrictions()));
        if (reasonJson == null) {
            return null;
        }

        Set<String> codes = reason.getRoadOperatorCodes() == null ? Set.of() : reason.getRoadOperatorCodes();
        List<String> urls = codes.stream()
                .map(codeToExemptionUrl::get)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
        reasonJson.setRequestExemptionUrls(urls);

        return reasonJson;
    }

    @SuppressWarnings("unchecked")
    private List<RestrictionJson> mapReasonRestrictionsToRestrictionJson(Set<Restriction> restrictions) {
        return restrictions.stream()
                .map(restriction -> restrictionsMapper.containsKey(restriction.getClass())
                        ? restrictionsMapper.get(restriction.getClass()).map(restriction)
                        : null)
                .filter(Objects::nonNull)
                .toList();
    }
}
