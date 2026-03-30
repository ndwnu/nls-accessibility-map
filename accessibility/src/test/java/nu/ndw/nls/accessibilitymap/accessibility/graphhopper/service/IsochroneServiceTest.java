package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.shapes.BBox;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.function.Consumer;
import lombok.SneakyThrows;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.IsochroneArguments;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityNetwork;
import nu.ndw.nls.routingmapmatcher.isochrone.algorithm.IsoLabel;
import nu.ndw.nls.routingmapmatcher.isochrone.algorithm.IsochroneByTimeDistanceAndWeight;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

@ExtendWith(MockitoExtension.class)
class IsochroneServiceTest {

    private IsochroneService isochroneService;

    @Mock
    private AccessibilityNetwork accessibilityNetwork;

    @Mock
    private NetworkData networkData;

    @Mock
    private EncodingManager encodingManager;

    @Mock
    private QueryGraph queryGraph;

    @Mock
    private IsochroneByTimeDistanceAndWeight isochroneByTimeDistanceAndWeight;

    @Mock
    private Weighting weighting;

    @Mock
    private BBox boundingBox;

    @Mock
    private Snap from;

    private MockedStatic<IsochroneShortestPathTreeFactory> isochroneShortestPathTreeFactoryMockedStatic;

    private MockedStatic<IsochroneFilter> isochroneFilterMockedStatic;

    @BeforeEach
    void setUp() {

        isochroneShortestPathTreeFactoryMockedStatic = Mockito.mockStatic(IsochroneShortestPathTreeFactory.class);
        isochroneFilterMockedStatic = Mockito.mockStatic(IsochroneFilter.class);

        isochroneService = new IsochroneService();
    }

    @AfterEach
    void tearDown() {

        isochroneShortestPathTreeFactoryMockedStatic.close();
        isochroneFilterMockedStatic.close();
    }

    @ParameterizedTest
    @CsvSource({
            "true, true, true, true",
            "false, true, true, false",
            "true, false, true, false",
            "true, true, false, false"
    })
    void getIsochroneMatches_municipalityId(
            boolean isNotRoot,
            boolean isWithinMunicipality,
            boolean isWithinBoundingBox,
            boolean hasMatch
    ) {

        IsoLabel isoLabel = createIsoLabel();

        when(accessibilityNetwork.getNetworkData()).thenReturn(networkData);
        when(accessibilityNetwork.getNetworkData().getEncodingManager()).thenReturn(encodingManager);
        when(accessibilityNetwork.getQueryGraph()).thenReturn(queryGraph);

        when(accessibilityNetwork.getFrom()).thenReturn(from);
        when(from.getClosestNode()).thenReturn(10);

        IsochroneArguments isochroneArguments = IsochroneArguments.builder()
                .weighting(weighting)
                .municipalityId(1)
                .boundingBox(boundingBox)
                .searchDistanceInMetres(2)
                .build();

        isochroneShortestPathTreeFactoryMockedStatic.when(() -> IsochroneShortestPathTreeFactory.createIsochroneByTimeDistanceAndWeight(
                        queryGraph,
                        isochroneArguments))
                .thenReturn(isochroneByTimeDistanceAndWeight);
        doAnswer(addIsoLabelToList(isoLabel)).when(isochroneByTimeDistanceAndWeight).search(eq(10), any());

        mockFiltersAndIsoLabelMapper(isoLabel, isochroneArguments, isNotRoot, isWithinMunicipality, isWithinBoundingBox);

        List<IsoLabel> isoLabels = isochroneService.search(accessibilityNetwork, isochroneArguments);

        if (hasMatch) {
            assertThat(isoLabels).containsExactly(isoLabel);
        } else {
            assertThat(isoLabels).isEmpty();
        }
    }

    private Answer addIsoLabelToList(IsoLabel isoLabel) {
        return invocation -> {
            Consumer<IsoLabel> callback = invocation.getArgument(1, Consumer.class);
            callback.accept(isoLabel);
            return null;
        };
    }

    private void mockFiltersAndIsoLabelMapper(
            IsoLabel isoLabel,
            IsochroneArguments isochroneArguments,
            boolean isNotRoot,
            boolean isWithinMunicipality,
            boolean isWithinBoundingBox
    ) {

        isochroneFilterMockedStatic.when(() -> IsochroneFilter.isNotRoot(isoLabel)).thenReturn(isNotRoot);

        if (!isNotRoot) {
            return;
        }
        isochroneFilterMockedStatic.when(() -> IsochroneFilter.isWithinMunicipality(
                encodingManager,
                queryGraph,
                isoLabel,
                isochroneArguments)).thenReturn(isWithinMunicipality);

        if (!isWithinMunicipality) {
            return;
        }
        isochroneFilterMockedStatic.when(() -> IsochroneFilter.isWithinBoundingBox(queryGraph, isoLabel, isochroneArguments))
                .thenReturn(isWithinBoundingBox);
    }

    @SneakyThrows
    private static IsoLabel createIsoLabel() {

        int rootId = -1;
        int edgeId = 1;
        int adjNode = 2;
        double weight = 0;

        Constructor<IsoLabel> constructor = IsoLabel.class.getDeclaredConstructor(
                int.class,
                int.class,
                double.class,
                long.class,
                double.class,
                IsoLabel.class);
        constructor.setAccessible(true);

        IsoLabel parent = constructor.newInstance(rootId, rootId, weight, 0, 0, null);

        return constructor.newInstance(edgeId, adjNode, weight, (long) 0, (double) 100, parent);
    }
}
