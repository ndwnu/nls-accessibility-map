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

/**
 * This class is responsible for mapping a {@link RoadSection} instance to a
 * {@link RoadSectionFeatureJson}. It handles conversions, computations, and transformations
 * required to prepare data for GeoJSON serialization.
 *<p></p>
 * The mapper determines unique feature identifiers, accessibility status, geometry transformations,
 * and match status based on the provided input parameters. It ensures that the forward or reverse
 * geometries of the road section are appropriately computed, and relevant properties are set in the
 * resulting JSON object.
 */
@Component
@RequiredArgsConstructor
public class RoadSectionFeatureV2Mapper {

    private final JtsLineStringJsonMapper jtsLineStringJsonMapper;

    /**
     * Maps a {@link RoadSection} object to a {@link RoadSectionFeatureJson}.
     * The method handles geometry transformation, accessibility status,
     * and match determination based on input parameters.
     *
     * @param roadSection the road section to be mapped
     * @param startPointRequested a flag indicating if the start point is requested
     * @param startPointMatch an optional candidate match for the start point
     * @param forward a flag indicating the direction (forward or reverse) for mapping
     * @return a {@link RoadSectionFeatureJson} representing the mapped road section
     */
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
