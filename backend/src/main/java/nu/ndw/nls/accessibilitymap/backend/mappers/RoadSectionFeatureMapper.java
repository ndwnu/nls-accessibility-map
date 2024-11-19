package nu.ndw.nls.accessibilitymap.backend.mappers;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.model.RoadSection;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.GeometryJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.LineStringJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionPropertiesJson;
import nu.ndw.nls.geometry.geojson.mappers.GeoJsonLineStringCoordinateMapper;
import nu.ndw.nls.geometry.rounding.dto.RoundDoubleConfiguration;
import nu.ndw.nls.routingmapmatcher.model.singlepoint.SinglePointMatch.CandidateMatch;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoadSectionFeatureMapper {

    private final GeoJsonLineStringCoordinateMapper geoJsonLineStringCoordinateMapper;

    public RoadSectionFeatureJson map(
            RoadSection roadSection,
            boolean startPointRequested,
            @Nullable CandidateMatch startPointMatch,
            boolean forward
    ) {
        int id = forward ? roadSection.getRoadSectionId() : -roadSection.getRoadSectionId();
        LineString geometry = forward ? roadSection.getGeometry() : roadSection.getGeometry().reverse();
        Boolean accessible = forward ? roadSection.getForwardAccessible() : roadSection.getBackwardAccessible();
        Boolean matched = mapMatched(roadSection, startPointRequested, startPointMatch, forward);

        return new RoadSectionFeatureJson(RoadSectionFeatureJson.TypeEnum.FEATURE, id,
                new LineStringJson(geoJsonLineStringCoordinateMapper.map(geometry,
                        RoundDoubleConfiguration.ROUND_7_HALF_UP), TypeEnum.LINE_STRING))
                .properties(new RoadSectionPropertiesJson(accessible).matched(matched));
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

        return roadSection.getRoadSectionId() == startPointMatch.getMatchedLinkId() && forward != startPointMatch.isReversed();
    }
}
