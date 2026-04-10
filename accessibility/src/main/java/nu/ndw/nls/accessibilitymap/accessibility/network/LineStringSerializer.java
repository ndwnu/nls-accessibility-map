package nu.ndw.nls.accessibilitymap.accessibility.network;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;

public class LineStringSerializer extends Serializer<LineString> {

    private static final WKTWriter writer = new WKTWriter();

    private static final WKTReader reader = new WKTReader();

    @Override
    public void write(Kryo kryo, Output output, LineString object) {
        output.writeString(writer.write(object));
    }

    @Override
    public LineString read(Kryo kryo, Input input, Class<? extends LineString> type) {
        try {
            return (LineString) reader.read(input.readString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
