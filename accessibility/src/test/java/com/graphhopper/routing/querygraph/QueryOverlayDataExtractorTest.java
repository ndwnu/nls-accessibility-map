package com.graphhopper.routing.querygraph;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.carrotsearch.hppc.IntArrayList;
import org.junit.jupiter.api.Test;

class QueryOverlayDataExtractorTest {

    @Test
    void getClosestEdges() {

        QueryOverlay queryOverlay = mock(QueryOverlay.class);
        QueryGraph queryGraph = mock(QueryGraph.class);
        when(queryGraph.getQueryOverlay()).thenReturn(queryOverlay);
        IntArrayList closestEdges = mock(IntArrayList.class);

        when(queryOverlay.getClosestEdges()).thenReturn(closestEdges);

        assertThat(QueryOverlayDataExtractor.getClosestEdges(queryGraph)).isEqualTo(closestEdges);
    }
}
