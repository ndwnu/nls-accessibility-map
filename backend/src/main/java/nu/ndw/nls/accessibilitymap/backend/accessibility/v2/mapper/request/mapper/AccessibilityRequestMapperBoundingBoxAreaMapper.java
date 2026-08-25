package nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.request.mapper;

import com.graphhopper.util.shapes.BBox;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest.AccessibilityRequestBuilder;
import nu.ndw.nls.accessibilitymap.backend.accessibility.v2.configuration.BoundingBoxAreaConfiguration;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.AreaRequestJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.BoundingBoxAreaRequestJson;
import org.springframework.stereotype.Component;

@Component
public class AccessibilityRequestMapperBoundingBoxAreaMapper extends AccessibilityRequestBuilderAreaMapper {

    protected AccessibilityRequestMapperBoundingBoxAreaMapper(
            BoundingBoxAreaConfiguration boundingBoxAreaConfiguration) {
        super(boundingBoxAreaConfiguration);
    }

    public void build(AccessibilityRequestBuilder accessibilityRequestBuilder, AreaRequestJson areaRequestJson) {
        if (areaRequestJson instanceof BoundingBoxAreaRequestJson boundingBoxAreaRequestJson) {
            BBox requestArea = BBox.fromPoints(
                    boundingBoxAreaRequestJson.getMinLatitude(),
                    boundingBoxAreaRequestJson.getMinLongitude(),
                    boundingBoxAreaRequestJson.getMaxLatitude(),
                    boundingBoxAreaRequestJson.getMaxLongitude()
            );

            BBox searchArea = applySearchDistance(
                    requestArea,
                    getBoundingBoxAreaConfiguration().getSearchDistanceGapFromRequestedSearchAreaInMeters());

            accessibilityRequestBuilder
                    .requestArea(requestArea)
                    .searchArea(searchArea);
        } else {
            throw new IllegalArgumentException("AreaRequestJson must be of type BoundingBoxAreaRequestJson");
        }
    }

    @Override
    public boolean canProcessAreaRequest(AreaRequestJson areaRequestJson) {
        return areaRequestJson instanceof BoundingBoxAreaRequestJson;
    }
}
