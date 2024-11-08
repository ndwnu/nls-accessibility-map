package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.graphhopper;

import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.EdgeIterator;
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
    protected Boolean calculateDefaultValue(int ignored) {
        return false;
    }

    @Override
    protected Class<BooleanEncodedValue> getEncoderType() {
        return BooleanEncodedValue.class;
    }

    @Override
    protected void set(EdgeIterator edge, BooleanEncodedValue encodedValue, Boolean value) {
        edge.set(encodedValue, value);
    }

    @Override
    protected void setReverse(EdgeIterator edge, BooleanEncodedValue encodedValue, Boolean value) {
        edge.setReverse(encodedValue, value);
    }

}
