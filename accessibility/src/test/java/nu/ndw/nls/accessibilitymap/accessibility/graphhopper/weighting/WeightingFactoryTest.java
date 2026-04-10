package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.weighting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.PMap;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbData;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbDataUpdates;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class WeightingFactoryTest {

    @Mock
    private NetworkData networkData;

    @Mock
    private QueryGraph queryGraph;

    @Mock
    private NwbDataUpdates nwbDataUpdates;

    @Mock
    private NwbData nwbData;

    @Mock
    private NetworkGraphHopper networkGraphHopper;

    @Mock
    private Weighting baseWeighting;

    @Mock
    private EncodingManager encodingManager;

    @Captor
    private ArgumentCaptor<RoadChangesWeightingDecorator> weightingCaptor;

    private WeightingFactory weightingFactory;

    @BeforeEach
    void setUp() {
        weightingFactory = new WeightingFactory();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void createWeighting_withRestrictions(boolean applyRestrictions) {
        when(networkData.getNetworkGraphHopper()).thenReturn(networkGraphHopper);
        when(networkGraphHopper.getEncodingManager()).thenReturn(encodingManager);
        when(networkGraphHopper.createWeighting(eq(NetworkConstants.CAR_PROFILE), any(PMap.class))).thenReturn(baseWeighting);
        when(queryGraph.wrapWeighting(weightingCaptor.capture())).thenReturn(baseWeighting);
        when(networkData.getNwbData()).thenReturn(nwbData);
        when(networkData.getNwbDataUpdates()).thenReturn(nwbDataUpdates);

        Set<Integer> blockedEdges = applyRestrictions ? Set.of(1) : Set.of();
        Weighting weighting = weightingFactory.createWeighting(networkData, queryGraph, blockedEdges);

        assertThat(weighting).isEqualTo(baseWeighting);

        assertThatWeightingIsCorrectlyConstructed(blockedEdges);
    }

    private void assertThatWeightingIsCorrectlyConstructed(Set<Integer> blockedEdges) {
        RoadChangesWeightingDecorator constructedWeighting = weightingCaptor.getValue();
        Weighting roadChangesSourceWeighting = (Weighting) ReflectionTestUtils.getField(constructedWeighting, "sourceWeighting");
        NwbDataUpdates nwbDataUpdatesInstance = (NwbDataUpdates) ReflectionTestUtils.getField(constructedWeighting, "nwbDataUpdates");
        EncodingManager roadChangesEncodingManager = (EncodingManager) ReflectionTestUtils.getField(constructedWeighting,
                "encodingManager");

        assertThat(roadChangesSourceWeighting)
                .isNotNull()
                .isInstanceOf(RoadDataWeightingDecorator.class);
        assertThat(nwbDataUpdatesInstance).isEqualTo(nwbDataUpdates);
        assertThat(roadChangesEncodingManager).isEqualTo(encodingManager);

        Weighting roadDataSourceWeighting = (Weighting) ReflectionTestUtils.getField(roadChangesSourceWeighting, "sourceWeighting");
        NwbData nwbDataInstance = (NwbData) ReflectionTestUtils.getField(roadChangesSourceWeighting, "nwbData");
        EncodingManager roadDataEncodingManager = (EncodingManager) ReflectionTestUtils.getField(roadChangesSourceWeighting,
                "encodingManager");

        assertThat(roadDataSourceWeighting)
                .isNotNull()
                .isInstanceOf(RestrictionWeightingDecorator.class);
        assertThat(nwbDataInstance).isEqualTo(nwbData);
        assertThat(roadDataEncodingManager).isEqualTo(encodingManager);

        Weighting restrictionWeightingSourceWeighting = (Weighting) ReflectionTestUtils.getField(roadDataSourceWeighting,
                "sourceWeighting");
        @SuppressWarnings("unchecked") Set<Integer> blockedEdgesSet = (Set<Integer>) ReflectionTestUtils.getField(roadDataSourceWeighting,
                "blockedEdges");
        assertThat(blockedEdgesSet).isEqualTo(blockedEdges);
        assertThat(restrictionWeightingSourceWeighting).isEqualTo(baseWeighting);
    }
}
