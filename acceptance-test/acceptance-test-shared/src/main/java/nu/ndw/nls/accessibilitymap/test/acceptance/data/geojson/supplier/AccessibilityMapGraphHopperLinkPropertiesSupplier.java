package nu.ndw.nls.accessibilitymap.test.acceptance.data.geojson.supplier;

import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.AccessibilityLink;
import nu.ndw.nls.accessibilitymap.test.acceptance.data.geojson.dto.LineStringProperties;
import nu.ndw.nls.springboot.test.graph.dto.Edge;
import nu.ndw.nls.springboot.test.graph.exporter.geojson.dto.LineStringGraphHopperGraphProperties;
import nu.ndw.nls.springboot.test.graph.exporter.graphhopper.service.geojson.dto.GraphHopperLinkInfo;
import nu.ndw.nls.springboot.test.graph.exporter.graphhopper.service.geojson.supplier.GraphHopperEdgePropertiesSupplier;

public class AccessibilityMapGraphHopperLinkPropertiesSupplier extends GraphHopperEdgePropertiesSupplier {

    @Override
    public LineStringGraphHopperGraphProperties create(Edge edge, GraphHopperLinkInfo graphHopperLinkInfo) {

        var lineStringGraphHopperNetworkProperties = super.create(edge, graphHopperLinkInfo);

        return LineStringProperties.builder()
                .roadSectionId(lineStringGraphHopperNetworkProperties.getRoadSectionId())
                .distanceInMeters(lineStringGraphHopperNetworkProperties.getDistanceInMeters())
                .fromNodeId(lineStringGraphHopperNetworkProperties.getFromNodeId())
                .toNodeId(lineStringGraphHopperNetworkProperties.getToNodeId())
                .directions(lineStringGraphHopperNetworkProperties.getDirections())
                .graphHopperFromNodeId(lineStringGraphHopperNetworkProperties.getGraphHopperFromNodeId())
                .graphHopperToNodeId(lineStringGraphHopperNetworkProperties.getGraphHopperToNodeId())
                .edge(lineStringGraphHopperNetworkProperties.getEdge())
                .edgeKey(lineStringGraphHopperNetworkProperties.getEdgeKey())
                .reverseEdgeKey(lineStringGraphHopperNetworkProperties.getReverseEdgeKey())
                .edgeAttributes(lineStringGraphHopperNetworkProperties.getEdgeAttributes())
                .municipalityCode(
                        (Integer) lineStringGraphHopperNetworkProperties.getEdgeAttributes().get(AccessibilityLink.MUNICIPALITY_CODE))
                .build();
    }
}
