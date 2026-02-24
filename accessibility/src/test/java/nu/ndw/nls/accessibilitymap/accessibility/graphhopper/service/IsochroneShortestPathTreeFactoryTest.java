package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.carrotsearch.hppc.IntArrayList;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.querygraph.QueryOverlayDataExtractor;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.BaseGraph;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.IsochroneArguments;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IsochroneShortestPathTreeFactoryTest {

    @Mock
    private QueryGraph queryGraph;

    @Mock
    private
    IsochroneArguments isochroneArguments;

    @Mock
    private BaseGraph baseGraph;

    @Mock
    private Weighting weighting;

    private MockedStatic<QueryOverlayDataExtractor> queryOverlayDataExtractorMockedStatic;

    @Mock
    private IntArrayList closestEdges;

    @BeforeEach
    void setUp() {

        queryOverlayDataExtractorMockedStatic = Mockito.mockStatic(QueryOverlayDataExtractor.class);
    }

    @AfterEach
    void tearDown() {

        queryOverlayDataExtractorMockedStatic.close();
    }

    @Test
    void createIsochroneByTimeDistanceAndWeight() {

        when(queryGraph.getBaseGraph()).thenReturn(baseGraph);
        when(isochroneArguments.weighting()).thenReturn(weighting);

        queryOverlayDataExtractorMockedStatic.when(() -> QueryOverlayDataExtractor.getClosestEdges(queryGraph))
                .thenReturn(closestEdges);

        var isochroneByTimeDistanceAndWeight = IsochroneShortestPathTreeFactory.createIsochroneByTimeDistanceAndWeight(
                queryGraph,
                isochroneArguments);

        // I know this is a bad test, but it is the only thing I can do considering the poor design of IsochroneByTimeDistanceAndWeight class
        // and Routing Map Matcher library in general. This stuff isn't even tested in the library itself.
        assertThat(isochroneByTimeDistanceAndWeight).isNotNull();
    }
}
