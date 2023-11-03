package nu.ndw.nls.accessibilitymap.jobs.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.db.nwb.jooq.services.NwbVersionCrudService;
import nu.ndw.nls.routingmapmatcher.domain.model.RoutingNetwork;
import nu.ndw.nls.routingmapmatcher.graphhopper.AccessibilityGraphHopperNetworkService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccessibilityNetworkService {

    private static final String GRAPHHOPPER = "graphhopper";
    private static final String NETWORK_NAME = "accessibility_latest";

    private final AccessibilityGraphHopperNetworkService accessibilityGraphHopperNetworkService;
    private final AccessibilityLinkService accessibilityLinkService;
    private final NwbVersionCrudService nwbVersionService;

    @Transactional
    public void storeLatestNetworkOnDisk() throws IOException {
        Files.createDirectories(Path.of(GRAPHHOPPER, NETWORK_NAME));
        Integer latestVersionId = nwbVersionService.findLatestVersionId();
        var routingNetwork = RoutingNetwork.builder()
                .networkNameAndVersion(NETWORK_NAME)
                .linkSupplier(() -> accessibilityLinkService.getLinks(latestVersionId).iterator())
                .build();
        accessibilityGraphHopperNetworkService.storeOnDisk(routingNetwork, Path.of(GRAPHHOPPER));
    }
}
