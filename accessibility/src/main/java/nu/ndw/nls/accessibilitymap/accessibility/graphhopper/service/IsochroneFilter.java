package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service;

import static nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.AccessibilityLink.MUNICIPALITY_CODE;

import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.FetchMode;
import com.graphhopper.util.PointList;
import java.util.Objects;
import lombok.NoArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.IsochroneArguments;
import nu.ndw.nls.routingmapmatcher.isochrone.algorithm.IsoLabel;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class IsochroneFilter {

    private static final int ROOT_ID = -1;

    public static boolean isNotRoot(IsoLabel isoLabel) {

        return isoLabel.getEdge() != ROOT_ID;
    }

    public static boolean isWithinBoundingBox(QueryGraph queryGraph, IsoLabel isoLabel, IsochroneArguments isochroneArguments) {

        if (Objects.isNull(isochroneArguments.boundingBox())) {
            return true;
        }

        EdgeIteratorState currentEdge = queryGraph.getEdgeIteratorState(isoLabel.getEdge(), isoLabel.getNode());
        PointList points = currentEdge.fetchWayGeometry(FetchMode.TOWER_ONLY);

        return isochroneArguments.boundingBox().intersects(points);
    }

    public static boolean isWithinMunicipality(
            EncodingManager encodingManager,
            QueryGraph queryGraph,
            IsoLabel isoLabel,
            IsochroneArguments isochroneArguments) {

        if (Objects.isNull(isochroneArguments.municipalityId())) {
            return true;
        }

        IntEncodedValue municipalityEncodeValueId = encodingManager.getIntEncodedValue(MUNICIPALITY_CODE);
        EdgeIteratorState currentEdge = queryGraph.getEdgeIteratorState(isoLabel.getEdge(), isoLabel.getNode());

        return currentEdge.get(municipalityEncodeValueId) == isochroneArguments.municipalityId();
    }

}
