package nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.request.mapper;

import com.graphhopper.util.shapes.BBox;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest.AccessibilityRequestBuilder;
import nu.ndw.nls.accessibilitymap.backend.accessibility.v2.configuration.BoundingBoxAreaConfiguration;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.AreaRequestJson;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AccessibilityRequestBuilderAreaMapper {

    private static final int METERS_PER_DEGREE = 111_320;

    @Getter
    private final BoundingBoxAreaConfiguration boundingBoxAreaConfiguration;

    public abstract void build(AccessibilityRequestBuilder accessibilityRequestBuilder, AreaRequestJson areaRequestJson);

    public abstract boolean canProcessAreaRequest(AreaRequestJson areaRequestJson);

    protected BBox applySearchDistance(BBox boundingBox, double searchDistanceGapFromRequestedSearchArea) {
        double expansionInDegrees = searchDistanceGapFromRequestedSearchArea / METERS_PER_DEGREE;

        return BBox.fromPoints(
                boundingBox.minLat - expansionInDegrees,
                boundingBox.minLon - expansionInDegrees,
                boundingBox.maxLat + expansionInDegrees,
                boundingBox.maxLon + expansionInDegrees
        );
    }
}
