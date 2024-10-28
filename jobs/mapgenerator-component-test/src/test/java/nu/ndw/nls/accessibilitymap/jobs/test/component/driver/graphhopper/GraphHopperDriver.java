package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper;

import static java.nio.file.attribute.PosixFilePermission.OTHERS_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;
import static nu.ndw.nls.accessibilitymap.shared.model.NetworkConstants.PROFILE;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.test.component.core.StateManagement;
import nu.ndw.nls.accessibilitymap.jobs.test.component.core.util.FileDataProvider;
import nu.ndw.nls.accessibilitymap.jobs.test.component.data.geojson.dto.Feature;
import nu.ndw.nls.accessibilitymap.jobs.test.component.data.geojson.dto.FeatureCollection;
import nu.ndw.nls.accessibilitymap.jobs.test.component.data.geojson.dto.LineStringGeometry;
import nu.ndw.nls.accessibilitymap.jobs.test.component.data.geojson.dto.LineStringProperties;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.DriverGeneralConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.database.entity.RoadSection;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.database.entity.Version;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.database.entity.nwbRoadSectionPrimaryKey;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.database.entity.repository.RoadSectionRepository;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.database.entity.repository.VersionRepository;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper.dto.AllAccessibleLinkBuilderConsumer;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper.dto.Link;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper.utils.LongSequenceSupplier;
import nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink;
import nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.AccessibilityLinkBuilder;
import nu.ndw.nls.accessibilitymap.shared.network.dtos.AccessibilityGraphhopperMetaData;
import nu.ndw.nls.geometry.factories.GeometryFactoryRijksdriehoek;
import nu.ndw.nls.routingmapmatcher.network.GraphHopperNetworkService;
import nu.ndw.nls.routingmapmatcher.network.model.RoutingNetworkSettings;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GraphHopperDriver implements StateManagement {

    private static final FileAttribute<?> FOLDER_PERMISSIONS = PosixFilePermissions.asFileAttribute(
            Set.of(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, OTHERS_READ, OTHERS_EXECUTE));

    private static final String VERSION = "accessibility_latest";

    private static final String ACCESSIBILITY_META_DATA_JSON = "accessibility_meta_data.json";

    private final DriverGeneralConfiguration driverGeneralConfiguration;

    private final GraphHopperConfiguration graphHopperConfiguration;

    private final GraphHopperNetworkService graphHopperNetworkService;

    private final RoadSectionRepository roadSectionRepository;

    private final VersionRepository versionRepository;

    private final GeometryFactoryRijksdriehoek geometryFactoryRijksdriehoek = new GeometryFactoryRijksdriehoek();

    private final FileDataProvider fileDataProvider;

    private final NetworkData networkData;

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

        writeNetworkAsGeoJsonToDisk();

        RoutingNetworkSettings<AccessibilityLink> routingNetworkSettings = RoutingNetworkSettings.builder(
                        AccessibilityLink.class)
                .indexed(true)
                .linkSupplier(() -> join(List.of(networkData.getLinks().stream()
                        .map(Link::getAccessibilityLink)
                        .toList())).iterator())
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

        networkData.getLinks().forEach(link -> {
            roadSectionRepository.save(RoadSection.builder()
                    .primaryKey(new nwbRoadSectionPrimaryKey(1, link.getAccessibilityLink().getId()))
                    .junctionIdFrom(link.getAccessibilityLink().getFromNodeId())
                    .junctionIdTo(link.getAccessibilityLink().getToNodeId())
                    .roadOperatorType("Municipality")
                    .geometry(link.getRijksDiehoekLineString())
                    .build());
        });

        graphHopperNetworkService.storeOnDisk(routingNetworkSettings);
        try {
            new ObjectMapper().writeValue(fullStorageLocation.resolve(ACCESSIBILITY_META_DATA_JSON).toFile(),
                    new AccessibilityGraphhopperMetaData(1));
        } catch (IOException exception) {
            fail(exception);
        }
    }

    @SuppressWarnings("java:S3658")
    private void writeNetworkAsGeoJsonToDisk() {

        LongSequenceSupplier idSupplier = new LongSequenceSupplier();
        FeatureCollection featureCollection = FeatureCollection.builder()
                .features(networkData.getLinks().stream()
                        .map(link -> Feature.builder()
                                .id(idSupplier.next())
                                .geometry(LineStringGeometry.builder()
                                        .coordinates(Arrays.stream(link.getWgs84LineString().getCoordinates()).
                                                map(coordinate -> List.of(coordinate.getX(), coordinate.getY()))
                                                .toList())
                                        .build())
                                .properties(LineStringProperties.builder()
                                        .roadSectionId(link.getAccessibilityLink().getId())
                                        .fromNodeId(link.getAccessibilityLink().getFromNodeId())
                                        .toNodeId(link.getAccessibilityLink().getToNodeId())
                                        .build())
                                .build())
                        .toList())
                .build();

        try {
            final ObjectMapper mapper = JsonMapper.builder().build();

            fileDataProvider.writeDataToFile(
                    driverGeneralConfiguration.getDebugFolder().resolve("network.geojson").toFile(),
                    mapper.writeValueAsString(featureCollection));
            log.debug(mapper.writeValueAsString(featureCollection));
        } catch (JsonProcessingException exception) {
            fail(exception);
        }
    }

    private <T> List<T> join(List<List<T>> lists) {
        return lists.stream().flatMap(List::stream).toList();
    }

    @Override
    public void clearStateAfterEachScenario() {

    }

}
