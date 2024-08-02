package nu.ndw.nls.accessibilitymap.backend.mappers;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.GeometryJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.LineStringJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionPropertiesJson;
import nu.ndw.nls.accessibilitymap.shared.accessibility.model.RoadSection;
import nu.ndw.nls.geometry.geojson.mappers.GeoJsonLineStringCoordinateMapper;
import nu.ndw.nls.geometry.rounding.dto.RoundDoubleConfiguration;
import nu.ndw.nls.routingmapmatcher.model.singlepoint.SinglePointMatch.CandidateMatch;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoadSectionFeatureMapper {

    private final GeoJsonLineStringCoordinateMapper geoJsonLineStringCoordinateMapper;

    public RoadSectionFeatureJson map(RoadSection roadSection, @Nullable CandidateMatch candidateMatch,
            boolean forward) {
        int id = forward ? roadSection.getRoadSectionId() : -roadSection.getRoadSectionId();
        LineString geometry = forward ? roadSection.getGeometry() : roadSection.getGeometry().reverse();
        Boolean accessible = forward ? roadSection.getForwardAccessible() : roadSection.getBackwardAccessible();
        Boolean matched = candidateMatch != null ? (roadSection.getRoadSectionId() == candidateMatch.getMatchedLinkId()
                && forward != candidateMatch.isReversed()) : null;

        return new RoadSectionFeatureJson(RoadSectionFeatureJson.TypeEnum.FEATURE, id,
                new LineStringJson(geoJsonLineStringCoordinateMapper.map(geometry,
                        RoundDoubleConfiguration.ROUND_7_HALF_UP), TypeEnum.LINE_STRING))
                .properties(new RoadSectionPropertiesJson(accessible).matched(matched));
    }
}
