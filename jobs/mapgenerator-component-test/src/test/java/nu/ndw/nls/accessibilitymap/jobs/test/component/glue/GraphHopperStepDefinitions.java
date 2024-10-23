package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import io.cucumber.java.en.Given;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper.GraphHopperConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper.NetworkGraphHopperBuilder;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper.dto.Node;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper.dto.NodeBuilder;
import nu.ndw.nls.routingmapmatcher.network.GraphHopperNetworkService;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;

@Slf4j
@RequiredArgsConstructor
public class GraphHopperStepDefinitions {

    private final NodeBuilder nodeBuilder;
    private final GraphHopperConfiguration graphHopperConfiguration;
    private final GraphHopperNetworkService graphHopperNetworkService;

    @Given("Graph Hopper network")
    public void graphHopperNetwork() {

        /*
         6----5-----4
         |    |     |
         |    11-10 |
         |   /   |  |
         |  7    |  |
         |   \   |  |
         |    8--9  |
         |    |     |
         1----2-----3
         */
        Node node1 = nodeBuilder.build(1, 1);
        Node node2 = nodeBuilder.build(5, 1);
        Node node3 = nodeBuilder.build(10, 1);
        Node node4 = nodeBuilder.build(10, 10);
        Node node5 = nodeBuilder.build(5, 10);
        Node node6 = nodeBuilder.build(1, 10);
        Node node7 = nodeBuilder.build(4, 5);
        Node node8 = nodeBuilder.build(5, 2);
        Node node9 = nodeBuilder.build(7, 2);
        Node node10 = nodeBuilder.build(7, 8);
        Node node11 = nodeBuilder.build(5, 8);

        NetworkGraphHopperBuilder networkGraphHopperBuilder = NetworkGraphHopperBuilder.builder(
                graphHopperConfiguration,
                graphHopperNetworkService);

        networkGraphHopperBuilder.createRoad(node1, node2);
        networkGraphHopperBuilder.createRoad(node2, node3);
        networkGraphHopperBuilder.createRoad(node3, node4);
        networkGraphHopperBuilder.createRoad(node4, node5);
        networkGraphHopperBuilder.createRoad(node5, node6);
        networkGraphHopperBuilder.createRoad(node6, node1);

        networkGraphHopperBuilder.createRoad(node7, node8);
        networkGraphHopperBuilder.createRoad(node8, node9);
        networkGraphHopperBuilder.createRoad(node9, node10);
        networkGraphHopperBuilder.createRoad(node10, node11);
        networkGraphHopperBuilder.createRoad(node11, node7);

        networkGraphHopperBuilder.createRoad(node8, node2);
        networkGraphHopperBuilder.createRoad(node5, node11);

        NetworkGraphHopper networkGraphHopper = networkGraphHopperBuilder.build();

        log.info(networkGraphHopper.toString());
    }
}
