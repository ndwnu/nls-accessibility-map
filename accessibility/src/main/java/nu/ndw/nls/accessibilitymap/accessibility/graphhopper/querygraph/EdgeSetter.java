package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.querygraph;

import com.graphhopper.routing.ev.EncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.EdgeIteratorState;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("unchecked")
@RequiredArgsConstructor
public abstract class EdgeSetter<DATA_TYPE,
        ENCODED_VALUE_TYPE extends EncodedValue> {

    private final EncodingManager encodingManager;

    public void setDefaultValue(EdgeIteratorState edge, String key, boolean reverse) {
        final ENCODED_VALUE_TYPE ev = getEncodedValueType(key);
        setValue(edge, key, reverse, getDefaultValue(ev));
    }

    public void setValue(EdgeIteratorState edge, String key, boolean reverse,
            Object value) {
        final ENCODED_VALUE_TYPE ev = getEncodedValueType(key);
        if (reverse && !ev.isStoreTwoDirections()) {
            setReverse(edge, ev, (DATA_TYPE) value);
        } else {
            set(edge, ev, (DATA_TYPE) value);
        }
    }

    public abstract Class<DATA_TYPE> getGetDataTypeClass();

    protected abstract DATA_TYPE getDefaultValue(ENCODED_VALUE_TYPE encodedValue);

    protected abstract Class<? extends EncodedValue> getEncoderType();

    protected abstract void set(EdgeIteratorState edge, ENCODED_VALUE_TYPE encodedValue, DATA_TYPE value);

    protected abstract void setReverse(EdgeIteratorState edge, ENCODED_VALUE_TYPE encodedValue, DATA_TYPE value);

    private ENCODED_VALUE_TYPE getEncodedValueType(String key) {
        Class<? extends EncodedValue> encoderType = getEncoderType();
        return (ENCODED_VALUE_TYPE) encodingManager.getEncodedValue(key, encoderType);
    }
}
