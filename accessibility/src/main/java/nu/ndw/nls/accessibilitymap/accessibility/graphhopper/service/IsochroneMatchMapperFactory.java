package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service;

import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import lombok.AllArgsConstructor;
import nu.ndw.nls.geometry.distance.FractionAndDistanceCalculator;
import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
import nu.ndw.nls.routingmapmatcher.isochrone.mappers.IsochroneMatchMapper;
import nu.ndw.nls.routingmapmatcher.util.PointListUtil;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class IsochroneMatchMapperFactory {

    private final EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor;

    private final FractionAndDistanceCalculator fractionAndDistanceCalculator;

    private final GeometryFactoryWgs84 geometryFactory;

    public IsochroneMatchMapper create(EncodingManager encodingManager) {

        return new IsochroneMatchMapper(
                encodingManager,
                edgeIteratorStateReverseExtractor,
                new PointListUtil(geometryFactory),
                fractionAndDistanceCalculator);
    }
}
