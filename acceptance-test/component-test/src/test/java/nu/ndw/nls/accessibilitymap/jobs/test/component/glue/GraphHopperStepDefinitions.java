package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import static org.assertj.core.api.Assertions.assertThat;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto.NwbRoadSection;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.database.entity.NwbRoadSectionPrimaryKey;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.database.entity.RoadSection;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.database.repository.RoadSectionRepository;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.graphhopper.GraphHopperDriver;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.graphhopper.GraphHopperTestDataService;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.graphhopper.NetworkDataService;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.graphhopper.dto.Link;
import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
import nu.ndw.nls.routingmapmatcher.exception.GraphHopperNotImportedException;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.locationtech.jts.geom.Coordinate;

@Slf4j
@RequiredArgsConstructor
public class GraphHopperStepDefinitions {

    private final GraphHopperDriver graphHopperDriver;

    private final NetworkDataService networkDataService;

    private final RoadSectionRepository roadSectionRepository;

    private final GraphHopperTestDataService graphHopperTestDataService;

    private final GeometryFactoryWgs84 geometryFactoryWgs84 = new GeometryFactoryWgs84();

    @Given("a simple Graph Hopper network")
    public void graphHopperNetwork() {

        graphHopperTestDataService.buildSimpleNetwork().buildNetwork();
    }

    @Given("a simpel nwb network")
    public void prepareNwbDatabaseNetwork() {

        graphHopperTestDataService.buildSimpleNetwork().buildNwbDatabaseNetwork();
    }

    @Given("NWB unroutable road sections")
    public void graphHopperNetworkWithRoads(List<NwbRoadSection> nwbRoadSections) {

        nwbRoadSections.forEach(nwbRoadSection -> {
            roadSectionRepository.save(RoadSection.builder()
                    .primaryKey(new NwbRoadSectionPrimaryKey(1, nwbRoadSection.id()))
                    .junctionIdFrom(nwbRoadSection.junctionIdFrom())
                    .junctionIdTo(nwbRoadSection.junctionIdTo())
                    .roadOperatorType("Municipality")
                    .geometry(networkDataService.createRijksdriehoekLineString(geometryFactoryWgs84.createLineString(
                            new Coordinate[]{
                                    new Coordinate(50, 51),
                                    new Coordinate(51, 51)
                            }
                    )))
                    .build());
        });
    }

    @Then("written graphhopper on disk should be comparable with network")
    public void graphhopperShouldBeComparableWithSimpleNwbNetwork() throws GraphHopperNotImportedException {

        NetworkGraphHopper network = graphHopperDriver.loadFromDisk();

        QueryGraph queryGraph = QueryGraph.create(network.getBaseGraph(), List.of());
        EdgeExplorer edgeExplorer = queryGraph.createEdgeExplorer();
        Set<String> roadsDetected = new HashSet<>();

        for (int startNode = 0; startNode < queryGraph.getNodes(); startNode++) {
            EdgeIterator edgeIterator = edgeExplorer.setBaseNode(startNode);
            while (edgeIterator.next()) {
                int fromNode = edgeIterator.getBaseNode() + 1;
                int toNode = edgeIterator.getAdjNode() + 1;
                roadsDetected.add(fromNode + "-" + toNode);
            }
        }

        assertThat(networkDataService.getLinks().stream()
                .map(Link::getAccessibilityLink)
                .allMatch(link ->
                        roadsDetected.contains(link.getFromNodeId() + "-" + link.getToNodeId())
                                && roadsDetected.contains(link.getToNodeId() + "-" + link.getFromNodeId())))
                .withFailMessage("Not all roads were detected. Detected roads: %s".formatted(roadsDetected))
                .isTrue();
        assertThat(network.getBaseGraph().getNodes()).isEqualTo(11);
    }


}
