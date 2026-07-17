package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.PMap;
import com.graphhopper.util.shapes.BBox;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.algorithm.RestrictionsIsochroneLabel;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.algorithm.limit.ExploreLimitCarAccessible;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.AccessibilityLink;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.IsochroneArguments;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.mapper.AccessibilityLinkCarMapper;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbData;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbDataUpdates;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityNetwork;
import nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode;
import nu.ndw.nls.routingmapmatcher.exception.GraphHopperNotImportedException;
import nu.ndw.nls.routingmapmatcher.isochrone.v2.dto.IsochroneLabel;
import nu.ndw.nls.routingmapmatcher.network.GraphHopperNetworkService;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import nu.ndw.nls.routingmapmatcher.network.model.DirectionalDto;
import nu.ndw.nls.routingmapmatcher.network.model.RoutingNetworkSettings;
import nu.ndw.nls.springboot.test.graph.NlsTestGraphCoreAutoConfiguration;
import nu.ndw.nls.springboot.test.graph.dto.Graph;
import nu.ndw.nls.springboot.test.graph.exporter.graphhopper.NlsTestGraphExporterGraphHopperAutoConfiguration;
import nu.ndw.nls.springboot.test.graph.exporter.graphhopper.service.GraphHopperExporter;
import nu.ndw.nls.springboot.test.graph.exporter.graphhopper.service.dto.GraphHopperExporterSettings;
import nu.ndw.nls.springboot.test.graph.service.GraphDataBuilder;
import org.apache.commons.io.FileUtils;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        NlsTestGraphCoreAutoConfiguration.class,
        NlsTestGraphExporterGraphHopperAutoConfiguration.class,
        AccessibilityLinkCarMapper.class
})
class IsochroneServiceIT {

    private static final int NWB_VERSION = 1;

    private static final int MUNICIPALITY_ID = 100;

    private static final int NON_MATCHING_MUNICIPALITY_ID = 999;

    private static final double SEARCH_DISTANCE_IN_METRES = 100_000;

    private static final String NETWORK_FOLDER_NAME = "latest";

    private static final double NODE_1_LAT = 52.1;

    private static final double NODE_1_LON = 4.51;

    private static final double NODE_2_LAT = 52.2;

    private static final double NODE_2_LON = 4.52;

    private static final double NODE_3_LAT = 52.3;

    private static final double NODE_3_LON = 4.53;

    private IsochroneService isochroneService;

    @Autowired
    private GraphHopperExporter graphHopperExporter;

    @Autowired
    private GraphHopperNetworkService graphHopperNetworkService;

    private AccessibilityNetwork accessibilityNetwork;

    private Path testDirectory;

    @BeforeEach
    void setUp() throws IOException, GraphHopperNotImportedException {
        isochroneService = new IsochroneService();
        testDirectory = Files.createTempDirectory(getClass().getSimpleName());

        Graph graph = buildGraph();
        NetworkGraphHopper networkGraphHopper = exportAndLoadNetwork(graph);
        NetworkData networkData = buildNetworkData(graph, networkGraphHopper);
        accessibilityNetwork = buildAccessibilityNetwork(networkGraphHopper, networkData);
    }

    @AfterEach
    void tearDown() throws IOException {
        FileUtils.deleteDirectory(testDirectory.toFile());
    }

    @Test
    void search() {
        IsochroneArguments isochroneArguments = buildIsochroneArguments(null, null);

        List<IsochroneLabel> result = isochroneService.search(accessibilityNetwork, isochroneArguments);

        assertThat(result)
                .hasSize(4)
                .satisfiesExactly(
                        label -> assertRestrictionsIsochroneLabel(label, 2, 1, 2, 802987L, 11152.592, 802.986624),
                        label -> assertRestrictionsIsochroneLabel(label, 0, 0, 0, 1605963L, 22305.032, 1605.9623040000001),
                        label -> assertRestrictionsIsochroneLabel(label, 1, 1, 3, 1605974L, 22305.184, 1605.973248),
                        label -> assertRestrictionsIsochroneLabel(label, 2, 0, 1, 2408939L, 33457.472, 2408.937984)
                )
                .noneMatch(IsochroneLabel::isRoot)
                .allMatch(IsochroneLabel::isDeleted);
    }

    @Test
    void search_withNonMatchingMunicipalityId_returnsEmpty() {
        IsochroneArguments isochroneArguments = buildIsochroneArguments(NON_MATCHING_MUNICIPALITY_ID, null);

        List<IsochroneLabel> result = isochroneService.search(accessibilityNetwork, isochroneArguments);

        assertThat(result).isEmpty();
    }

    @Test
    void search_withNonIntersectingBoundingBox_returnsEmpty() {
        IsochroneArguments isochroneArguments = buildIsochroneArguments(null, new BBox(0.0, 1.0, 0.0, 1.0));

        List<IsochroneLabel> result = isochroneService.search(accessibilityNetwork, isochroneArguments);

        assertThat(result).isEmpty();
    }

    private IsochroneArguments buildIsochroneArguments(Integer municipalityId, BBox boundingBox) {
        return IsochroneArguments.builder()
                .exploreLimit(new ExploreLimitCarAccessible(
                        accessibilityNetwork.getQueryGraph(),
                        accessibilityNetwork.getNetworkData().getNwbNetworkData(),
                        new EdgeIteratorStateReverseExtractor()))
                .weighting(accessibilityNetwork.getWeighting())
                .searchDistanceInMetres(SEARCH_DISTANCE_IN_METRES)
                .municipalityId(municipalityId)
                .boundingBox(boundingBox)
                .reverseFlow(false)
                .build();
    }

    private Graph buildGraph() {
        GraphDataBuilder graphDataBuilder = new GraphDataBuilder();
        graphDataBuilder.createNode(1L, NODE_1_LAT, NODE_1_LON);
        graphDataBuilder.createNode(2L, NODE_2_LAT, NODE_2_LON);
        graphDataBuilder.createNode(3L, NODE_3_LAT, NODE_3_LON);
        graphDataBuilder.createEdge(1L, 2L);
        graphDataBuilder.createEdge(2L, 3L);
        return graphDataBuilder.build();
    }

    private NetworkGraphHopper exportAndLoadNetwork(Graph graph) throws IOException, GraphHopperNotImportedException {
        Path networkLocation = testDirectory.resolve("graphhopper");

        graphHopperExporter.export(
                graph, GraphHopperExporterSettings.builder(AccessibilityLink.class)
                        .locationOnDisk(networkLocation)
                        .networkFolderName(NETWORK_FOLDER_NAME)
                        .profiles(List.of(NetworkConstants.CAR_PROFILE))
                        .linkSupplier(edge -> AccessibilityLink.builder()
                                .id(edge.getId())
                                .fromNodeId(edge.getFromNode().getId())
                                .toNodeId(edge.getToNode().getId())
                                .geometry(edge.getWgs84LineString())
                                .distanceInMeters(edge.getDistanceInMeters())
                                .municipalityCode(MUNICIPALITY_ID)
                                .accessibility(DirectionalDto.<Boolean>builder()
                                        .forward(edge.isForward())
                                        .reverse(edge.isBackward())
                                        .build())
                                .build())
                        .build());

        return graphHopperNetworkService.loadFromDisk(RoutingNetworkSettings.builder(AccessibilityLink.class)
                .indexed(true)
                .graphhopperRootPath(networkLocation)
                .networkNameAndVersion(NETWORK_FOLDER_NAME)
                .profiles(List.of(NetworkConstants.CAR_PROFILE))
                .build());
    }

    private NetworkData buildNetworkData(Graph graph, NetworkGraphHopper networkGraphHopper) {
        List<AccessibilityNwbRoadSection> roadSections = graph.getEdges().stream()
                .map(edge -> AccessibilityNwbRoadSection.builder()
                        .roadSectionId(edge.getId())
                        .fromNode(edge.getFromNode().getId())
                        .toNode(edge.getToNode().getId())
                        .municipalityId(MUNICIPALITY_ID)
                        .geometry(null)
                        .forwardAccessible(edge.isForward())
                        .backwardAccessible(edge.isBackward())
                        .carriagewayTypeCode(CarriagewayTypeCode.HR)
                        .functionalRoadClass("1")
                        .build())
                .toList();

        NwbData nwbData = new NwbData(NWB_VERSION, roadSections);
        NwbDataUpdates nwbDataUpdates = new NwbDataUpdates(NWB_VERSION, List.of());
        return new NetworkData(networkGraphHopper, nwbData, nwbDataUpdates);
    }

    private static void assertRestrictionsIsochroneLabel(
            IsochroneLabel label,
            int node,
            int edge,
            int edgeKey,
            long time,
            double distance,
            double weight) {
        assertThat(label).isInstanceOf(RestrictionsIsochroneLabel.class);
        assertThat(label.getNode()).isEqualTo(node);
        assertThat(label.getEdge()).isEqualTo(edge);
        assertThat(label.getEdgeKey()).isEqualTo(edgeKey);
        assertThat(label.getTimeInMilliSeconds()).isEqualTo(time);
        assertThat(label.getDistanceInMeters()).isCloseTo(distance, Offset.offset(1e-6));
        assertThat(label.getWeight()).isCloseTo(weight, Offset.offset(1e-6));
        assertThat(label.isLeafNode()).isFalse();
        assertThat(label.isDeleted()).isTrue();
        assertThat(((RestrictionsIsochroneLabel) label).getRestrictions()).isEmpty();
    }

    private AccessibilityNetwork buildAccessibilityNetwork(NetworkGraphHopper networkGraphHopper, NetworkData networkData) {
        Snap fromSnap = networkGraphHopper.getLocationIndex().findClosest(
                NODE_1_LAT, NODE_1_LON, EdgeFilter.ALL_EDGES);

        QueryGraph queryGraph = QueryGraph.create(networkGraphHopper.getBaseGraph(), List.of(fromSnap));

        Weighting weighting = queryGraph.wrapWeighting(
                networkGraphHopper.createWeighting(NetworkConstants.CAR_PROFILE, new PMap()));

        return new AccessibilityNetwork(
                networkData,
                queryGraph,
                new Restrictions(),
                Map.of(),
                fromSnap,
                null,
                weighting);
    }
}
