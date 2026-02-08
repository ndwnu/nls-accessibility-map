package nu.ndw.nls.accessibilitymap.test.acceptance.driver.graphhopper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.graphhopper.supplier.AccessibilityNwbRoadSectionDtoSupplier;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.rabbitmq.RabbitMQMessageDriver;
import nu.ndw.nls.springboot.test.component.driver.job.JobDriver;
import nu.ndw.nls.springboot.test.graph.dto.Graph;
import nu.ndw.nls.springboot.test.graph.exporter.database.nwb.NwbDatabaseExporter;
import nu.ndw.nls.springboot.test.graph.exporter.database.nwb.dto.NwbDataAccessSettings;
import nu.ndw.nls.springboot.test.graph.service.GraphDataBuilder;
import nu.ndw.nls.springboot.test.graph.service.GraphGeneratorService;
import nu.ndw.nls.springboot.test.graph.service.dto.GenerateSpecification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GraphHopperDriver {

    private final NwbDatabaseExporter nwbDatabaseExporter;

    private final GraphDataBuilder graphDataBuilder = new GraphDataBuilder();

    private final GraphGeneratorService graphGeneratorService;

    private final JobDriver jobDriver;

    private final RabbitMQMessageDriver rabbitMqDriver;

    @Getter
    private Graph lastBuiltGraph;

    public GraphHopperDriver createRoad(long startNodeId, long endNodeId) {

        graphDataBuilder.createEdge(startNodeId, endNodeId);
        return this;
    }

    public GraphHopperDriver createNode(long id, double x, double y) {

        graphDataBuilder.createNode(id, y, x);
        return this;
    }

    @SuppressWarnings("java:S3658")
    public GraphHopperDriver insertNwbData() {

        lastBuiltGraph = graphDataBuilder.build();

        nwbDatabaseExporter.export(
                lastBuiltGraph,
                NwbDataAccessSettings.builder()
                        .nwbRoadSectionDtoSupplier(new AccessibilityNwbRoadSectionDtoSupplier())
                        .build());

        return this;
    }

    public void rebuildCache() {
        rabbitMqDriver.publishNwbImportedEvent();
        jobDriver.run("rebuildNetworkCache");
    }

    public Graph generate(GenerateSpecification generateSpecification) {
        lastBuiltGraph = graphGeneratorService.generate(generateSpecification);
        return lastBuiltGraph;
    }
}
