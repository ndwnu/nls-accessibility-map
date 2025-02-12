package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.querygraph;

import com.graphhopper.routing.ev.EncodedValue;
import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.EdgeIteratorState;
import org.springframework.stereotype.Component;

@Component
public class IntegerEdgeSetter extends EdgeSetter<Integer, IntEncodedValue> {

    public IntegerEdgeSetter(EncodingManager encodingManager) {
        super(encodingManager);
    }

    @Override
    public Class<Integer> getGetDataTypeClass() {
        return Integer.class;
    }

    @Override
    protected Integer getDefaultValue(IntEncodedValue encodedValue) {
        return encodedValue.getMaxStorableInt();
    }

    @Override
    protected Class<? extends EncodedValue> getEncoderType() {
        return IntEncodedValue.class;
    }

    @Override
    protected void set(EdgeIteratorState edge, IntEncodedValue encodedValue, Integer value) {
        edge.set(encodedValue, value);
    }

    @Override
    protected void setReverse(EdgeIteratorState edge, IntEncodedValue encodedValue, Integer value) {
        edge.setReverse(encodedValue, value);
    }
}
