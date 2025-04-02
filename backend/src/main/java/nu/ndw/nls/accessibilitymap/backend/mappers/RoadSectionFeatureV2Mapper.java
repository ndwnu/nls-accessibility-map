package nu.ndw.nls.accessibilitymap.backend.mappers;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionPropertiesJson;
import nu.ndw.nls.geojson.geometry.mappers.JtsLineStringJsonMapper;
import nu.ndw.nls.routingmapmatcher.model.singlepoint.SinglePointMatch.CandidateMatch;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoadSectionFeatureV2Mapper {

    private final JtsLineStringJsonMapper jtsLineStringJsonMapper;

    public RoadSectionFeatureJson map(
            RoadSection roadSection,
            boolean startPointRequested,
            @Nullable CandidateMatch startPointMatch,
            boolean forward
    ) {
        int id = (int) (forward ? roadSection.getId() : -roadSection.getId());
        LineString geometry = forward ? roadSection.getMergedForwardGeometry() : roadSection.getMergedForwardGeometry().reverse();
        Boolean accessible = forward ? roadSection.isForwardAccessible() : roadSection.isBackwardAccessible();
        Boolean matched = mapMatched(roadSection, startPointRequested, startPointMatch, forward);

        return new RoadSectionFeatureJson(
                RoadSectionFeatureJson.TypeEnum.FEATURE,
                id,
                jtsLineStringJsonMapper.map(geometry),
                new RoadSectionPropertiesJson(accessible, matched));
    }

    private static @Nullable Boolean mapMatched(
            RoadSection roadSection,
            boolean startPointPresent,
            @Nullable CandidateMatch startPointMatch,
            boolean forward
    ) {
        if (!startPointPresent) {
            return null;
        }

        if (startPointMatch == null) {
            return false;
        }

        return roadSection.getId() == startPointMatch.getMatchedLinkId() && forward != startPointMatch.isReversed();
    }
}
