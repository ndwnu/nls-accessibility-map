package nu.ndw.nls.accessibilitymap.shared.accessibility.graphhopper.factory;

import static nu.ndw.nls.accessibilitymap.shared.model.NetworkConstants.PROFILE;

import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import com.graphhopper.storage.index.LocationIndexTree;
import com.graphhopper.util.PMap;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.shared.accessibility.graphhopper.IsochroneService;
import nu.ndw.nls.geometry.distance.FractionAndDistanceCalculator;
import nu.ndw.nls.routingmapmatcher.isochrone.algorithm.ShortestPathTreeFactory;
import nu.ndw.nls.routingmapmatcher.isochrone.mappers.IsochroneMatchMapper;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import nu.ndw.nls.routingmapmatcher.util.PointListUtil;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IsochroneServiceFactory {

    private final EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor;

    private final FractionAndDistanceCalculator fractionAndDistanceCalculator;

    public IsochroneService createService(NetworkGraphHopper network) {
        LocationIndexTree locationIndexTree = network.getLocationIndex();
        BaseGraph baseGraph = network.getBaseGraph();
        EncodingManager encodingManager = network.getEncodingManager();

        IsochroneMatchMapper isochroneMatchMapper = new IsochroneMatchMapper(encodingManager,
                edgeIteratorStateReverseExtractor, new PointListUtil(), fractionAndDistanceCalculator);
        Weighting weighting = network.createWeighting(PROFILE, new PMap());
        ShortestPathTreeFactory shortestPathTreeFactory = new ShortestPathTreeFactory(weighting);
        return new IsochroneService(encodingManager, baseGraph, isochroneMatchMapper, shortestPathTreeFactory,
                locationIndexTree);
    }

}
