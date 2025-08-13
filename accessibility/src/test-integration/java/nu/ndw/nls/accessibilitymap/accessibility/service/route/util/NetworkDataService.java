package nu.ndw.nls.accessibilitymap.accessibility.service.route.util;

import static org.assertj.core.api.Fail.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.AccessibilityLink;
import nu.ndw.nls.accessibilitymap.accessibility.service.route.util.dto.Link;
import nu.ndw.nls.accessibilitymap.accessibility.service.route.util.dto.Node;
import nu.ndw.nls.geometry.crs.CrsTransformer;
import nu.ndw.nls.geometry.factories.GeometryFactoryRijksdriehoek;
import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
import nu.ndw.nls.routingmapmatcher.network.model.DirectionalDto;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NetworkDataService {

    private final GeometryFactoryWgs84 geometryFactoryWgs84 = new GeometryFactoryWgs84();

    private final GeometryFactoryRijksdriehoek geometryFactoryRijksdriehoek = new GeometryFactoryRijksdriehoek();

    private final CrsTransformer crsTransformer;

    private final LongSequenceSupplier longSequenceSupplier = new LongSequenceSupplier();

    @Getter
    private final Map<Long, Link> links = new HashMap<>();

    @Getter
    private final Map<Long, Node> nodes = new HashMap<>();

    public NetworkDataService createRoad(
            long startNodeId,
            long endNodeId) {

        var startNode = findNodeById(startNodeId);
        var endNode = findNodeById(endNodeId);

        var latLongLineString = geometryFactoryWgs84.createLineString(
                new Coordinate[]{
                        startNode.getLatLongAsCoordinate(),
                        endNode.getLatLongAsCoordinate()
                }
        );

        var accessibilityLinkBuilder = AccessibilityLink.builder()
                .id(longSequenceSupplier.next())
                .municipalityCode(1)
                .fromNodeId(startNode.getId())
                .toNodeId(endNode.getId())
                .geometry(latLongLineString)
                .distanceInMeters(createRijksdriehoekLineString(latLongLineString).getLength())
                .accessibility(DirectionalDto.<Boolean>builder()
                        .forward(true)
                        .reverse(true)
                        .build());

        var link = Link.builder()
                .accessibilityLink(accessibilityLinkBuilder.build())
                .rijksDiehoekLineString(createRijksdriehoekLineString(latLongLineString))
                .wgs84LineString(latLongLineString)
                .startNode(startNode)
                .endNode(endNode)
                .build();

        links.put(link.getAccessibilityLink().getId(), link);
        startNode.addLink(link);
        endNode.addLink(link);

        return this;
    }

    public LineString createRijksdriehoekLineString(LineString latLongLineString) {
        return geometryFactoryRijksdriehoek.createLineString(
                new Coordinate[]{
                        crsTransformer.transformFromWgs84ToRdNew(latLongLineString).getCoordinates()[0],
                        crsTransformer.transformFromWgs84ToRdNew(latLongLineString).getCoordinates()[1]
                }
        );
    }

    public NetworkDataService createNode(long id, double x, double y) {

        nodes.put(id, Node.builder()
                .id(id)
                .latitude(y)
                .longitude(x)
                .build());

        return this;
    }

    public Node findNodeById(long id) {

        if (!nodes.containsKey(id)) {
            fail("Node with id '%s' does not exist", id);
        }

        return nodes.get(id);
    }

    public Link findRoadSectionById(long id) {

        if (!links.containsKey(id)) {
            fail("Link with id '%s' does not exist", id);
        }

        return links.get(id);
    }


    public Link findLinkBetweenNodes(long firstNodeId, long secondNodeId) {

        Node firstNode = findNodeById(firstNodeId);
        Node secondNode = findNodeById(secondNodeId);

        List<Link> commonLinks = firstNode.getCommonLinks(secondNode);
        if (commonLinks.size() != 1) {
            fail("There should be only one link between two nodes. We found %s. So that means your network data is not matching your expectations."
                    .formatted(commonLinks.size()));
        }

        return commonLinks.getFirst();
    }

    public void clear() {

        nodes.clear();
        links.clear();
    }
}
