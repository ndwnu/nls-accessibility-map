package nu.ndw.nls.accessibilitymap.accessibility.service;

import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.GraphHopperNetwork;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service.NetworkMetaDataService;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.service.AccessibilityNwbRoadSectionService;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessibilityContextProvider {

    private final GraphHopperService graphHopperService;

    private final AccessibilityNwbRoadSectionService accessibilityNwbRoadSectionService;

    private final NetworkMetaDataService networkMetaDataService;

    public AccessibilityContext get() {
        GraphHopperNetwork graphHopperNetwork = graphHopperService.getNetworkGraphHopper();
        int nwbVersionId = networkMetaDataService.loadMetaData().nwbVersion();

        return AccessibilityContext.builder()
                .nwbVersionId(nwbVersionId)
                .graphHopperNetwork(graphHopperNetwork)
                .accessibilityNwbRoadSections(accessibilityNwbRoadSectionService.getRoadSectionsByIdForNwbVersion(nwbVersionId))
                .build();
    }
}
