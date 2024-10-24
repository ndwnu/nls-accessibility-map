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
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.test.component.core.StateManagement;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.database.entity.RoadSection;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.database.entity.Version;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.database.entity.nwbRoadSectionPrimaryKey;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.database.entity.repository.RoadSectionRepository;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.database.entity.repository.VersionRepository;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper.dto.AllAccessibleLinkBuilderConsumer;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink;
import nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.AccessibilityLinkBuilder;
import nu.ndw.nls.accessibilitymap.shared.network.dtos.AccessibilityGraphhopperMetaData;
import nu.ndw.nls.routingmapmatcher.exception.GraphHopperNotImportedException;
import nu.ndw.nls.routingmapmatcher.network.GraphHopperNetworkService;
import nu.ndw.nls.routingmapmatcher.network.model.RoutingNetworkSettings;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GraphHopperDriver implements StateManagement {

    private static final FileAttribute<?> FOLDER_PERMISSIONS = PosixFilePermissions.asFileAttribute(
            Set.of(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, OTHERS_READ, OTHERS_EXECUTE));


    private static final String VERSION = "accessibility_latest";

    private static final String ACCESSIBILITY_META_DATA_JSON = "accessibility_meta_data.json";

    private final GraphHopperConfiguration graphHopperConfiguration;

    private final GraphHopperNetworkService graphHopperNetworkService;

    private final RoadSectionRepository roadSectionRepository;

    private final VersionRepository versionRepository;

    @Getter
    private NetworkData networkData;

    public GraphHopperDriver createRoad(long startNodeId, long endNodeId) {

        networkData.createRoad(startNodeId, endNodeId, new AllAccessibleLinkBuilderConsumer());
        return this;
    }
    public GraphHopperDriver createRoad(long startNodeId, long endNodeId,
            Consumer<AccessibilityLinkBuilder> linkConfigurerconsumer) {

        networkData.createRoad(startNodeId, endNodeId, linkConfigurerconsumer);
        return this;
    }

    public GraphHopperDriver createNode(long id, double x, double y) {

        networkData.createNode(id, x, y);
        return this;
    }

    public void buildNetwork() {

        RoutingNetworkSettings<AccessibilityLink> routingNetworkSettings = RoutingNetworkSettings.builder(
                        AccessibilityLink.class)
                .indexed(true)
                .linkSupplier(() -> join(List.of(networkData.getLinks())).iterator())
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

        versionRepository.save(Version.builder()
                .versionId(1)
                .status("OK")
                .referenceDate(OffsetDateTime.now())
                .imported(OffsetDateTime.now())
                .revision(OffsetDateTime.now())
                .build());

        networkData.getLinks().forEach(accessibilityLink -> {
            roadSectionRepository.save(RoadSection.builder()
                    .primaryKey(new nwbRoadSectionPrimaryKey(1, accessibilityLink.getId()))
                    .junctionIdFrom(accessibilityLink.getFromNodeId())
                    .junctionIdTo(accessibilityLink.getToNodeId())
                    .roadOperatorType("Municipality")
                    .geometry(accessibilityLink.getGeometry())
                    .build());
        });
        graphHopperNetworkService.storeOnDisk(routingNetworkSettings);
        try {
            new ObjectMapper().writeValue(fullStorageLocation.resolve(ACCESSIBILITY_META_DATA_JSON).toFile(),
                    new AccessibilityGraphhopperMetaData(1));
        } catch (IOException exception) {
            fail(exception);
        }

        try {
            networkData.setNetworkGraphHopper(graphHopperNetworkService.loadFromDisk(routingNetworkSettings));
        } catch (GraphHopperNotImportedException exception) {
            fail(exception);
        }
    }

    private <T> List<T> join(List<List<T>> lists) {
        return lists.stream().flatMap(List::stream).toList();
    }

    @Override
    public void clearStateAfterEachScenario() {
        networkData = new NetworkData();
    }

}
