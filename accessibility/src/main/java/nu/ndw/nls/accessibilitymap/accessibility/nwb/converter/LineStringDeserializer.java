package nu.ndw.nls.accessibilitymap.accessibility.nwb.converter;

import tools.jackson.core.JsonParser;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;
import java.io.Serial;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

public class LineStringDeserializer extends StdDeserializer<LineString> {

    @Serial
    private static final long serialVersionUID = 1L;

    protected LineStringDeserializer() {
        super(LineString.class);
    }

    @Override
    public LineString deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
        TypeReference<List<double[]>> typeRef = new TypeReference<>() {
        };

        List<double[]> coordinates = jsonParser.objectReadContext().readValue(jsonParser, typeRef);

        GeometryFactory geometryFactory = new GeometryFactory();

        return geometryFactory.createLineString(coordinates.stream()
                .map(coordinate -> new Coordinate(coordinate[0], coordinate[1]))
                .toArray(Coordinate[]::new));
    }
}
