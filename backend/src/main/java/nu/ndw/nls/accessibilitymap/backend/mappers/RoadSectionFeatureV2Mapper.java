package nu.ndw.nls.accessibilitymap.backend.mappers;

import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionPropertiesJson;
import nu.ndw.nls.geojson.geometry.mappers.JtsLineStringJsonMapper;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Component;

/**
 * This class is responsible for mapping a {@link RoadSection} instance to a {@link RoadSectionFeatureJson}. It handles conversions,
 * computations, and transformations required to prepare data for GeoJSON serialization.
 * <p></p>
 * The mapper determines unique feature identifiers, accessibility status, geometry transformations, and match status based on the provided
 * input parameters. It ensures that the forward or reverse geometries of the road section are appropriately computed, and relevant
 * properties are set in the resulting JSON object.
 */
@Component
@RequiredArgsConstructor
public class RoadSectionFeatureV2Mapper {

    private final JtsLineStringJsonMapper jtsLineStringJsonMapper;

    /**
     * Maps a {@link RoadSection} to a {@link RoadSectionFeatureJson}, converting geometric and property-related data into a JSON-friendly
     * format. Computes the feature ID, geometry, accessibility, and match status based on the provided parameters.
     *
     * @param roadSection the road section to be converted
     * @param matched     the match status of the road section
     * @param forward     a boolean indicating whether to use forward-facing data (if true) or backward-facing data (if false) for geometry
     *                    and accessibility
     * @return a {@link RoadSectionFeatureJson} instance containing the transformed data
     */
    public RoadSectionFeatureJson map(
            RoadSection roadSection,
            Boolean matched,
            boolean forward
    ) {
        int id = (int) (forward ? roadSection.getId() : -roadSection.getId());
        LineString geometry = forward ? roadSection.getMergedForwardGeometry() : roadSection.getMergedBackWardGeometry();
        boolean accessible = forward ? roadSection.isForwardAccessible() : roadSection.isBackwardAccessible();
        return new RoadSectionFeatureJson(
                RoadSectionFeatureJson.TypeEnum.FEATURE,
                id,
                jtsLineStringJsonMapper.map(geometry),
                new RoadSectionPropertiesJson(accessible, matched));
    }


}
