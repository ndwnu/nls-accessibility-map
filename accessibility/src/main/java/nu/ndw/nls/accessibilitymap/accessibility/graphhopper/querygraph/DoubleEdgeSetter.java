package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.querygraph;

import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.EncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.EdgeIteratorState;
import org.springframework.stereotype.Component;

@Component
public class DoubleEdgeSetter extends EdgeSetter<Double, DecimalEncodedValue> {

    public DoubleEdgeSetter(EncodingManager encodingManager) {
        super(encodingManager);
    }

    @Override
    public Class<Double> getGetDataTypeClass() {
        return Double.class;
    }

    @Override
    protected Class<? extends EncodedValue> getEncoderType() {
        return DecimalEncodedValue.class;
    }

    @Override
    protected Double getDefaultValue(DecimalEncodedValue encodedValue) {
        return encodedValue.getMaxStorableDecimal();
    }

    @Override
    protected void set(EdgeIteratorState edge, DecimalEncodedValue encodedValue, Double value) {
        edge.set(encodedValue, value);
    }

    @Override
    protected void setReverse(EdgeIteratorState edge, DecimalEncodedValue encodedValue, Double value) {
        edge.setReverse(encodedValue, value);
    }
}
