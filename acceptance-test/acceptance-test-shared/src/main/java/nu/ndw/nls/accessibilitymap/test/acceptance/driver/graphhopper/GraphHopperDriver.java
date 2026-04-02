package nu.ndw.nls.accessibilitymap.test.acceptance.driver.graphhopper;

import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.graphhopper.supplier.AccessibilityNwbRoadSectionDtoSupplier;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.rabbitmq.RabbitMQMessageDriver;
import nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode;
import nu.ndw.nls.springboot.test.component.driver.job.JobDriver;
import nu.ndw.nls.springboot.test.graph.dto.Direction;
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

    public GraphHopperDriver createDirectionalRoad(long startNodeId, long endNodeId, Set<Direction> directions) {

        graphDataBuilder.createEdge(startNodeId, endNodeId, directions);
        return this;
    }

    public GraphHopperDriver createNode(long id, double x, double y) {

        graphDataBuilder.createNode(id, y, x);
        return this;
    }

    @SneakyThrows
    @SuppressWarnings("java:S3658")
    public GraphHopperDriver insertNwbData() {
        return insertNwbDataWithCarriagewayOverrides(Map.of());
    }

    @SneakyThrows
    @SuppressWarnings("java:S3658")
    public GraphHopperDriver insertNwbDataWithCarriagewayOverrides(Map<Long, CarriagewayTypeCode> carriagewayOverrides) {
        lastBuiltGraph = graphDataBuilder.build();
        nwbDatabaseExporter.export(
                lastBuiltGraph,
                NwbDataAccessSettings.builder()
                        .nwbRoadSectionDtoSupplier(new AccessibilityNwbRoadSectionDtoSupplier(carriagewayOverrides))
                        .build());

        return this;
    }

    public void rebuildCache() {
        // RabbitMQ is automatically configured on startup. But if the job is not yet running, the queues are yet configured.
        jobDriver.run("job", "configureRabbitMQ");

        rabbitMqDriver.publishNwbImportedEvent();
        jobDriver.run("job", "rebuildNetworkCache");
    }

    public Graph generate(GenerateSpecification generateSpecification) {
        lastBuiltGraph = graphGeneratorService.generate(generateSpecification);
        return lastBuiltGraph;
    }
}
