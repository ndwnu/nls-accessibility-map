package nu.ndw.nls.accessibilitymap.accessibility.nwb.converter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.locationtech.jts.geom.LineString;

public class LineStringSerializer extends StdSerializer<LineString> {

    private static final String COORDINATES = "coordinates";

    private ObjectMapper objectMapper;

    protected LineStringSerializer() {
        super(LineString.class);
    }


    @Override
    public void serialize(LineString lineString, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        List<double[]> coordinates = Arrays.stream(lineString.getCoordinates())
                .map(coordinate -> new double[]{coordinate.getX(), coordinate.getY()})
                .toList();
        jsonGenerator.writeRawValue(getObjectMapper().writeValueAsString(coordinates));
    }


    private ObjectMapper getObjectMapper() {
        if (Objects.isNull(objectMapper)) {
            objectMapper = new ObjectMapper();
        }
        return objectMapper;
    }


}
