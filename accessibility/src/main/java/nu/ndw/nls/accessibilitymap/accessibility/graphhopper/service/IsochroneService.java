package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EncodingManager;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.IsochroneArguments;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityNetwork;
import nu.ndw.nls.routingmapmatcher.isochrone.algorithm.IsoLabel;
import nu.ndw.nls.routingmapmatcher.isochrone.mappers.IsochroneMatchMapper;
import nu.ndw.nls.routingmapmatcher.model.IsochroneMatch;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class IsochroneService {

    private final IsochroneMatchMapperFactory isochroneMatchMapperFactory;

    public List<IsochroneMatch> search(
            AccessibilityNetwork accessibilityNetwork,
            IsochroneArguments isochroneArguments) {

        EncodingManager encodingManager = accessibilityNetwork.getNetworkData().getGraphHopperNetwork().network().getEncodingManager();
        IsochroneMatchMapper isochroneMatchMapper = isochroneMatchMapperFactory.create(encodingManager);

        QueryGraph queryGraph = accessibilityNetwork.getQueryGraph();
        var isochroneByTimeDistanceAndWeight = IsochroneShortestPathTreeFactory.createIsochroneByTimeDistanceAndWeight(
                queryGraph,
                isochroneArguments);

        List<IsoLabel> isoLabels = new ArrayList<>();
        isochroneByTimeDistanceAndWeight.search(accessibilityNetwork.getFrom().getClosestNode(), isoLabels::add);

        return isoLabels.stream()
                .filter(IsochroneFilter::isNotRoot)
                .filter(isoLabel -> IsochroneFilter.isWithinMunicipality(encodingManager, queryGraph, isoLabel, isochroneArguments))
                .filter(isoLabel -> IsochroneFilter.isWithinBoundingBox(queryGraph, isoLabel, isochroneArguments))
                .map(isoLabel -> isochroneMatchMapper.mapToIsochroneMatch(
                        isoLabel,
                        Double.POSITIVE_INFINITY,
                        queryGraph,
                        accessibilityNetwork.getFrom().getClosestEdge(),
                        false))
                .toList();
    }
}
