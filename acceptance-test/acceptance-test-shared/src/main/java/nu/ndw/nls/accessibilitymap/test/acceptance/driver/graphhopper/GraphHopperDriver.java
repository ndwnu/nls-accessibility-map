package nu.ndw.nls.accessibilitymap.test.acceptance.driver.graphhopper;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.graphhopper.supplier.AccessibilityNwbRoadSectionDtoSupplier;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.graphhopper.supplier.AccessibilityVersionSupplier;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.rabbitmq.RabbitMQMessageDriver;
import nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode;
import nu.ndw.nls.springboot.test.component.driver.job.JobDriver;
import nu.ndw.nls.springboot.test.graph.dto.Direction;
import nu.ndw.nls.springboot.test.graph.dto.Graph;
import nu.ndw.nls.springboot.test.graph.exporter.database.nwb.NwbDatabaseExporter;
import nu.ndw.nls.springboot.test.graph.exporter.database.nwb.dto.NwbDataAccessSettings;
import nu.ndw.nls.springboot.test.graph.exporter.database.nwb.dto.NwbDataAccessSettings.NwbDataAccessSettingsBuilder;
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

    @Getter
    private Integer lastBuiltGraphVersion;

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

    public GraphHopperDriver insertNwbData() {
        return insertNwbData(null, Map.of(), null);
    }

    public GraphHopperDriver insertNwbDataWithVersion(String version) {
        return insertNwbData(version, Map.of(), null);
    }

    public GraphHopperDriver insertNwbDataWithCarriagewayOverrides(Map<Long, CarriagewayTypeCode> carriagewayOverrides) {
        return insertNwbData(null, carriagewayOverrides, null);
    }

    public GraphHopperDriver insertNwbDataWithRoadOperatorCode(String roadOperatorCode) {
        return insertNwbData(null, Map.of(), roadOperatorCode);
    }

    private GraphHopperDriver insertNwbData(String version, Map<Long, CarriagewayTypeCode> carriagewayOverrides,
            String roadOperatorCode) {
        lastBuiltGraph = graphDataBuilder.build();

        NwbDataAccessSettingsBuilder nwbDataAccessSettingsBuilder = NwbDataAccessSettings.builder()
                .nwbRoadSectionDtoSupplier(new AccessibilityNwbRoadSectionDtoSupplier(carriagewayOverrides, roadOperatorCode));
        if (Objects.nonNull(version)) {
            nwbDataAccessSettingsBuilder.versionDtoSupplier(new AccessibilityVersionSupplier(version));
        }

        NwbDataAccessSettings nwbDataAccessSettings = nwbDataAccessSettingsBuilder.build();

        lastBuiltGraphVersion = nwbDataAccessSettings.getVersionDtoSupplier().create().getVersionId();

        nwbDatabaseExporter.export(lastBuiltGraph, nwbDataAccessSettings);

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
