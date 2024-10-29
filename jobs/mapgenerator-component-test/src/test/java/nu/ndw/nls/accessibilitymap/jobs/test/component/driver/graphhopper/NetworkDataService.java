package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper;

import static org.assertj.core.api.Fail.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.test.component.core.StateManagement;
import nu.ndw.nls.accessibilitymap.jobs.test.component.core.util.LongSequenceSupplier;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper.dto.Link;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper.dto.Node;
import nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink;
import nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.AccessibilityLinkBuilder;
import nu.ndw.nls.geometry.crs.CrsTransformer;
import nu.ndw.nls.geometry.factories.GeometryFactoryRijksdriehoek;
import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NetworkDataService implements StateManagement {

    private final LongSequenceSupplier longSequenceSupplier = new LongSequenceSupplier();

    private final GeometryFactoryWgs84 geometryFactoryWgs84 = new GeometryFactoryWgs84();

    private final GeometryFactoryRijksdriehoek geometryFactoryRijksdriehoek = new GeometryFactoryRijksdriehoek();

    private final CrsTransformer crsTransformer;

    @Getter
    private List<Link> links = new ArrayList<>();

    @Getter
    private Map<Long, Node> nodes = new HashMap<>();

    public NetworkDataService createRoad(long startNodeId, long endNodeId) {

        return createRoad(startNodeId, endNodeId, new AllAccessibleLinkBuilderConsumer());
    }

    public NetworkDataService createRoad(
            long startNodeId,
            long endNodeId,
            Consumer<AccessibilityLinkBuilder> linkConfigurerconsumer) {

        Node startNode = findNodeById(startNodeId);
        Node endNode = findNodeById(endNodeId);

        LineString latLongLineString = geometryFactoryWgs84.createLineString(
                new Coordinate[]{
                        startNode.getLatLongAsCoordinate(),
                        endNode.getLatLongAsCoordinate()
                }
        );

        AccessibilityLinkBuilder accessibilityLinkBuilder = AccessibilityLink.builder()
                .id(longSequenceSupplier.next())
                .fromNodeId(startNode.getId())
                .toNodeId(endNode.getId())
                .geometry(latLongLineString);

        linkConfigurerconsumer.accept(accessibilityLinkBuilder);

        Link link = Link.builder()
                .accessibilityLink(accessibilityLinkBuilder.build())
                .rijksDiehoekLineString(geometryFactoryRijksdriehoek.createLineString(
                        new Coordinate[]{
                                crsTransformer.transformFromWgs84ToRdNew(latLongLineString).getCoordinates()[0],
                                crsTransformer.transformFromWgs84ToRdNew(latLongLineString).getCoordinates()[1]
                        }
                ))
                .wgs84LineString(latLongLineString)
                .build();

        links.add(link);
        startNode.addLink(link);
        endNode.addLink(link);

        return this;
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

    @Override
    public void clearStateAfterEachScenario() {

        nodes.clear();
        links.clear();
    }
}
