package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mappers.response;

import jakarta.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureCollectionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureCollectionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureJson;
import nu.ndw.nls.routingmapmatcher.model.singlepoint.SinglePointMatch.CandidateMatch;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoadSectionFeatureCollectionMapper {

    private final RoadSectionFeatureMapper roadSectionFeatureMapper;

    /**
     * Maps the specified accessibility information into a `RoadSectionFeatureCollectionJson` object.
     *
     * @param accessibility the accessibility details containing collections of road sections
     * @param startPoint    optional parameter representing the starting point
     * @param accessible    optional parameter indicating if accessibility should be considered
     * @return a `RoadSectionFeatureCollectionJson` containing a collection of mapped road section features
     */
    public RoadSectionFeatureCollectionJson map(
            Accessibility accessibility,
            CandidateMatch startPoint,
            @Nullable Boolean accessible
    ) {

        List<RoadSectionFeatureJson> features = accessibility.combinedAccessibility().stream()
                .map(r -> roadSectionFeatureMapper.map(r, Objects.nonNull(startPoint), startPoint, accessible))
                .flatMap(Collection::stream)
                .toList();

        return new RoadSectionFeatureCollectionJson(TypeEnum.FEATURE_COLLECTION, features);
    }

}
