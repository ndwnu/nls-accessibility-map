package nu.ndw.nls.accessibilitymap.accessibility.nwb.converter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.io.Serial;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;

public class LineStringSerializer extends StdSerializer<LineString> {

    @Serial
    private static final long serialVersionUID = 1L;

    private ObjectMapper objectMapper;

    protected LineStringSerializer() {
        super(LineString.class);
    }


    @Override
    public void serialize(
            LineString lineString,
            JsonGenerator gen,
            SerializerProvider serializers
    ) throws IOException {

        Coordinate[] coords = lineString.getCoordinates();

        gen.writeStartArray();

        for (int i = 0; i < coords.length; i++) {
            Coordinate c = coords[i];

            gen.writeStartArray(2);
            gen.writeNumber(c.getX());
            gen.writeNumber(c.getY());
            gen.writeEndArray();
        }

        gen.writeEndArray();
    }

}
