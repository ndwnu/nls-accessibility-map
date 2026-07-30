package nu.ndw.nls.accessibilitymap.accessibility.service.debug.services;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

@Component
public class BoundingBoxService {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    private final GeometryFactory geometryMapper = new GeometryFactory();

    public void boundTargetFileToBoundingBoxFile(File boundingBoxFile, File targetFileToBound, File outputFile) {
        Envelope bbox = computeBoundingBox(boundingBoxFile);

        JsonNode target = jsonMapper.readTree(targetFileToBound);
        List<JsonNode> keptFeatures = new ArrayList<>();
        for (JsonNode feature : target.get("features")) {
            Geometry geometry = toJtsGeometry(feature.get("geometry"));
            if (geometry.getEnvelopeInternal().intersects(bbox)) {
                keptFeatures.add(feature);
            }
        }

        ObjectNode result = jsonMapper.createObjectNode();
        result.put("type", "FeatureCollection");
        result.putArray("features").addAll(keptFeatures);
        jsonMapper.writeValue(outputFile, result);
    }

    private Envelope computeBoundingBox(File geoJsonFile) {
        JsonNode root = jsonMapper.readTree(geoJsonFile);
        Envelope envelope = new Envelope();
        for (JsonNode feature : root.get("features")) {
            envelope.expandToInclude(toJtsGeometry(feature.get("geometry")).getEnvelopeInternal());
        }
        return envelope;
    }

    private Geometry toJtsGeometry(JsonNode geometryNode) {
        JsonNode coords = geometryNode.get("coordinates");
        return switch (geometryNode.get("type").asString()) {
            case "Point" -> geometryMapper.createPoint(toCoordinate(coords));
            case "LineString" -> geometryMapper.createLineString(toCoordinateArray(coords));
            case "Polygon" -> geometryMapper.createPolygon(toCoordinateArray(coords.get(0)));
            default -> throw new IllegalArgumentException("Unsupported geometry type: " + geometryNode.get("type").asString());
        };
    }

    private Coordinate toCoordinate(JsonNode point) {
        return new Coordinate(point.get(0).asDouble(), point.get(1).asDouble());
    }

    private Coordinate[] toCoordinateArray(JsonNode coords) {
        Coordinate[] result = new Coordinate[coords.size()];
        int i = 0;
        for (JsonNode c : coords) {
            result[i++] = toCoordinate(c);
        }
        return result;
    }
}
