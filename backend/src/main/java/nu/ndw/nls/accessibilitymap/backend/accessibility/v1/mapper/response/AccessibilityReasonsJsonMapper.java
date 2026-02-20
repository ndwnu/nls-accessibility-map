package nu.ndw.nls.accessibilitymap.backend.accessibility.v1.mapper.response;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason.ReasonType;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReasonGroup;
import nu.ndw.nls.accessibilitymap.backend.accessibility.v1.mapper.response.restriction.AccessibilityRestrictionJsonMapper;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.ReasonJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.TrafficSignTypeJson;
import org.springframework.stereotype.Component;

@Component
public class AccessibilityReasonsJsonMapper {

    @SuppressWarnings("java:S3740")
    private final Map<ReasonType, AccessibilityRestrictionJsonMapper> restrictionMapperMap;

    @SuppressWarnings("java:S3740")
    public AccessibilityReasonsJsonMapper(
            List<AccessibilityRestrictionJsonMapper> restrictionMapperList) {

        restrictionMapperMap = restrictionMapperList.stream()
                .collect(Collectors.toMap(AccessibilityRestrictionJsonMapper::mapperForType, Function.identity()));
    }

    public List<List<ReasonJson>> mapToReasonJson(List<AccessibilityReasonGroup> reasons) {
        return reasons.stream()
                .map(reasonsList -> reasonsList.stream()
                        .map(reason -> {
                            Optional<Restriction> restriction = reason.getRestrictions().stream().findFirst();
                            if (restriction.isPresent() && restriction.get() instanceof TrafficSign trafficSign) {
                                return new ReasonJson()
                                        .trafficSignId(UUID.fromString(trafficSign.externalId()))
                                        .trafficSignType(TrafficSignTypeJson.fromValue(trafficSign.trafficSignType().getRvvCode()))
                                        .restrictions(mapToRestrictionJson(reason).stream().toList());
                            } else {
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .toList())
                .toList();
    }

    @SuppressWarnings("java:S3740")
    private Optional<RestrictionJson> mapToRestrictionJson(AccessibilityReason accessibilityReason) {
        return restrictionMapperMap.containsKey(accessibilityReason.getReasonType())
                ? Optional.of(restrictionMapperMap.get(accessibilityReason.getReasonType()).map(accessibilityReason))
                : Optional.empty();
    }
}
