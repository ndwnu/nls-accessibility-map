package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.graphhopper;

import com.graphhopper.routing.ev.EncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.EdgeIterator;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("unchecked")
@RequiredArgsConstructor
public abstract class EdgeSetter<DATA_TYPE,
        ENCODED_VALUE_TYPE extends EncodedValue> {

    private final EncodingManager encodingManager;

    public void setDefaultValue(EdgeIterator edge, String key, boolean reverse, int bits) {
        setValue(edge, key, reverse, calculateDefaultValue(bits));
    }

    public void setValue(EdgeIterator edge, String key, boolean reverse,
            Object value) {
        final ENCODED_VALUE_TYPE ev = getEncodedValueType(key);
        if (reverse && !ev.isStoreTwoDirections()) {
            setReverse(edge, ev, (DATA_TYPE) value);
        } else {
            set(edge, ev, (DATA_TYPE) value);
        }
    }

    public abstract Class<DATA_TYPE> getGetDataTypeClass();

    protected abstract DATA_TYPE calculateDefaultValue(int bits);

    protected abstract Class<? extends EncodedValue> getEncoderType();

    protected abstract void set(EdgeIterator edge, ENCODED_VALUE_TYPE encodedValue, DATA_TYPE value);

    protected abstract void setReverse(EdgeIterator edge, ENCODED_VALUE_TYPE encodedValue, DATA_TYPE value);

    private ENCODED_VALUE_TYPE getEncodedValueType(String key) {
        Class<? extends EncodedValue> encoderType = getEncoderType();
        return (ENCODED_VALUE_TYPE) encodingManager.getEncodedValue(key, encoderType);
    }

}
