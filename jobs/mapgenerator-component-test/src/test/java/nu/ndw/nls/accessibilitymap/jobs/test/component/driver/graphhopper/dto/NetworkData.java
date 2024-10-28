package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper.dto;

import static org.assertj.core.api.Fail.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.test.component.core.StateManagement;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper.utils.LongSequenceSupplier;
import nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink;
import nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.AccessibilityLinkBuilder;
import nu.ndw.nls.geometry.crs.CrsTransformer;
import nu.ndw.nls.geometry.factories.GeometryFactoryRijksdriehoek;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NetworkData implements StateManagement {

    private final LongSequenceSupplier longSequenceSupplier = new LongSequenceSupplier();

    private final GeometryFactoryRijksdriehoek geometryFactoryRijksdriehoek = new GeometryFactoryRijksdriehoek();

    private final CrsTransformer crsTransformer;

    @Getter
    private List<Link> links = new ArrayList<>();

    private Map<Long, Node> nodes = new HashMap<>();

    public NetworkData createRoad(long startNodeId, long endNodeId) {

        return createRoad(startNodeId, endNodeId, new AllAccessibleLinkBuilderConsumer());
    }

    public NetworkData createRoad(
            long startNodeId,
            long endNodeId,
            Consumer<AccessibilityLinkBuilder> linkConfigurerconsumer) {

        Node startNode = findNodeById(startNodeId);
        Node endNode = findNodeById(endNodeId);

        LineString rijksDriehoekLineString = geometryFactoryRijksdriehoek.createLineString(
                new Coordinate[]{
                        startNode.getCoordinate(),
                        endNode.getCoordinate()
                }
        );
        LineString wgs84LineString = (LineString) crsTransformer.transformFromRdNewToWgs84(rijksDriehoekLineString);

        AccessibilityLinkBuilder linkBuilder = AccessibilityLink.builder()
                .id(longSequenceSupplier.next())
                .fromNodeId(startNode.getId())
                .toNodeId(endNode.getId())
                .geometry(wgs84LineString);

        linkConfigurerconsumer.accept(linkBuilder);

        AccessibilityLink link = linkBuilder.build();
        links.add(Link.builder()
                .accessibilityLink(link)
                .rijksDiehoekLineString(rijksDriehoekLineString)
                .wgs84LineString(wgs84LineString)
                .build());
        startNode.addLink(link);
        endNode.addLink(link);

        return this;
    }

    public NetworkData createNode(long id, double x, double y) {

        nodes.put(id, Node.builder()
                .id(id)
                .coordinate(new Coordinate(x, y))
                .build());

        return this;
    }

    public Node findNodeById(long id) {

        if (!nodes.containsKey(id)) {
            fail("Node with id '%s' does not exist", id);
        }

        return nodes.get(id);
    }

    public AccessibilityLink findLinkBetweenNodes(long firstNodeId, long secondNodeId) {

        Node firstNode = findNodeById(firstNodeId);
        Node secondNode = findNodeById(secondNodeId);

        List<AccessibilityLink> commonLinks = firstNode.getCommonLinks(secondNode);
        if (commonLinks.size() != 1) {
            fail("There should be only one link between two nodes. We found %s. So that means your network data is not matching your expectations."
                    .formatted(commonLinks.size()));
        }

        return commonLinks.getFirst();
    }

    @Override
    public void clearStateAfterEachScenario() {

        nodes.clear();
        links.clear();
    }
}
