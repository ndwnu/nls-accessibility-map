package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EncodingManager;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.IsochroneArguments;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityNetwork;
import nu.ndw.nls.routingmapmatcher.isochrone.algorithm.IsoLabel;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class IsochroneService {

    public List<IsoLabel> search(AccessibilityNetwork accessibilityNetwork, IsochroneArguments isochroneArguments) {

        QueryGraph queryGraph = accessibilityNetwork.getQueryGraph();
        var isochroneByTimeDistanceAndWeight = IsochroneShortestPathTreeFactory.createIsochroneByTimeDistanceAndWeight(
                queryGraph,
                isochroneArguments);

        List<IsoLabel> isoLabels = new ArrayList<>();
        isochroneByTimeDistanceAndWeight.search(accessibilityNetwork.getFrom().getClosestNode(), isoLabels::add);

        EncodingManager encodingManager = accessibilityNetwork.getNetworkData().getNetworkGraphHopper().getEncodingManager();
        return isoLabels.stream()
                .filter(IsochroneFilter::isNotRoot)
                .filter(isoLabel -> IsochroneFilter.isWithinMunicipality(encodingManager, queryGraph, isoLabel, isochroneArguments))
                .filter(isoLabel -> IsochroneFilter.isWithinBoundingBox(queryGraph, isoLabel, isochroneArguments))
                .toList();
    }
}
