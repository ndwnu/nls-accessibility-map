package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper;

import static java.nio.file.attribute.PosixFilePermission.OTHERS_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;
import static nu.ndw.nls.accessibilitymap.shared.model.NetworkConstants.PROFILE;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper.dto.AllAccessibleLinkBuilderConsumer;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper.dto.Node;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper.utils.LongSequenceSupplier;
import nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink;
import nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.AccessibilityLinkBuilder;
import nu.ndw.nls.accessibilitymap.shared.network.dtos.AccessibilityGraphhopperMetaData;
import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
import nu.ndw.nls.routingmapmatcher.exception.GraphHopperNotImportedException;
import nu.ndw.nls.routingmapmatcher.network.GraphHopperNetworkService;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import nu.ndw.nls.routingmapmatcher.network.model.RoutingNetworkSettings;
import org.locationtech.jts.geom.Coordinate;

@RequiredArgsConstructor
public class NetworkGraphHopperBuilder {

    private static final FileAttribute<?> FOLDER_PERMISSIONS = PosixFilePermissions.asFileAttribute(
            Set.of(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, OTHERS_READ, OTHERS_EXECUTE));


    private static final String VERSION = "accessibility_latest";
    private static final String ACCESSIBILITY_META_DATA_JSON = "accessibility_meta_data.json";

    private final GraphHopperConfiguration graphHopperConfiguration;

    private final GraphHopperNetworkService graphHopperNetworkService;

    private List<AccessibilityLink> links = new ArrayList<>();

    private final GeometryFactoryWgs84 geometryFactoryWgs84 = new GeometryFactoryWgs84();

    private final LongSequenceSupplier longSequenceSupplier = new LongSequenceSupplier();


    public static NetworkGraphHopperBuilder builder(
            GraphHopperConfiguration graphHopperConfiguration,
            GraphHopperNetworkService graphHopperNetworkService) {

        return new NetworkGraphHopperBuilder(graphHopperConfiguration, graphHopperNetworkService);
    }

    public NetworkGraphHopperBuilder createRoad(
            Node startNode,
            Node endNode) {

        return createRoad(startNode, endNode, new AllAccessibleLinkBuilderConsumer());
    }

    public NetworkGraphHopperBuilder createRoad(
            Node startNode,
            Node endNode,
            Consumer<AccessibilityLinkBuilder> linkConfigurerconsumer) {

        AccessibilityLinkBuilder linkBuilder = AccessibilityLink.builder()
                .id(longSequenceSupplier.next())
                .fromNodeId(startNode.id())
                .toNodeId(endNode.id())
                .geometry(geometryFactoryWgs84.createLineString(
                        new Coordinate[]{startNode.coordinate(), endNode.coordinate()}));

        linkConfigurerconsumer.accept(linkBuilder);

        links.add(linkBuilder.build());

        return this;
    }

    public NetworkGraphHopper build() {

        RoutingNetworkSettings<AccessibilityLink> routingNetworkSettings = RoutingNetworkSettings.builder(
                        AccessibilityLink.class)
                .indexed(true)
                .linkSupplier(() -> join(List.of(links)).iterator())
                .graphhopperRootPath(graphHopperConfiguration.getLocationOnDisk())
                .networkNameAndVersion(VERSION)
                .profiles(List.of(PROFILE))
                .build();

        Path fullStorageLocation = graphHopperConfiguration.getLocationOnDisk()
                .resolve(routingNetworkSettings.getNetworkNameAndVersion()).normalize();
        try {
            Files.createDirectories(fullStorageLocation, FOLDER_PERMISSIONS);
        } catch (IOException exception) {
            fail(exception);
        }

        graphHopperNetworkService.storeOnDisk(routingNetworkSettings);
        try {
            new ObjectMapper().writeValue(fullStorageLocation.resolve(ACCESSIBILITY_META_DATA_JSON).toFile(), new AccessibilityGraphhopperMetaData(123));
        } catch (IOException exception) {
            fail(exception);
        }

        try {
            return graphHopperNetworkService.loadFromDisk(routingNetworkSettings);
        } catch (GraphHopperNotImportedException exception) {
            fail(exception);
            return null;
        }
    }

    private <T> List<T> join(List<List<T>> lists) {
        return lists.stream().flatMap(List::stream).toList();
    }
}
