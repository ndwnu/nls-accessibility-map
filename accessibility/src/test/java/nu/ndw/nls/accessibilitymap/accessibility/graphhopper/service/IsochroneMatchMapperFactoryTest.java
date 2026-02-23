package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.util.ReflectionTestUtils.getField;

import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import nu.ndw.nls.geometry.distance.FractionAndDistanceCalculator;
import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
import nu.ndw.nls.routingmapmatcher.isochrone.mappers.IsochroneMatchMapper;
import nu.ndw.nls.routingmapmatcher.util.PointListUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IsochroneMatchMapperFactoryTest {

    private IsochroneMatchMapperFactory isochroneMatchMapperFactory;

    @Mock
    private EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor;

    @Mock
    private FractionAndDistanceCalculator fractionAndDistanceCalculator;

    @Mock
    private GeometryFactoryWgs84 geometryFactory;

    @Mock
    private EncodingManager encodingManager;

    @BeforeEach
    void setUp() {

        isochroneMatchMapperFactory = new IsochroneMatchMapperFactory(
                edgeIteratorStateReverseExtractor,
                fractionAndDistanceCalculator,
                geometryFactory);
    }

    @Test
    void create() {
        IsochroneMatchMapper isochroneMatchMapper = isochroneMatchMapperFactory.create(encodingManager);

        // I know this is a bad test, but it is the only thing I can do considering the poor design of IsochroneMatchMapper class
        // and Routing Map Matcher library in general.
        assertThat(isochroneMatchMapper).isNotNull();

        assertThat(getField(isochroneMatchMapper, "encodingManager")).isSameAs(encodingManager);
        assertThat(getField(isochroneMatchMapper, "edgeIteratorStateReverseExtractor")).isSameAs(edgeIteratorStateReverseExtractor);
        assertThat(getField(isochroneMatchMapper, "fractionAndDistanceCalculator")).isSameAs(fractionAndDistanceCalculator);

        PointListUtil pointListUtil = (PointListUtil) getField(isochroneMatchMapper, "pointListUtil");
        assertThat(pointListUtil).isNotNull();
        assertThat(getField(pointListUtil, "geometryFactory")).isSameAs(geometryFactory);
    }
}
