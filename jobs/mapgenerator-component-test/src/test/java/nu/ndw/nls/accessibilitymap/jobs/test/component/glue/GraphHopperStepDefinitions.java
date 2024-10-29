package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import io.cucumber.java.en.Given;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper.GraphHopperDriver;

@Slf4j
@RequiredArgsConstructor
public class GraphHopperStepDefinitions {

    private final GraphHopperDriver graphHopperDriver;

    @Given("a simple Graph Hopper network")
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
        graphHopperDriver
                .createNode(1, 1, 1)
                .createNode(2, 5, 1)
                .createNode(3, 10, 1)
                .createNode(4, 10, 10)
                .createNode(5, 5, 10)
                .createNode(6, 1, 10)
                .createNode(7, 4, 5)
                .createNode(8, 5, 2)
                .createNode(9, 7, 2)
                .createNode(10, 7, 8)
                .createNode(11, 5, 8)

                //Outer circle
                .createRoad(1, 2).createRoad(2, 3).createRoad(3, 4)
                .createRoad(4, 5).createRoad(5, 6).createRoad(6, 1)

                //Inner circle
                .createRoad(7, 8).createRoad(8, 9).createRoad(9, 10)
                .createRoad(10, 11).createRoad(11, 7)

                //Circle connections
                .createRoad(8, 2).createRoad(5, 11)
                .buildNetwork();
    }
}
