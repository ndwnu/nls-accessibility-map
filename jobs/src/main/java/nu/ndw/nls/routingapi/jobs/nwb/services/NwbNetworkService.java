package nu.ndw.nls.routingapi.jobs.nwb.services;

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
public class NwbNetworkService {

    private static final String GRAPHHOPPER = "graphhopper";
    private static final String NETWORK_NAME = "nwb_latest";

    private final AccessibilityGraphHopperNetworkService accessibilityGraphHopperNetworkService;
    private final NwbLinkService linkService;
    private final NwbVersionCrudService versionService;

    @Transactional
    public void storeLatestNetworkOnDisk() throws IOException {
        Files.createDirectories(Path.of(GRAPHHOPPER, NETWORK_NAME));
        Integer latestVersionId = versionService.findLatestVersionId();
        var routingNetwork = RoutingNetwork.builder()
                .networkNameAndVersion(NETWORK_NAME)
                .linkSupplier(() -> linkService.getLinks(latestVersionId).iterator())
                .build();
        accessibilityGraphHopperNetworkService.storeOnDisk(routingNetwork, Path.of(GRAPHHOPPER));
    }
}