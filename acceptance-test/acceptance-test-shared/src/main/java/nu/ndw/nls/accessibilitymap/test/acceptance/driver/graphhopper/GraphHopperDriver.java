package nu.ndw.nls.accessibilitymap.test.acceptance.driver.graphhopper;

import static java.nio.file.attribute.PosixFilePermission.OTHERS_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;
import static nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants.CAR_PROFILE;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.AccessibilityLink;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.AccessibilityLink.AccessibilityLinkBuilder;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.network.GraphhopperMetaData;
import nu.ndw.nls.accessibilitymap.test.acceptance.core.util.FileService;
import nu.ndw.nls.accessibilitymap.test.acceptance.core.util.LongSequenceSupplier;
import nu.ndw.nls.accessibilitymap.test.acceptance.data.geojson.dto.Feature;
import nu.ndw.nls.accessibilitymap.test.acceptance.data.geojson.dto.FeatureCollection;
import nu.ndw.nls.accessibilitymap.test.acceptance.data.geojson.dto.LineStringGeometry;
import nu.ndw.nls.accessibilitymap.test.acceptance.data.geojson.dto.LineStringProperties;
import nu.ndw.nls.accessibilitymap.test.acceptance.data.geojson.dto.PointGeometry;
import nu.ndw.nls.accessibilitymap.test.acceptance.data.geojson.dto.PointNodeProperties;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.DriverGeneralConfiguration;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.database.entity.NwbRoadSectionPrimaryKey;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.database.entity.RoadSection;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.database.entity.Version;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.database.repository.RoadSectionRepository;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.database.repository.VersionRepository;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.graphhopper.dto.Link;
import nu.ndw.nls.routingmapmatcher.exception.GraphHopperNotImportedException;
import nu.ndw.nls.routingmapmatcher.network.GraphHopperNetworkService;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import nu.ndw.nls.routingmapmatcher.network.model.DirectionalDto;
import nu.ndw.nls.routingmapmatcher.network.model.RoutingNetworkSettings;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GraphHopperDriver {

    private static final FileAttribute<?> FOLDER_PERMISSIONS = PosixFilePermissions.asFileAttribute(
            Set.of(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, OTHERS_READ, OTHERS_EXECUTE));

    private static final String VERSION = "accessibility_latest";

    private static final String ACCESSIBILITY_META_DATA_JSON = "accessibility_meta_data.json";

    private final DriverGeneralConfiguration driverGeneralConfiguration;

    private final GraphHopperConfiguration graphHopperConfiguration;

    private final GraphHopperNetworkService graphHopperNetworkService;

    private final RoadSectionRepository roadSectionRepository;

    private final VersionRepository versionRepository;

    private final FileService fileService;

    private final NetworkDataService networkDataService;

    public GraphHopperDriver createRoad(long startNodeId, long endNodeId) {

        networkDataService.createRoad(startNodeId, endNodeId, new AllAccessibleLinkBuilderConsumer());
        return this;
    }

    public GraphHopperDriver createRoad(long startNodeId, long endNodeId,
            Consumer<AccessibilityLinkBuilder> linkConfigurerconsumer) {

        networkDataService.createRoad(startNodeId, endNodeId, linkConfigurerconsumer);
        return this;
    }

    public GraphHopperDriver createNode(long id, double x, double y) {

        networkDataService.createNode(id, x, y);
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

        RoutingNetworkSettings<AccessibilityLink> routingNetworkSettings = RoutingNetworkSettings.builder(AccessibilityLink.class)
                .indexed(true)
                .linkSupplier(() -> join(List.of(networkDataService.getLinks().stream()
                        .map(Link::getAccessibilityLink)
                        .toList())).iterator())
                .graphhopperRootPath(graphHopperConfiguration.getLocationOnDisk())
                .networkNameAndVersion(VERSION)
                .profiles(List.of(CAR_PROFILE))
                .build();

        Path fullStorageLocation = graphHopperConfiguration.getLocationOnDisk()
                .resolve(routingNetworkSettings.getNetworkNameAndVersion()).normalize();
        try {
            Files.createDirectories(fullStorageLocation, FOLDER_PERMISSIONS);
        } catch (IOException exception) {
            fail(exception);
        }

        buildNwbDatabaseNetwork();

        graphHopperNetworkService.storeOnDisk(routingNetworkSettings);
        try {
            new ObjectMapper().writeValue(
                    fullStorageLocation.resolve(ACCESSIBILITY_META_DATA_JSON).toFile(),
                    new GraphhopperMetaData(1));
        } catch (IOException exception) {
            fail(exception);
        }
        writeGraphHopperNetworkAsGeoJsonToDisk(routingNetworkSettings);
    }

    public void buildNwbDatabaseNetwork() {

        versionRepository.save(Version.builder()
                .versionId(1)
                .status("OK")
                .referenceDate(OffsetDateTime.now())
                .imported(OffsetDateTime.now())
                .revision(OffsetDateTime.now())
                .build());

        networkDataService.getLinks()
                .forEach(link -> roadSectionRepository.save(RoadSection.builder()
                        .primaryKey(new NwbRoadSectionPrimaryKey(1, link.getAccessibilityLink().getId()))
                        .junctionIdFrom(link.getAccessibilityLink().getFromNodeId())
                        .junctionIdTo(link.getAccessibilityLink().getToNodeId())
                        .roadOperatorType("Municipality")
                        .geometry(link.getRijksDiehoekLineString())
                        .build()));
    }

    @SuppressWarnings("java:S3658")
    private void writeGraphHopperNetworkAsGeoJsonToDisk(RoutingNetworkSettings<AccessibilityLink> routingNetworkSettings) {

        Map<String, LinkInfo> roadsDetected = new HashMap<>();
        try {
            NetworkGraphHopper network = graphHopperNetworkService.loadFromDisk(routingNetworkSettings);

            QueryGraph queryGraph = QueryGraph.create(network.getBaseGraph(), List.of());
            EdgeExplorer edgeExplorer = queryGraph.createEdgeExplorer();

            for (int startNode = 0; startNode < queryGraph.getNodes(); startNode++) {
                EdgeIterator edgeIterator = edgeExplorer.setBaseNode(startNode);
                while (edgeIterator.next()) {
                    int fromNode = edgeIterator.getBaseNode() + 1;
                    int toNode = edgeIterator.getAdjNode() + 1;
                    roadsDetected.put(fromNode + "-" + toNode, LinkInfo.builder()
                            .fromNode(fromNode)
                            .toNode(toNode)
                            .edge(edgeIterator.getEdge())
                            .edgeKey(edgeIterator.getEdgeKey())
                            .reverseEdgeKey(edgeIterator.getReverseEdgeKey())
                            .build());
                }
            }
        } catch (GraphHopperNotImportedException exception) {
            fail(exception);
        }

        LongSequenceSupplier idSupplier = new LongSequenceSupplier();
        FeatureCollection featureCollection = FeatureCollection.builder()
                .features(Stream.concat(
                        networkDataService.getLinks().stream()
                                .map(link -> {
                                    LinkInfo linkInfo = roadsDetected.get(
                                            link.getAccessibilityLink().getFromNodeId() + "-" + link.getAccessibilityLink().getToNodeId());
                                    return Feature.builder()
                                            .id(idSupplier.next())
                                            .geometry(LineStringGeometry.builder()
                                                    .coordinates(Arrays.stream(link.getWgs84LineString().getCoordinates()).
                                                            map(coordinate -> List.of(coordinate.getX(), coordinate.getY()))
                                                            .toList())
                                                    .build())
                                            .properties(LineStringProperties.builder()
                                                    .roadSectionId(link.getAccessibilityLink().getId())
                                                    .directions(mapDirections(link.getAccessibilityLink().getAccessibility()))
                                                    .fromNodeId(linkInfo.fromNode())
                                                    .toNodeId(linkInfo.toNode())
                                                    .edge(linkInfo.edge())
                                                    .edgeKey(linkInfo.edgeKey())
                                                    .reverseEdgeKey(linkInfo.reverseEdgeKey())
                                                    .build())
                                            .build();
                                }),
                        networkDataService.getNodes().values().stream()
                                .map(node -> Feature.builder()
                                        .id(idSupplier.next())
                                        .geometry(PointGeometry.builder()
                                                .coordinates(List.of(
                                                        node.getLatLongAsCoordinate().getX(),
                                                        node.getLatLongAsCoordinate().getY()))
                                                .build())
                                        .properties(PointNodeProperties.builder()
                                                .nodeId(node.getId())
                                                .build())
                                        .build())
                ).toList())
                .build();

        try {
            ObjectMapper mapper = JsonMapper.builder().build();

            fileService.writeDataToFile(
                    driverGeneralConfiguration.getDebugFolder().resolve("network.geojson").toFile(),
                    mapper.writeValueAsString(featureCollection));
            log.debug(mapper.writeValueAsString(featureCollection));
        } catch (JsonProcessingException exception) {
            fail(exception);
        }
    }

    @SuppressWarnings("java:S1142")
    private List<Direction> mapDirections(DirectionalDto<Boolean> accessibility) {

        if (accessibility.isEqualForBothDirections()) {
            return List.of(Direction.FORWARD, Direction.BACKWARD);
        }

        if (Boolean.TRUE.equals(accessibility.forward())) {
            return List.of(Direction.FORWARD);
        }

        if (Boolean.TRUE.equals(accessibility.reverse())) {
            return List.of(Direction.FORWARD);
        }

        return List.of();
    }

    @Builder
    private record LinkInfo(
            int fromNode,
            int toNode,
            int edge,
            int edgeKey,
            int reverseEdgeKey) {

    }

    private <T> List<T> join(List<List<T>> lists) {

        return lists.stream().flatMap(List::stream).toList();
    }
}
