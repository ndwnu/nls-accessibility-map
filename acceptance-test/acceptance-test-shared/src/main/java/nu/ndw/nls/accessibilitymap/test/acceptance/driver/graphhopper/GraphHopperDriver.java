package nu.ndw.nls.accessibilitymap.test.acceptance.driver.graphhopper;

import static nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants.CAR_PROFILE;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.AccessibilityLink;
import nu.ndw.nls.accessibilitymap.test.acceptance.data.geojson.supplier.AccessibilityMapGraphHopperLinkPropertiesSupplier;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.DriverGeneralConfiguration;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.graphhopper.mapper.EdgeToAccessibilityLinkMapper;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.graphhopper.supplier.AccessibilityNwbRoadSectionDtoSupplier;
import nu.ndw.nls.routingmapmatcher.exception.GraphHopperNotImportedException;
import nu.ndw.nls.routingmapmatcher.network.GraphHopperNetworkService;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import nu.ndw.nls.routingmapmatcher.network.model.RoutingNetworkSettings;
import nu.ndw.nls.springboot.test.graph.dto.Graph;
import nu.ndw.nls.springboot.test.graph.exporter.database.nwb.NwbDatabaseExporter;
import nu.ndw.nls.springboot.test.graph.exporter.database.nwb.dto.NwbDataAccessSettings;
import nu.ndw.nls.springboot.test.graph.exporter.graphhopper.service.GraphHopperExporter;
import nu.ndw.nls.springboot.test.graph.exporter.graphhopper.service.dto.GraphHopperExporterSettings;
import nu.ndw.nls.springboot.test.graph.exporter.graphhopper.service.geojson.GraphHopperGeoJsonNetworkExporter;
import nu.ndw.nls.springboot.test.graph.exporter.graphhopper.service.geojson.dto.GraphHopperGeoJsonExporterSettings;
import nu.ndw.nls.springboot.test.graph.service.GraphDataBuilder;
import nu.ndw.nls.springboot.test.graph.service.GraphGeneratorService;
import nu.ndw.nls.springboot.test.graph.service.dto.GenerateSpecification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GraphHopperDriver {

    private static final String VERSION = "accessibility_latest";

    private final DriverGeneralConfiguration driverGeneralConfiguration;

    private final GraphHopperConfiguration graphHopperConfiguration;

    private final GraphHopperNetworkService graphHopperNetworkService;

    private final GraphHopperExporter graphHopperExporter;

    private final NwbDatabaseExporter nwbDatabaseExporter;

    private final GraphHopperGeoJsonNetworkExporter graphHopperGeoJsonNetworkExporter;

    private final GraphDataBuilder graphDataBuilder = new GraphDataBuilder();

    private final GraphGeneratorService graphGeneratorService;

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

    public NetworkGraphHopper loadFromDisk() throws GraphHopperNotImportedException {

        RoutingNetworkSettings<AccessibilityLink> routingNetworkSettings = RoutingNetworkSettings.builder(AccessibilityLink.class)
                .indexed(true)
                .graphhopperRootPath(graphHopperConfiguration.getLocationOnDisk())
                .networkNameAndVersion(VERSION)
                .profiles(List.of(CAR_PROFILE))
                .build();

        return graphHopperNetworkService.loadFromDisk(routingNetworkSettings);
    }

    @SuppressWarnings("java:S3658")
    public void buildNetwork() {

        lastBuiltGraph = graphDataBuilder.build();

        nwbDatabaseExporter.export(
                lastBuiltGraph,
                NwbDataAccessSettings.builder()
                        .nwbRoadSectionDtoSupplier(new AccessibilityNwbRoadSectionDtoSupplier())
                        .build());

        var graphHopperSettings = GraphHopperExporterSettings.builder(AccessibilityLink.class)
                .locationOnDisk(graphHopperConfiguration.getLocationOnDisk())
                .linkSupplier(EdgeToAccessibilityLinkMapper::buildFromEdge)
                .networkFolderName(VERSION)
                .profiles(List.of(CAR_PROFILE))
                .metadataFileName("accessibility_meta_data.json")
                .build();
        try {
            graphHopperExporter.export(lastBuiltGraph, graphHopperSettings);
        } catch (IOException exception) {
            fail(exception);
        }

        try {
            graphHopperGeoJsonNetworkExporter.writeGeoJsonToDisk(
                    lastBuiltGraph,
                    GraphHopperGeoJsonExporterSettings.builder()
                            .exportFile(driverGeneralConfiguration.getDebugFolder().resolve("network.geojson").toFile())
                            .graphHopperEdgePropertiesSupplier(new AccessibilityMapGraphHopperLinkPropertiesSupplier())
                            .graphHopperExporterSettings(graphHopperSettings)
                            .build()
            );
        } catch (IOException exception) {
            fail(exception);
        }
    }

    public Graph generate(GenerateSpecification generateSpecification) {
        lastBuiltGraph = graphGeneratorService.generate(generateSpecification);
        return lastBuiltGraph;
    }
}
