package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.routing;

import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;

import com.graphhopper.routing.Path.EdgeVisitor;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.FetchMode;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityReasons;
import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
import org.apache.commons.io.FileUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.wololo.geojson.Feature;
import org.wololo.geojson.GeoJSON;
import org.wololo.jts2geojson.GeoJSONWriter;

@RequiredArgsConstructor
public class RouteWithTrafficSignsGeoJsonVisitor implements EdgeVisitor {

    private final AccessibilityReasons accessibilityReasons;
    private final EncodingManager encodingManager;
    private final EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor;
    GeometryFactoryWgs84 geometryFactoryWgs84 = new GeometryFactoryWgs84();
    GeoJSONWriter writer = new GeoJSONWriter();
    List<Feature> featuresJson = new ArrayList<>();
    AtomicInteger idCount = new AtomicInteger();

    @Override
    public void next(EdgeIteratorState edgeIteratorState, int index, int prevEdgeId) {
        Map<String, Object> properties = new HashMap<>();
        LineString lineString = edgeIteratorState.fetchWayGeometry(FetchMode.ALL).toLineString(false);
        var geometry = writer.write(lineString);
        int roadSectionId = getLinkId(encodingManager, edgeIteratorState);
        Direction direction = edgeIteratorStateReverseExtractor.hasReversed(edgeIteratorState) ? Direction.BACKWARD : Direction.FORWARD;
        properties.put("roadSectionId", roadSectionId);
        featuresJson.add(new Feature(idCount.getAndIncrement(), geometry, properties));
        if (accessibilityReasons.hasReasons(roadSectionId, direction)) {

            accessibilityReasons.getReasonsByRoadSectionAndDirection(roadSectionId, direction)
                    .stream().forEach(reason -> {
                                Map<String, Object> trafficSignProperties = new HashMap<>();
                                Point point = geometryFactoryWgs84.createPoint(
                                        //Latitude is the Y axis, longitude is the X axis
                                        new Coordinate(reason.trafficSign().networkSnappedLongitude(), reason.trafficSign().networkSnappedLatitude()));
                                var pointGeoJson = writer.write(point);
                                trafficSignProperties.put("roadSectionId", roadSectionId);
                                trafficSignProperties
                                        .put("rvv", reason.trafficSign().trafficSignType().name());
                                trafficSignProperties
                                        .put("icon", reason.trafficSign().iconUri());
                                featuresJson.add(new Feature(idCount.getAndIncrement(), pointGeoJson, trafficSignProperties));
                            }

                    );
        }
    }

    @Override
    public void finish() {
        GeoJSON json = writer.write(featuresJson);
        try {
            FileUtils.writeStringToFile(new File("/tmp/route_" + UUID.randomUUID() + ".geojson"), json.toString(),

                    Charset.defaultCharset().name());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int getLinkId(EncodingManager encodingManager, EdgeIteratorState edge) {

        return edge.get(encodingManager.getIntEncodedValue(WAY_ID_KEY));
    }
}
