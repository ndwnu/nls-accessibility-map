package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mappers.response;

import jakarta.annotation.Nullable;
import java.util.List;
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

    private final RoadSectionFeatureMapper roadSectionFeatureV2Mapper;

    /**
     * Maps the specified accessibility information into a `RoadSectionFeatureCollectionJson` object.
     *
     * @param accessibility the accessibility details containing collections of road sections
     * @param startPointRequested indicates whether the start point was requested
     * @param startPointMatch optional parameter representing the match for the starting point
     * @param accessible optional parameter indicating if accessibility should be considered
     * @return a `RoadSectionFeatureCollectionJson` containing a collection of mapped road section features
     */
    public RoadSectionFeatureCollectionJson map(
            Accessibility accessibility,
            boolean startPointRequested,
            @Nullable CandidateMatch startPointMatch,
            @Nullable Boolean accessible
    ) {

        List<RoadSectionFeatureJson> features = accessibility.combinedAccessibility().stream()
                .map(r -> roadSectionFeatureV2Mapper.map(r, startPointRequested, startPointMatch, accessible))
                .flatMap(roadSectionFeatureJsons -> roadSectionFeatureJsons.stream())
                .toList();

        return new RoadSectionFeatureCollectionJson(TypeEnum.FEATURE_COLLECTION, features);
    }


}
