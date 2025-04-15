package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.factory;

import static nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants.PROFILE;

import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import com.graphhopper.util.PMap;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service.IsochroneService;
import nu.ndw.nls.geometry.distance.FractionAndDistanceCalculator;
import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
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

    private final GeometryFactoryWgs84 geometryFactory;

    public IsochroneService createService(NetworkGraphHopper network) {

        EncodingManager encodingManager = network.getEncodingManager();

        IsochroneMatchMapper isochroneMatchMapper = new IsochroneMatchMapper(
                encodingManager,
                edgeIteratorStateReverseExtractor,
                new PointListUtil(geometryFactory),
                fractionAndDistanceCalculator);

        Weighting weighting = network.createWeighting(PROFILE, new PMap());
        ShortestPathTreeFactory shortestPathTreeFactory = new ShortestPathTreeFactory(weighting, encodingManager);

        return new IsochroneService(encodingManager, isochroneMatchMapper, shortestPathTreeFactory);
    }

}
