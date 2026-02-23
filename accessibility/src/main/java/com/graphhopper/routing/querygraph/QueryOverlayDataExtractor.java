package com.graphhopper.routing.querygraph;

import com.carrotsearch.hppc.IntArrayList;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class QueryOverlayDataExtractor {

    public static IntArrayList getClosestEdges(QueryGraph queryGraph) {
        return queryGraph.getQueryOverlay().getClosestEdges();
    }
}
