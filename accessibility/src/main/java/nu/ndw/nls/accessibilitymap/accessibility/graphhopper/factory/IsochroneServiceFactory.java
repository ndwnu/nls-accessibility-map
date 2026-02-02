package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.factory;

import static nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants.CAR_PROFILE;

import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import com.graphhopper.util.PMap;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service.IsochroneService;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityNetwork;
import nu.ndw.nls.geometry.distance.FractionAndDistanceCalculator;
import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
import nu.ndw.nls.routingmapmatcher.isochrone.algorithm.ShortestPathTreeFactory;
import nu.ndw.nls.routingmapmatcher.isochrone.mappers.IsochroneMatchMapper;
import nu.ndw.nls.routingmapmatcher.util.PointListUtil;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IsochroneServiceFactory {

    private final EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor;

    private final FractionAndDistanceCalculator fractionAndDistanceCalculator;

    private final GeometryFactoryWgs84 geometryFactory;

    public IsochroneService createService(AccessibilityNetwork accessibilityNetwork) {

        var network = accessibilityNetwork.getAccessibilityContext().graphHopperNetwork().network();
        var encodingManager = network.getEncodingManager();

        IsochroneMatchMapper isochroneMatchMapper = new IsochroneMatchMapper(
                encodingManager,
                edgeIteratorStateReverseExtractor,
                new PointListUtil(geometryFactory),
                fractionAndDistanceCalculator);

        Weighting weighting = network.createWeighting(CAR_PROFILE, new PMap());
        ShortestPathTreeFactory shortestPathTreeFactory = new ShortestPathTreeFactory(weighting, encodingManager);

        return new IsochroneService(encodingManager, isochroneMatchMapper, shortestPathTreeFactory);
    }
}
