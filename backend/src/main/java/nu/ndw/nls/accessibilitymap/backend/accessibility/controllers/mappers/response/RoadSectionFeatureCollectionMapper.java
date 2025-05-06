package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mappers.response;

import jakarta.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureCollectionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureCollectionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureJson;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoadSectionFeatureCollectionMapper {

    private final RoadSectionFeatureMapper roadSectionFeatureMapper;

    public RoadSectionFeatureCollectionJson map(
            Accessibility accessibility,
            boolean startPointHasBeenRequested,
            @Nullable Long matchedStartPointRoadSectionId,
            @Nullable Boolean filterOutWithAccessibility) {

        List<RoadSectionFeatureJson> features = accessibility.combinedAccessibility().stream()
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
