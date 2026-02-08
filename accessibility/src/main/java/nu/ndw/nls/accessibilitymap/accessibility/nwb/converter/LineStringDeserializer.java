package nu.ndw.nls.accessibilitymap.accessibility.nwb.converter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

public class LineStringDeserializer extends StdDeserializer<LineString> {

    protected LineStringDeserializer() {
        super(LineString.class);
    }

    @Override
    public LineString deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        TypeReference<List<double[]>> typeRef = new TypeReference<>() {
        };

        List<double[]> coordinates = jsonParser.getCodec().readValue(jsonParser, typeRef);

        GeometryFactory geometryFactory = new GeometryFactory();

        return geometryFactory.createLineString(coordinates.stream()
                .map(coordinate -> new Coordinate(coordinate[0], coordinate[1]))
                .toArray(Coordinate[]::new));
    }
}
