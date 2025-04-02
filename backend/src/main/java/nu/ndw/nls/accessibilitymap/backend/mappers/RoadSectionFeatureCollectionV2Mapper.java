package nu.ndw.nls.accessibilitymap.backend.mappers;

import jakarta.annotation.Nullable;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.services.accessibility.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureCollectionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureCollectionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureJson;
import nu.ndw.nls.routingmapmatcher.model.singlepoint.SinglePointMatch.CandidateMatch;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoadSectionFeatureCollectionV2Mapper {

    private final RoadSectionFeatureV2Mapper roadSectionFeatureV2Mapper;

    /**
     * Maps the provided accessibility information, filtering and transforming data
     * into a collection of road section feature JSON objects.
     *
     * @param accessibility the accessibility object containing road section data
     * @param startPointRequested a flag indicating if the starting point is requested
     * @param startPointMatch an optional candidate match for the starting point
     * @param accessible an optional flag to filter road sections by their accessibility status
     * @return a RoadSectionFeatureCollectionJson object containing type and features
     */
    public RoadSectionFeatureCollectionJson map(
            Accessibility accessibility,
            boolean startPointRequested,
            @Nullable CandidateMatch startPointMatch,
            @Nullable Boolean accessible
    ) {
        List<RoadSectionFeatureJson> features = accessibility.combinedAccessibility().stream()
                .flatMap(r -> Stream.of(
                        roadSectionFeatureV2Mapper.map(r, startPointRequested, startPointMatch, true),
                        roadSectionFeatureV2Mapper.map(r, startPointRequested, startPointMatch, false)
                ))
                .filter(r -> (r.getProperties() != null ? r.getProperties().getAccessible() : null) != null)
                .filter(r -> accessible == null || accessible.equals(r.getProperties().getAccessible())
                        || Boolean.TRUE.equals(r.getProperties().getMatched()))
                .toList();
        return new RoadSectionFeatureCollectionJson(TypeEnum.FEATURE_COLLECTION, features);
    }
}
