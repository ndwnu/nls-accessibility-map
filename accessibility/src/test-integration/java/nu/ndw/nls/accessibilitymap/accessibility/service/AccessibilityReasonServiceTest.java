//package nu.ndw.nls.accessibilitymap.accessibility.service;
//
//import static java.nio.file.attribute.PosixFilePermission.OTHERS_EXECUTE;
//import static java.nio.file.attribute.PosixFilePermission.OTHERS_READ;
//import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE;
//import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
//import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;
//import static nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants.CAR_PROFILE;
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.when;
//
//import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.attribute.FileAttribute;
//import java.nio.file.attribute.PosixFilePermissions;
//import java.util.List;
//import java.util.Objects;
//import java.util.Set;
//import java.util.stream.Collectors;
//import lombok.extern.slf4j.Slf4j;
//import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
//import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
//import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
//import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperNetworkSettingsBuilder;
//import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperService;
//import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphhopperConfiguration;
//import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.AccessibilityLink;
//import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.network.GraphhopperMetaData;
//import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.factory.IsochroneServiceFactory;
//import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.querygraph.QueryGraphConfigurer;
//import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.querygraph.QueryGraphFactory;
//import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service.NetworkCacheDataService;
//import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityRequest;
//import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityReason;
//import nu.ndw.nls.accessibilitymap.accessibility.service.mapper.AccessibilityReasonsMapper;
//import nu.ndw.nls.accessibilitymap.accessibility.service.mapper.RoadSectionMapper;
//import nu.ndw.nls.accessibilitymap.accessibility.service.mapper.TrafficSignSnapMapper;
//import nu.ndw.nls.accessibilitymap.accessibility.service.route.util.LongSequenceSupplier;
//import nu.ndw.nls.accessibilitymap.accessibility.service.route.util.NetworkDataService;
//import nu.ndw.nls.accessibilitymap.accessibility.service.route.util.dto.Link;
//import nu.ndw.nls.accessibilitymap.accessibility.service.route.util.dto.Node;
//import nu.ndw.nls.accessibilitymap.accessibility.service.route.util.mapper.AccessibilityLinkCarMapper;
//import nu.ndw.nls.accessibilitymap.accessibility.time.ClockBeanConfiguration;
//import nu.ndw.nls.accessibilitymap.accessibility.time.ClockService;
//import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.dto.TrafficSigns;
//import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.TrafficSignDataService;
//import nu.ndw.nls.geometry.crs.CrsTransformer;
//import nu.ndw.nls.routingmapmatcher.RoutingMapMatcherConfiguration;
//import nu.ndw.nls.routingmapmatcher.exception.GraphHopperNotImportedException;
//import nu.ndw.nls.routingmapmatcher.network.GraphHopperNetworkService;
//import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
//import nu.ndw.nls.routingmapmatcher.network.model.RoutingNetworkSettings;
//import org.apache.commons.io.FileUtils;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//
//@Slf4j
//@ExtendWith(MockitoExtension.class)
//@SpringBootTest(classes = {RoutingMapMatcherConfiguration.class, AccessibilityLinkCarMapper.class,
//        IsochroneServiceFactory.class, GraphhopperConfiguration.class, RoadSectionMapper.class,
//        ClockService.class, ClockBeanConfiguration.class, TrafficSignSnapMapper.class, RoadSectionTrafficSignAssigner.class,
//        RoadSectionCombinator.class, AccessibilityReasonsMapper.class
//})
//@TestPropertySource(properties = {
//        "logging.level.nu.ndw.nls.accessibilitymap.accessibility.service: DEBUG",
//        "logging.level.nu.ndw.nls.accessibilitymap.accessibility.graphhopper: DEBUG",
//})
//class AccessibilityReasonServiceTest {
//
//    private static final FileAttribute<?> FOLDER_PERMISSIONS = PosixFilePermissions.asFileAttribute(
//            Set.of(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, OTHERS_READ, OTHERS_EXECUTE));
//
//    @Autowired
//    private GraphHopperNetworkService graphHopperNetworkService;
//
//    @Autowired
//    private GraphHopperService graphHopperService;
//
//
//    @Autowired
//    private AccessibilityReasonsMapper accessibilityReasonsMapper;
//
//
//    @Autowired
//    private EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor;
//
//    @MockitoBean
//    private GraphHopperNetworkSettingsBuilder graphHopperNetworkSettingsBuilder;
//
//
//    @MockitoBean
//    private TrafficSignDataService trafficSignDataService;
//
//    @MockitoBean
//    private MissingRoadSectionProvider missingRoadSectionProvider;
//
//    @MockitoBean
//    private GraphhopperMetaData graphhopperMetaData;
//
//    @Autowired
//    private NetworkCacheDataService networkCacheDataService;
//
//    private AccessibilityReasonService accessibilityReasonService;
//
//    private NetworkGraphHopper graphHopper;
//
//    private NetworkDataService networkDataService;
//
//    private QueryGraphFactory queryGraphFactory;
//
//    private TrafficSignSnapMapper trafficSignSnapMapper;
//
//    private QueryGraphConfigurer queryGraphConfigurer;
//
//    private LongSequenceSupplier trafficSignSequenceSupplier;
//
//    private Path graphHopperDir;
//
//    private TrafficSignRestrictionsBuilder trafficSignRestrictionsBuilder;
//
//    @BeforeEach
//    void setUp() throws IOException, GraphHopperNotImportedException {
//        trafficSignRestrictionsBuilder = new TrafficSignRestrictionsBuilder();
//        graphHopperDir = Files.createTempDirectory(this.getClass().getSimpleName() + "-graphhopper");
//        System.out.println("GraphHopper dir: " + graphHopperDir);
//        queryGraphFactory = new QueryGraphFactory();
//        trafficSignSnapMapper = new TrafficSignSnapMapper();
//        networkDataService = new NetworkDataService(new CrsTransformer());
//        queryGraphConfigurer = new QueryGraphConfigurer(new EdgeIteratorStateReverseExtractor());
//        trafficSignSequenceSupplier = new LongSequenceSupplier();
//        accessibilityReasonService = new AccessibilityReasonService(accessibilityReasonsMapper, graphHopperService, trafficSignSnapMapper,
//                queryGraphConfigurer, edgeIteratorStateReverseExtractor, trafficSignDataService);
//
//        /*
//         5----4-----3
//         |    |     |
//         |    10-9  |
//         |   /   |  |
//         |  6    |  |
//         |   \   |  |
//         |    7--8  |
//         |    |     |
//         0----1-----2
//         */
//        networkDataService
//                //Outer circle
//                .createNode(0, 1, 1)
//                .createNode(1, 5, 1)
//                .createNode(2, 10, 1)
//                .createNode(3, 10, 10)
//                .createNode(4, 5, 10)
//                .createNode(5, 1, 10)
//                .createRoad(0, 1).createRoad(1, 2).createRoad(2, 3).createRoad(3, 4).createRoad(4, 5).createRoad(5, 0)
//
//                //Inner circle
//                .createNode(6, 4, 5)
//                .createNode(7, 5, 2)
//                .createNode(8, 7, 2)
//                .createNode(9, 7, 8)
//                .createNode(10, 5, 8)
//                .createRoad(6, 7).createRoad(7, 8).createRoad(8, 9).createRoad(9, 10).createRoad(10, 6)
//
//                //Inner and outer circles connections
//                .createRoad(7, 1).createRoad(4, 10);
//
//        graphHopper = buildNetwork();
//    }
//
//    @AfterEach
//    void tearDown() throws IOException {
//
//        if (Objects.nonNull(graphHopper) && graphHopperDir.toFile().exists()) {
//            FileUtils.deleteDirectory(graphHopperDir.toFile());
//        }
//    }
//
//
//    @Test
//    void test() {
//
//        var trafficSigns = List.of(
//                buildTrafficSign(2, 3, Direction.BACKWARD, TrafficSignType.C12, 0.9),
//              //  buildTrafficSign(2, 3, Direction.BACKWARD, TrafficSignType.C18, 0.8),
//               // buildTrafficSign(2, 3, Direction.BACKWARD, TrafficSignType.C7, 0.7),
//                buildTrafficSign(3, 4, Direction.FORWARD, TrafficSignType.C18, 0.8),
//                buildTrafficSign(3, 4, Direction.FORWARD, TrafficSignType.C7, 0.7),
//                buildTrafficSign(3, 4, Direction.FORWARD, TrafficSignType.C1, 0.8)
//        );
//
//        var accessibilityRequest = prepareAccessibilityRequest(3, 0, trafficSigns);
//        List<List<AccessibilityReason>> reasons = accessibilityReasonService.getReasons(accessibilityRequest);
//        reasons.forEach(System.out::println);
//        assertThat(reasons).hasSize(2);
//
////        var accessibilityRequest = prepareAccessibilityRequest(3, 0, trafficSigns);
////        var accessibility = accessibilityService.calculateAccessibility(graphHopper, accessibilityRequest);
////
////        var result = accessibilityReasonService.findRestrictions(graphHopper, accessibility);
////
////        assertThat(result.keySet()).containsExactlyInAnyOrder(
////                Set.of("C1"),
////                Set.of("C12")
////        );
//    }
//
//
//    private NetworkGraphHopper buildNetwork() throws IOException, GraphHopperNotImportedException {
//
//        RoutingNetworkSettings<AccessibilityLink> networkSettings = RoutingNetworkSettings.builder(AccessibilityLink.class)
//                .indexed(true)
//                .linkSupplier(() -> join(List.of(networkDataService.getLinks().values().stream()
//                        .map(Link::getAccessibilityLink)
//                        .toList())).iterator())
//                .networkNameAndVersion("0")
//                .graphhopperRootPath(graphHopperDir)
//                .profiles(List.of(CAR_PROFILE))
//                .build();
//        when(graphHopperNetworkSettingsBuilder.defaultNetworkSettings()).thenReturn(networkSettings);
//        graphHopperNetworkService.storeOnDisk(networkSettings);
//        Path fullStorageLocation = graphHopperDir.resolve(networkSettings.getNetworkNameAndVersion()).normalize();
//        Files.createDirectories(fullStorageLocation, FOLDER_PERMISSIONS);
//
//        graphHopperNetworkService.storeOnDisk(networkSettings);
//        return graphHopperNetworkService.loadFromDisk(networkSettings);
//    }
//
//    private TrafficSign buildTrafficSign(int startNodeId, int endNodeId, Direction direction, TrafficSignType trafficSignType,
//            double fraction) {
//
//        var link = networkDataService.findLinkBetweenNodes(startNodeId, endNodeId);
//
//        Node startNode = link.getStartNode();
//        Node endNode = link.getEndNode();
//
//        double latitude = startNode.getLatitude() + ((endNode.getLatitude() - startNode.getLatitude()) * fraction);
//        double longitude = startNode.getLongitude() + ((endNode.getLongitude() - startNode.getLongitude()) * fraction);
//        var id = (int) trafficSignSequenceSupplier.next();
//        var trafficSign = TrafficSign.builder()
//                .id(id)
//                .externalId(id + "")
//                .roadSectionId((int) link.getAccessibilityLink().getId())
//                .trafficSignType(trafficSignType)
//                .latitude(latitude)
//                .longitude(longitude)
//                .networkSnappedLatitude(latitude)
//                .networkSnappedLongitude(longitude)
//                .direction(direction)
//                .build();
//        var restrictions = trafficSignRestrictionsBuilder.buildFor(trafficSign);
//        return trafficSign.withRestrictions(restrictions);
//    }
//
//    private <T> List<T> join(List<List<T>> lists) {
//
//        return lists.stream().flatMap(List::stream).toList();
//    }
//
//    private AccessibilityRequest prepareAccessibilityRequest(int from, int to, List<TrafficSign> trafficSigns) {
//
//        var accessibilityRequest = AccessibilityRequest.builder()
//                .startLocationLatitude(networkDataService.findNodeById(from).getLatitude())
//                .startLocationLongitude(networkDataService.findNodeById(from).getLongitude())
//                .endLocationLatitude(networkDataService.findNodeById(to).getLatitude())
//                .endLocationLongitude(networkDataService.findNodeById(to).getLongitude())
//                .searchRadiusInMeters(Double.MAX_VALUE)
//                .trafficSignTypes(trafficSigns.stream().map(TrafficSign::trafficSignType).collect(Collectors.toSet()))
//                .build();
//
//        when(trafficSignDataService.findAllBy(accessibilityRequest)).thenReturn(trafficSigns);
//        networkCacheDataService.create(new TrafficSigns(trafficSigns), graphHopper);
//        return accessibilityRequest;
//    }
//}
