package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.graphhopper;

import com.graphhopper.routing.ev.EncodedValue;
import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.EdgeIterator;
import org.springframework.stereotype.Component;

@Component
public class IntegerEdgeSetter extends EdgeSetter<Integer, IntEncodedValue> {

    private static final int BASE_TWO = 2;

    public IntegerEdgeSetter(EncodingManager encodingManager) {
        super(encodingManager);
    }

    @Override
    public Class<Integer> getGetDataTypeClass() {
        return Integer.class;
    }

    @Override
    protected Integer calculateDefaultValue(int bits) {
        return (int) Math.pow(BASE_TWO, bits);
    }

    @Override
    protected Class<? extends EncodedValue> getEncoderType() {
        return IntEncodedValue.class;
    }

    @Override
    protected void set(EdgeIterator edge, IntEncodedValue encodedValue, Integer value) {
        edge.set(encodedValue, value);
    }

    @Override
    protected void setReverse(EdgeIterator edge, IntEncodedValue encodedValue, Integer value) {
        edge.setReverse(encodedValue, value);
    }
}
