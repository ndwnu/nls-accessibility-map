package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.TraversalMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.algorithm.RestrictionIsochroneAlgorithm;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.IsochroneArguments;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityNetwork;
import nu.ndw.nls.routingmapmatcher.isochrone.v2.dto.IsochroneLabel;
import nu.ndw.nls.routingmapmatcher.isochrone.v2.exploration.ExploreDistanceLimit;
import nu.ndw.nls.routingmapmatcher.isochrone.v2.exploration.ExploreLimitComposite;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class IsochroneService {

    public List<IsochroneLabel> search(
            AccessibilityNetwork accessibilityNetwork,
            IsochroneArguments isochroneArguments) {

        QueryGraph queryGraph = accessibilityNetwork.getQueryGraph();

        RestrictionIsochroneAlgorithm restrictionIsochroneAlgorithm = new RestrictionIsochroneAlgorithm(
                accessibilityNetwork.getQueryGraph(),
                accessibilityNetwork.getNetworkData().getNetworkGraphHopper().getEncodingManager(),
                TraversalMode.EDGE_BASED,
                isochroneArguments.reverseFlow(),
                isochroneArguments.weighting(),
                new ExploreLimitComposite<>(List.of(
                        new ExploreDistanceLimit<>(isochroneArguments.searchDistanceInMetres(), true),
                        isochroneArguments.exploreLimit()
                )),
                Comparator.comparingDouble(IsochroneLabel::getTimeInMilliSeconds),
                accessibilityNetwork.getRestrictionsByEdgeKey()
        );

        List<IsochroneLabel> isoLabels = new ArrayList<>();
        restrictionIsochroneAlgorithm.search(accessibilityNetwork.getFrom().getClosestNode(), isoLabels::add);

        EncodingManager encodingManager = accessibilityNetwork.getNetworkData().getNetworkGraphHopper().getEncodingManager();
        return isoLabels.stream()
                .filter(isochroneLabel -> !isochroneLabel.isRoot())
                .filter(isoLabel -> IsochroneFilter.isWithinMunicipality(encodingManager, queryGraph, isoLabel, isochroneArguments))
                .filter(isoLabel -> IsochroneFilter.isWithinBoundingBox(queryGraph, isoLabel, isochroneArguments))
                .toList();
    }
}
