package nu.ndw.nls.accessibilitymap.backend.mappers;

import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.model.RoadSection;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureCollectionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureCollectionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureJson;
import nu.ndw.nls.routingmapmatcher.model.singlepoint.SinglePointMatch.CandidateMatch;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoadSectionFeatureCollectionMapper {

    private final RoadSectionFeatureMapper roadSectionFeatureMapper;

    public RoadSectionFeatureCollectionJson map(
            Map<Integer, RoadSection> idToRoadSectionMap,
            boolean startPointRequested,
            @Nullable CandidateMatch startPointMatch,
            @Nullable Boolean accessible
    ) {
        List<RoadSectionFeatureJson> features = idToRoadSectionMap.values().stream()
                .flatMap(r -> Stream.of(
                        roadSectionFeatureMapper.map(r, startPointRequested, startPointMatch, true),
                        roadSectionFeatureMapper.map(r, startPointRequested, startPointMatch, false)
                ))
                .filter(r -> r.getProperties().getAccessible() != null)
                .filter(r -> accessible == null || accessible.equals(r.getProperties().getAccessible())
                             || Boolean.TRUE.equals(r.getProperties().getMatched()))
                .toList();
        return new RoadSectionFeatureCollectionJson(TypeEnum.FEATURE_COLLECTION, features);
    }
}
