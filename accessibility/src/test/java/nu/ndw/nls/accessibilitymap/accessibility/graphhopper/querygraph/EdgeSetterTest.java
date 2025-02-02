package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.querygraph;

import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.EdgeIteratorState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class EdgeSetterTest {

    private static final String KEY = "key";

    @Mock
    private EdgeIteratorState edgeIteratorState;

    @Mock
    private EncodingManager encodingManager;

    @Mock
    private BooleanEncodedValue booleanEncodedValue;

    @Mock
    private EdgeSetter edgeSetter;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(edgeSetter,
                "encodingManager", encodingManager);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            true, true
            true, false
            false, true
            false, false
            """)
    void setDefaultValue_ok(boolean reversed, boolean storeInTwoDirections) {
        doCallRealMethod()
                .when(edgeSetter)
                .setDefaultValue(edgeIteratorState, KEY, reversed);
        doCallRealMethod()
                .when(edgeSetter)
                .setValue(edgeIteratorState, KEY, reversed, true);
        when(edgeSetter.getEncoderType())
                .thenReturn(BooleanEncodedValue.class);
        if (reversed) {
            when(booleanEncodedValue.isStoreTwoDirections()).thenReturn(storeInTwoDirections);
        }
        when(encodingManager.getEncodedValue(KEY, BooleanEncodedValue.class)).thenReturn(booleanEncodedValue);
        when(edgeSetter.getDefaultValue(booleanEncodedValue)).thenReturn(true);

        edgeSetter.setDefaultValue(edgeIteratorState, KEY, reversed);

        if (reversed && !booleanEncodedValue.isStoreTwoDirections()) {
            verify(edgeSetter).setReverse(edgeIteratorState, booleanEncodedValue, true);
        } else {
            verify(edgeSetter).set(edgeIteratorState, booleanEncodedValue, true);
        }

    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            true, true
            true, false
            false, true
            false, false
            """)
    void setValue_ok(boolean reversed, boolean storeInTwoDirections) {
        doCallRealMethod().when(edgeSetter).setValue(edgeIteratorState, KEY, reversed, true);

        if (reversed) {
            when(booleanEncodedValue.isStoreTwoDirections()).thenReturn(storeInTwoDirections);
        }

        when(edgeSetter.getEncoderType()).thenReturn(BooleanEncodedValue.class);
        when(encodingManager.getEncodedValue(KEY, BooleanEncodedValue.class)).thenReturn(booleanEncodedValue);

        edgeSetter.setValue(edgeIteratorState, KEY, reversed, true);

        if (reversed && !booleanEncodedValue.isStoreTwoDirections()) {
            verify(edgeSetter).setReverse(edgeIteratorState, booleanEncodedValue, true);
        } else {
            verify(edgeSetter).set(edgeIteratorState, booleanEncodedValue, true);
        }
    }
}
