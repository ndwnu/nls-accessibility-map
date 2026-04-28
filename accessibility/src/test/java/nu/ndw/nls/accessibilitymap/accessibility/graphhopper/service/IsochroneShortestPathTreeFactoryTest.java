package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.weighting.Weighting;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.IsochroneArguments;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IsochroneShortestPathTreeFactoryTest {

    @Mock
    private QueryGraph queryGraph;

    @Mock
    private
    IsochroneArguments isochroneArguments;

    @Mock
    private Weighting weighting;

    @Test
    void createIsochroneByTimeDistanceAndWeight() {

        when(isochroneArguments.weighting()).thenReturn(weighting);

        var isochroneByTimeDistanceAndWeight = IsochroneShortestPathTreeFactory.createIsochroneByTimeDistanceAndWeight(
                queryGraph,
                isochroneArguments);

        // I know this is a bad test, but it is the only thing I can do considering the poor design of IsochroneByTimeDistanceAndWeight class
        // and Routing Map Matcher library in general. This stuff isn't even tested in the library itself.
        assertThat(isochroneByTimeDistanceAndWeight).isNotNull();
    }
}
