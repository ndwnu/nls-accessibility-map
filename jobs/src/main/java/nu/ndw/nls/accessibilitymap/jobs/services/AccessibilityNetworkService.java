package nu.ndw.nls.accessibilitymap.jobs.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import nu.ndw.nls.accessibilitymap.jobs.services.AccessibilityLinkService.AccessibilityLinkResponse;
import nu.ndw.nls.db.nwb.jooq.services.NwbVersionCrudService;
import nu.ndw.nls.routingmapmatcher.domain.model.RoutingNetwork;
import nu.ndw.nls.routingmapmatcher.graphhopper.AccessibilityGraphHopperNetworkService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccessibilityNetworkService {

    private static final String NETWORK_NAME = "accessibility_latest";

    private final AccessibilityGraphHopperNetworkService accessibilityGraphHopperNetworkService;
    private final AccessibilityLinkService accessibilityLinkService;
    private final NwbVersionCrudService nwbVersionService;
    private final Path graphHopperPath;

    public AccessibilityNetworkService(AccessibilityGraphHopperNetworkService accessibilityGraphHopperNetworkService,
            AccessibilityLinkService accessibilityLinkService, NwbVersionCrudService nwbVersionService,
            @Value("${graphhopper.dir}") String graphHopperDir) {
        this.accessibilityGraphHopperNetworkService = accessibilityGraphHopperNetworkService;
        this.accessibilityLinkService = accessibilityLinkService;
        this.nwbVersionService = nwbVersionService;
        this.graphHopperPath = Path.of(graphHopperDir);
    }

    @Transactional
    public void storeLatestNetworkOnDisk() throws IOException {
        Files.createDirectories(graphHopperPath.resolve(NETWORK_NAME));
        Integer latestVersionId = nwbVersionService.findLatestVersionId();
        AccessibilityLinkResponse linkResponse = accessibilityLinkService.getLinks(latestVersionId);
        var routingNetwork = RoutingNetwork.builder()
                .networkNameAndVersion(NETWORK_NAME)
                .linkSupplier(() -> linkResponse.links().iterator())
                .dataDate(linkResponse.dataDate())
                .build();
        accessibilityGraphHopperNetworkService.storeOnDisk(routingNetwork, graphHopperPath);
    }
}
