package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.graphhopper;

import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.EdgeIteratorState;
import org.springframework.stereotype.Component;

@Component
public class BooleanEdgeSetter extends EdgeSetter<Boolean, BooleanEncodedValue> {

    public BooleanEdgeSetter(EncodingManager encodingManager) {
        super(encodingManager);
    }

    @Override
    public Class<Boolean> getGetDataTypeClass() {
        return Boolean.class;
    }

    @Override
    protected Boolean getDefaultValue(BooleanEncodedValue ignored) {
        return false;
    }

    @Override
    protected Class<BooleanEncodedValue> getEncoderType() {
        return BooleanEncodedValue.class;
    }

    @Override
    protected void set(EdgeIteratorState edge, BooleanEncodedValue encodedValue, Boolean value) {
        edge.set(encodedValue, value);
    }

    @Override
    protected void setReverse(EdgeIteratorState edge, BooleanEncodedValue encodedValue, Boolean value) {
        edge.setReverse(encodedValue, value);
    }

}
