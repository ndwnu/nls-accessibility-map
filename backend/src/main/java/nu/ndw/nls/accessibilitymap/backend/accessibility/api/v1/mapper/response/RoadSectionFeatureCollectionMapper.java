package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.mapper.response;

import jakarta.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.generated.model.v1.RoadSectionFeatureCollectionJson;
import nu.ndw.nls.accessibilitymap.generated.model.v1.RoadSectionFeatureCollectionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.generated.model.v1.RoadSectionFeatureJson;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoadSectionFeatureCollectionMapper {

    private final RoadSectionFeatureMapper roadSectionFeatureMapper;

    public RoadSectionFeatureCollectionJson map(
            Collection<RoadSection> roadSections,
            boolean startPointHasBeenRequested,
            @Nullable Long matchedStartPointRoadSectionId,
            @Nullable Boolean filterOutWithAccessibility) {

        List<RoadSectionFeatureJson> features = roadSections.stream()
                .map(roadSection -> roadSectionFeatureMapper.map(
                        roadSection,
                        startPointHasBeenRequested,
                        matchedStartPointRoadSectionId,
                        filterOutWithAccessibility))
                .flatMap(Collection::stream)
                .toList();

        return new RoadSectionFeatureCollectionJson(TypeEnum.FEATURE_COLLECTION, features);
    }

}
