package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.weighting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.EdgeIteratorState;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RestrictionWeightingDecoratorTest {

    private RestrictionWeightingDecorator restrictionWeightingDecorator;

    @Mock
    private Weighting sourceWeighting;

    @Mock
    private Set<Integer> blockedEdges;

    @Mock
    private EdgeIteratorState edgeIteratorState;

    @BeforeEach
    void setUp() {
        restrictionWeightingDecorator = new RestrictionWeightingDecorator(sourceWeighting, blockedEdges);
    }

    @Test
    void calcMinWeightPerDistance() {

        restrictionWeightingDecorator.calcMinWeightPerDistance();

        verify(sourceWeighting).calcMinWeightPerDistance();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void calcEdgeWeight_isBlockedEdge(boolean reversed) {

        when(edgeIteratorState.getEdgeKey()).thenReturn(123);
        when(blockedEdges.contains(123)).thenReturn(true);

        assertThat(restrictionWeightingDecorator.calcEdgeWeight(edgeIteratorState, reversed)).isEqualTo(Double.POSITIVE_INFINITY);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void calcEdgeWeight_isNotBlockedEdge(boolean reversed) {

        when(sourceWeighting.calcEdgeWeight(edgeIteratorState, reversed)).thenReturn(234D);

        assertThat(restrictionWeightingDecorator.calcEdgeWeight(edgeIteratorState, reversed)).isEqualTo(234D);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void calcEdgeMillis(boolean reversed) {

        when(sourceWeighting.calcEdgeMillis(edgeIteratorState, reversed)).thenReturn(345L);

        assertThat(restrictionWeightingDecorator.calcEdgeMillis(edgeIteratorState, reversed)).isEqualTo(345L);
    }

    @Test
    void calcTurnWeight() {

        when(sourceWeighting.calcTurnWeight(1, 2, 3)).thenReturn(4D);

        assertThat(restrictionWeightingDecorator.calcTurnWeight(1, 2, 3)).isEqualTo(4D);
    }

    @Test
    void calcTurnMillis() {

        when(sourceWeighting.calcTurnMillis(1, 2, 3)).thenReturn(4L);

        assertThat(restrictionWeightingDecorator.calcTurnMillis(1, 2, 3)).isEqualTo(4L);
    }

    @Test
    void hasTurnCosts() {

        restrictionWeightingDecorator.hasTurnCosts();

        verify(sourceWeighting).hasTurnCosts();
    }

    @Test
    void getName() {

        restrictionWeightingDecorator.getName();

        verify(sourceWeighting).getName();
    }
}
