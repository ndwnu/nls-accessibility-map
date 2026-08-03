package nu.ndw.nls.accessibilitymap.accessibility.service.debug.services;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

@Component
@RequiredArgsConstructor
public class GeoJsonBoundingService {

    private static final String FEATURE_TYPE_FEATURE_COLLECTION = "FeatureCollection";

    private static final String FEATURE_TYPE_POINT = "Point";

    private static final String FEATURE_TYPE_LINE_STRING = "LineString";

    private static final String FEATURE_TYPE_POLYGON = "Polygon";

    private static final String FIELD_FEATURES = "features";

    private static final String FIELD_GEOMETRY = "geometry";

    private static final String FIELD_COORDINATES = "coordinates";

    private static final int INDEX_LONGITUDE_X = 0;

    private static final int INDEX_LATITUDE_Y = 1;

    private static final String TYPE = "type";

    private final JsonMapper jsonMapper;

    private final GeometryFactoryWgs84 geometryMapper;

    public void boundTargetFileToBoundingBoxFile(File sourceForBounds, File sourceToBound, File outputFile) {
        Envelope bbox = computeBoundingBox(sourceForBounds);

        JsonNode target = jsonMapper.readTree(sourceToBound);
        List<JsonNode> keptFeatures = new ArrayList<>();
        for (JsonNode feature : target.get(FIELD_FEATURES)) {
            Geometry geometry = toJtsGeometry(feature.get(FIELD_GEOMETRY));
            if (geometry.getEnvelopeInternal().intersects(bbox)) {
                keptFeatures.add(feature);
            }
        }

        ObjectNode result = jsonMapper.createObjectNode();
        result.put(TYPE, FEATURE_TYPE_FEATURE_COLLECTION);
        result.putArray(FIELD_FEATURES).addAll(keptFeatures);
        jsonMapper.writeValue(outputFile, result);
    }

    private Envelope computeBoundingBox(File geoJsonFile) {
        JsonNode root = jsonMapper.readTree(geoJsonFile);
        Envelope envelope = new Envelope();
        for (JsonNode feature : root.get(FIELD_FEATURES)) {
            envelope.expandToInclude(toJtsGeometry(feature.get(FIELD_GEOMETRY)).getEnvelopeInternal());
        }
        return envelope;
    }

    private Geometry toJtsGeometry(JsonNode geometryNode) {
        JsonNode coords = geometryNode.get(FIELD_COORDINATES);
        return switch (geometryNode.get(TYPE).asString()) {
            case FEATURE_TYPE_POINT -> geometryMapper.createPoint(toCoordinate(coords));
            case FEATURE_TYPE_LINE_STRING -> geometryMapper.createLineString(toCoordinateArray(coords));
            case FEATURE_TYPE_POLYGON -> geometryMapper.createPolygon(toCoordinateArray(coords.get(INDEX_LONGITUDE_X)));
            default -> throw new IllegalArgumentException("Unsupported geometry type: " + geometryNode.get(TYPE).asString());
        };
    }

    private Coordinate toCoordinate(JsonNode point) {
        return new Coordinate(point.get(INDEX_LONGITUDE_X).asDouble(), point.get(INDEX_LATITUDE_Y).asDouble());
    }

    @SuppressWarnings("java:S881")
    private Coordinate[] toCoordinateArray(JsonNode coords) {
        Coordinate[] result = new Coordinate[coords.size()];
        int i = 0;
        for (JsonNode c : coords) {
            result[i++] = toCoordinate(c);
        }
        return result;
    }
}
