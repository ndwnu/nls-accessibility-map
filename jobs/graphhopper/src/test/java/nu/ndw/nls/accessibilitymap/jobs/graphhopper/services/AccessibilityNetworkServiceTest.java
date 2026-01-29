package nu.ndw.nls.accessibilitymap.jobs.graphhopper.services;

import static nu.ndw.nls.events.NlsEventType.ACCESSIBILITY_ROUTING_NETWORK_UPDATED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.SneakyThrows;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperNetworkSettingsBuilder;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.AccessibilityLink;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.network.GraphhopperMetaData;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service.NetworkMetaDataService;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.service.AccessibilityNwbRoadSectionService;
import nu.ndw.nls.accessibilitymap.jobs.graphhopper.mapper.AccessibilityRoutingNetworkEventMapper;
import nu.ndw.nls.accessibilitymap.jobs.graphhopper.services.mapper.AccessibilityNwbRoadSectionToLinkMapper;
import nu.ndw.nls.db.nwb.jooq.services.NwbVersionCrudService;
import nu.ndw.nls.events.NlsEvent;
import nu.ndw.nls.routingmapmatcher.network.GraphHopperNetworkService;
import nu.ndw.nls.routingmapmatcher.network.model.RoutingNetworkSettings;
import nu.ndw.nls.springboot.core.time.ClockService;
import nu.ndw.nls.springboot.messaging.services.MessageService;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityNetworkServiceTest {

    private AccessibilityNetworkService accessibilityNetworkService;

    @Mock
    private NlsEvent publishedEvent;

    @Mock
    private GraphHopperNetworkService indexedGraphHopperNetworkService;

    @Mock
    private AccessibilityNwbRoadSectionService accessibilityNwbRoadSectionService;

    @Mock
    private AccessibilityNwbRoadSectionToLinkMapper accessibilityNwbRoadSectionToLinkMapper;

    @Mock
    private GraphHopperNetworkSettingsBuilder graphHopperNetworkSettingsBuilder;

    @Mock
    private MessageService messageService;

    @Mock
    private AccessibilityRoutingNetworkEventMapper accessibilityRoutingNetworkEventMapper;

    @Mock
    private NetworkMetaDataService networkMetaDataService;

    @Mock
    private NwbVersionCrudService nwbVersionCrudService;

    @Mock
    private RoutingNetworkSettings<AccessibilityLink> routingNetworkSettings;

    @Mock
    private AccessibilityLink accessibilityLink;

    @Mock
    private AccessibilityNwbRoadSection accessibilityNwbRoadSection;

    @Mock
    private ClockService clockService;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    private Path testFolder;

    @SneakyThrows
    @BeforeEach
    void setUp() {

        accessibilityNetworkService = new AccessibilityNetworkService(
                indexedGraphHopperNetworkService,
                accessibilityNwbRoadSectionService,
                graphHopperNetworkSettingsBuilder,
                messageService,
                accessibilityRoutingNetworkEventMapper,
                networkMetaDataService,
                nwbVersionCrudService,
                accessibilityNwbRoadSectionToLinkMapper,
                clockService);

        testFolder = Files.createTempDirectory("test-accessibility-network-service");
    }

    @AfterEach
    void tearDown() throws IOException {

        FileUtils.deleteDirectory(testFolder.toFile());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void storeLatestNetworkOnDisk(boolean publishEvents) throws IOException {

        Path graphopperPath = testFolder.resolve("latest");
        OffsetDateTime timestamp = OffsetDateTime.parse("2022-12-06T09:00:00.001Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        when(graphHopperNetworkSettingsBuilder.getLatestPath()).thenReturn(graphopperPath);

        int nwbVersionId = 123;
        when(nwbVersionCrudService.findLatestVersionId()).thenReturn(nwbVersionId);
        when(accessibilityNwbRoadSectionService.findAllByVersion(nwbVersionId)).thenReturn(List.of(accessibilityNwbRoadSection));
        when(accessibilityNwbRoadSectionToLinkMapper.map(accessibilityNwbRoadSection)).thenReturn(accessibilityLink);
        when(clockService.now()).thenReturn(timestamp);

        when(graphHopperNetworkSettingsBuilder.networkSettingsWithData(List.of(accessibilityLink), timestamp.toInstant()))
                .thenReturn(routingNetworkSettings);

        if (publishEvents) {
            when(graphHopperNetworkSettingsBuilder.publishEvents()).thenReturn(true);
            when(accessibilityRoutingNetworkEventMapper.map(nwbVersionId, timestamp.toInstant())).thenReturn(publishedEvent);
            when(publishedEvent.getType()).thenReturn(ACCESSIBILITY_ROUTING_NETWORK_UPDATED);
            when(accessibilityRoutingNetworkEventMapper.map(nwbVersionId, timestamp.toInstant())).thenReturn(publishedEvent);
        }

        accessibilityNetworkService.storeLatestNetworkOnDisk();

        assertThat(Files.exists(graphopperPath)).isTrue();

        verify(indexedGraphHopperNetworkService).storeOnDisk(routingNetworkSettings);
        verify(networkMetaDataService).saveMetaData(new GraphhopperMetaData(nwbVersionId));

        if (publishEvents) {
            verify(messageService).publish(publishedEvent);
            loggerExtension.containsLog(
                    Level.INFO, "Sending %s event for NWB version %s"
                            .formatted(
                                    ACCESSIBILITY_ROUTING_NETWORK_UPDATED.getLabel(),
                                    nwbVersionId));
        } else {
            verifyNoMoreInteractions(messageService);
        }

        loggerExtension.containsLog(Level.INFO, "Starting network creation for %s".formatted(graphopperPath.toAbsolutePath()));
        loggerExtension.containsLog(Level.INFO, "Retrieving link data");
        loggerExtension.containsLog(Level.INFO, "Creating GraphHopper network and writing to disk");
    }

    @Test
    void networkExists_no_network() {
        when(graphHopperNetworkSettingsBuilder.getLatestPath()).thenReturn(testFolder.resolve("latest"));
        assertThat(accessibilityNetworkService.networkExists()).isFalse();
    }
}
