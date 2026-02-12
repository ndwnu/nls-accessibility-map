package nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.request.mapper;

import com.graphhopper.util.shapes.BBox;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest.AccessibilityRequestBuilder;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.AreaRequestJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.BoundingBoxAreaRequestJson;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.operation.distance.DistanceOp;
import org.springframework.stereotype.Component;

@Component
public class AccessibilityRequestMapperBoundingBoxAreaMapper implements AccessibilityRequestBuilderAreaMapper {

    private static final int METERS_PER_DEGREE = 111_320;

    private static final double SEARCH_GRID_DISTANCE_FROM_REQUEST_AREA = 10_000.0; // 10KM

    private final GeometryFactory geometryFactory;

    public AccessibilityRequestMapperBoundingBoxAreaMapper() {
        geometryFactory = new GeometryFactory();
    }

    public void build(AccessibilityRequestBuilder accessibilityRequestBuilder, AreaRequestJson areaRequestJson) {
        if (areaRequestJson instanceof BoundingBoxAreaRequestJson boundingBoxAreaRequestJson) {
            BBox requestArea = BBox.fromPoints(
                    boundingBoxAreaRequestJson.getMinLatitude(),
                    boundingBoxAreaRequestJson.getMinLongitude(),
                    boundingBoxAreaRequestJson.getMaxLatitude(),
                    boundingBoxAreaRequestJson.getMaxLongitude()
            );

            double expansionInDegrees = SEARCH_GRID_DISTANCE_FROM_REQUEST_AREA / METERS_PER_DEGREE;
            BBox searchArea = BBox.fromPoints(
                    requestArea.minLat - expansionInDegrees,
                    requestArea.minLon - expansionInDegrees,
                    requestArea.maxLat + expansionInDegrees,
                    requestArea.maxLon + expansionInDegrees
            );

            accessibilityRequestBuilder
                    .requestArea(requestArea)
                    .searchArea(searchArea)
                    .maxSearchDistanceInMeters(calculateMaxDistanceInMeters(searchArea));
        } else {
            throw new IllegalArgumentException("AreaRequestJson must be of type BoundingBoxAreaRequestJson");
        }
    }

    @Override
    public boolean canProcessAreaRequest(AreaRequestJson areaRequestJson) {
        return areaRequestJson instanceof BoundingBoxAreaRequestJson;
    }

    private double calculateMaxDistanceInMeters(BBox searchArea) {
        double maxDistance = DistanceOp.distance(
                geometryFactory.createPoint(new Coordinate(searchArea.minLon, searchArea.minLat)),
                geometryFactory.createPoint(new Coordinate(searchArea.maxLon, searchArea.maxLat)));
        return maxDistance * METERS_PER_DEGREE;
    }
}
