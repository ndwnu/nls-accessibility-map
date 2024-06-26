package nu.ndw.nls.accessibilitymap.backend.graphhopper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.storage.index.LocationIndexTree;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.EdgeIteratorState;
import java.lang.reflect.Constructor;
import java.util.function.Consumer;
import lombok.SneakyThrows;
import nu.ndw.nls.routingmapmatcher.isochrone.algorithm.IsoLabel;
import nu.ndw.nls.routingmapmatcher.isochrone.algorithm.IsochroneByTimeDistanceAndWeight;
import nu.ndw.nls.routingmapmatcher.isochrone.algorithm.ShortestPathTreeFactory;
import nu.ndw.nls.routingmapmatcher.isochrone.mappers.IsochroneMatchMapper;
import nu.ndw.nls.routingmapmatcher.model.IsochroneMatch;
import nu.ndw.nls.routingmapmatcher.model.IsochroneUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IsochroneServiceTest {

    private static final int ROOT_ID = -1;
    private static final double Y_COORDINATE = 1D;
    private static final double X_COORDINATE = 0D;
    private static final double ISOCHRONE_VALUE_METERS = 200D;
    private static final int START_NODE_ID = 1;
    private static final int MUNICIPALITY_ID = 1;
    private static final String MUNICIPALITY_CODE_KEY = "municipality_code";

    @Mock
    private EncodingManager encodingManager;
    @Mock
    private BaseGraph baseGraph;
    @Mock
    private IsochroneMatchMapper isochroneMatchMapper;
    @Mock
    private ShortestPathTreeFactory shortestPathTreeFactory;
    @Mock
    private LocationIndexTree locationIndexTree;

    @Mock
    private QueryGraph queryGraph;
    @Mock
    private Point point;
    @Mock
    private Snap startSegment;
    @Mock
    private IsochroneByTimeDistanceAndWeight isochroneAlgorithm;
    @Mock
    private EdgeIteratorState currentEdge;
    @Mock
    private IntEncodedValue intEncodedValue;
    @Mock
    private Weighting weighting;

    @InjectMocks
    private IsochroneService isochroneService;

    @Test
    void getIsochroneMatchesByMunicipalityId_ok() {
        IsoLabel isoLabel = createIsoLabel();
        setupFixture();
        when(shortestPathTreeFactory.createShortestPathTreeByTimeDistanceAndWeight(
                any(Weighting.class),
                any(QueryGraph.class),
                eq(TraversalMode.EDGE_BASED),
                anyDouble(),
                eq(IsochroneUnit.METERS),
                eq(false)))
                .thenReturn(isochroneAlgorithm);

        when(queryGraph.getEdgeIteratorState(anyInt(), anyInt()))
                .thenReturn(currentEdge);
        when(encodingManager.getIntEncodedValue(MUNICIPALITY_CODE_KEY))
                .thenReturn(intEncodedValue);
        when(currentEdge.get(intEncodedValue)).thenReturn(MUNICIPALITY_ID);
        doAnswer(ans -> {
            Consumer<IsoLabel> callback = ans.getArgument(1, Consumer.class);
            callback.accept(isoLabel);
            return null;
        }).when(isochroneAlgorithm).search(eq(START_NODE_ID), any());
        when(isochroneMatchMapper.mapToIsochroneMatch(isoLabel, Double.POSITIVE_INFINITY, queryGraph,
                startSegment.getClosestEdge())).thenReturn(IsochroneMatch.builder().build());
        wrapWithStaticMock(() -> isochroneService.getIsochroneMatchesByMunicipalityId(weighting, point, MUNICIPALITY_ID,
                ISOCHRONE_VALUE_METERS));
        verify(shortestPathTreeFactory).createShortestPathTreeByTimeDistanceAndWeight(weighting, queryGraph,
                TraversalMode.EDGE_BASED, ISOCHRONE_VALUE_METERS, IsochroneUnit.METERS, false);
    }

    private void wrapWithStaticMock(Runnable function) {
        try (MockedStatic<QueryGraph> queryGraphStaticMock = Mockito.mockStatic(QueryGraph.class)) {
            queryGraphStaticMock.when(() -> QueryGraph.create(eq(baseGraph), any(Snap.class))).thenReturn(queryGraph);
            function.run();
        }
    }

    private void setupFixture() {
        when(point.getY()).thenReturn(Y_COORDINATE);
        when(point.getX()).thenReturn(X_COORDINATE);
        when(locationIndexTree.findClosest(Y_COORDINATE, X_COORDINATE, EdgeFilter.ALL_EDGES)).thenReturn(startSegment);
        when(startSegment.getClosestNode()).thenReturn(START_NODE_ID);
    }

    @SneakyThrows
    private static IsoLabel createIsoLabel() {
        int edgeId = 1;
        int adjNode = 2;
        double weight = 0;
        Constructor<IsoLabel> constructor = IsoLabel.class.getDeclaredConstructor(
                int.class,
                int.class,
                double.class,
                long.class,
                double.class,
                IsoLabel.class);
        constructor.setAccessible(true);
        IsoLabel parent = constructor.newInstance(ROOT_ID, ROOT_ID, weight, 0, 0, null);
        return constructor.newInstance(edgeId, adjNode, weight, (long) 0, (double) 100, parent);
    }
}
