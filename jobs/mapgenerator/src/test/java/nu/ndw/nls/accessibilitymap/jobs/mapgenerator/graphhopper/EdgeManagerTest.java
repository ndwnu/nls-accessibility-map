package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.graphhopper;

import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.TRAFFIC_SIGN_ID;
import static nu.ndw.nls.routingmapmatcher.network.model.Link.REVERSED_LINK_ID;
import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import com.graphhopper.util.EdgeIterator;
import java.util.Optional;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink;
import nu.ndw.nls.routingmapmatcher.network.annotations.mappers.EncodedValuesMapper;
import nu.ndw.nls.routingmapmatcher.network.annotations.model.EncodedValueDto;
import nu.ndw.nls.routingmapmatcher.network.annotations.model.EncodedValuesByTypeDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EdgeManagerTest {

    private static final String KEY = "key";
    private static final int BITS = 0;
    @Mock
    private EncodedValuesMapper encodedValuesMapper;
    @Mock
    private EdgeSetterRegistry edgeSetterRegistry;
    @Mock
    private EdgeSetter<?, ?> edgeSetter;
    @Mock
    private EdgeIterator edgeIterator;
    @Mock
    private EncodedValuesByTypeDto<AccessibilityLink> encodedValuesByTypeDto;
    @Mock
    private EdgeIteratorStateReverseExtractor reverseExtractor;

    @Mock
    private EncodedValueDto<AccessibilityLink, Boolean> encodedValueDto;

    private EdgeManager edgeManager;

    @BeforeEach
    void setUp() {
        when(encodedValuesMapper.map(AccessibilityLink.class)).thenReturn(encodedValuesByTypeDto);

        edgeManager = new EdgeManager(encodedValuesMapper, edgeSetterRegistry, reverseExtractor);
    }

    @Test
    void setValueOnEdge_ok() {
        when(reverseExtractor.hasReversed(edgeIterator)).thenReturn(false);
        when(encodedValuesByTypeDto.getValueTypeByKey(KEY)).thenReturn(Optional.of(Boolean.class));
        when(edgeSetterRegistry.getEdgeSetter(Boolean.class)).thenReturn(Optional.of(edgeSetter));

        edgeManager.setValueOnEdge(edgeIterator, KEY, true);

        verify(edgeSetter, times(1)).setValue(edgeIterator, KEY, false, true);
    }

    @Test
    void setValueOnEdge_exception_invalidKey() {
        when(reverseExtractor.hasReversed(edgeIterator)).thenReturn(false);
        when(encodedValuesByTypeDto.getValueTypeByKey(KEY)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                edgeManager.setValueOnEdge(edgeIterator, KEY, true))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Invalid key: key");
    }

    @Test
    void setValueOnEdge_exception_no_edgeSetter() {
        when(reverseExtractor.hasReversed(edgeIterator)).thenReturn(false);
        when(encodedValuesByTypeDto.getValueTypeByKey(KEY)).thenReturn(Optional.of(Boolean.class));
        when(edgeSetterRegistry.getEdgeSetter(Boolean.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                edgeManager.setValueOnEdge(edgeIterator, KEY, true))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("No EdgeSetter found for class "
                        + "java.lang.Boolean");
    }

    @Test
    void resetRestrictionsOnEdge_ok() {
        when(reverseExtractor.hasReversed(edgeIterator)).thenReturn(false);
        when(encodedValuesByTypeDto.keySet()).thenReturn(Set.of(KEY));
        when(encodedValuesByTypeDto.getValueTypeByKey(KEY)).thenReturn(Optional.of(Boolean.class));
        when(edgeSetterRegistry.getEdgeSetter(Boolean.class)).thenReturn(Optional.of(edgeSetter));
        when(encodedValuesByTypeDto.get(Boolean.class, KEY)).thenReturn(encodedValueDto);
        when(encodedValueDto.valueType()).thenReturn(Boolean.class);
        when(encodedValueDto.key()).thenReturn(KEY);

        edgeManager.resetRestrictionsOnEdge(edgeIterator);

        verify(edgeSetter, times(1)).setDefaultValue(edgeIterator, KEY, false);
    }


    @Test
    void resetRestrictionsOnEdge_ok_noResults() {
        when(reverseExtractor.hasReversed(edgeIterator)).thenReturn(false);
        when(encodedValuesByTypeDto.keySet()).thenReturn(Set.of(TRAFFIC_SIGN_ID, WAY_ID_KEY, REVERSED_LINK_ID));

        edgeManager.resetRestrictionsOnEdge(edgeIterator);

        verify(edgeSetter, times(0))
                .setDefaultValue(edgeIterator, TRAFFIC_SIGN_ID, false);
    }
}
