package nu.ndw.nls.accessibilitymap.accessibility.nwb.converter;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.ser.std.StdSerializer;
import java.io.Serial;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.locationtech.jts.geom.LineString;
import tools.jackson.databind.SerializationContext;

public class LineStringSerializer extends StdSerializer<LineString> {

    @Serial
    private static final long serialVersionUID = 1L;

    private ObjectMapper objectMapper;

    protected LineStringSerializer() {
        super(LineString.class);
    }


    @Override
    public void serialize(LineString lineString, JsonGenerator jsonGenerator, SerializationContext serializerProvider) {

        List<double[]> coordinates = Arrays.stream(lineString.getCoordinates())
                .map(coordinate -> new double[]{coordinate.getX(), coordinate.getY()})
                .toList();
        jsonGenerator.writeRawValue(getObjectMapper().writeValueAsString(coordinates));
    }


    private ObjectMapper getObjectMapper() {
        if (Objects.isNull(objectMapper)) {
            objectMapper = new JsonMapper();
        }
        return objectMapper;
    }


}
