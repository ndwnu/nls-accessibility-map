package nu.ndw.nls.accessibilitymap.accessibility.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import com.graphhopper.util.EdgeIteratorState;
import io.micrometer.core.annotation.Timed;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.GraphHopperNetwork;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.mapper.isochone.IsoLabelToGeometryMapper;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.mapper.isochone.IsoLabelToRoadSectionIdMapper;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityNetwork;
import nu.ndw.nls.routingmapmatcher.isochrone.algorithm.IsoLabel;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.locationtech.jts.geom.LineString;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoadSectionMapperTest {

    private RoadSectionMapper roadSectionMapper;

    @Mock
    private IsoLabelToRoadSectionIdMapper isoLabelToRoadSectionIdMapper;

    @Mock
    private IsoLabelToGeometryMapper isoLabelToGeometryMapper;

    @Mock
    private EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor;

    @Mock
    private AccessibilityNetwork accessibilityNetwork;

    @Mock
    private IsoLabel isoLabel;

    @Mock
    private EdgeIteratorState edgeIteratorState;

    @Mock
    private LineString geometry;

    @Mock
    private Restriction restriction;

    @Mock
    private QueryGraph queryGraph;

    @Mock
    private NetworkData networkData;

    @Mock
    private GraphHopperNetwork graphHopperNetwork;

    @Mock
    private NetworkGraphHopper networkGraphHopper;

    @Mock
    private EncodingManager encodingManager;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @BeforeEach
    void setUp() {
        roadSectionMapper = new RoadSectionMapper(
                isoLabelToRoadSectionIdMapper,
                isoLabelToGeometryMapper,
                edgeIteratorStateReverseExtractor);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            true,
            false,
            """)
    void map(boolean isReversed) {

        List<IsoLabel> isochroneMatches = List.of(isoLabel);

        when(isoLabel.getEdge()).thenReturn(2);
        when(isoLabel.getNode()).thenReturn(1);
        when(accessibilityNetwork.getQueryGraph()).thenReturn(queryGraph);
        when(queryGraph.getEdgeIteratorState(2, 1)).thenReturn(edgeIteratorState);
        when(accessibilityNetwork.getNetworkData()).thenReturn(networkData);
        when(networkData.getGraphHopperNetwork()).thenReturn(graphHopperNetwork);
        when(graphHopperNetwork.network()).thenReturn(networkGraphHopper);
        when(networkGraphHopper.getEncodingManager()).thenReturn(encodingManager);

        when(edgeIteratorStateReverseExtractor.hasReversed(edgeIteratorState)).thenReturn(isReversed);
        when(isoLabelToGeometryMapper.map(edgeIteratorState)).thenReturn(geometry);
        when(isoLabelToRoadSectionIdMapper.map(edgeIteratorState, encodingManager, false)).thenReturn(1);

        when(edgeIteratorState.getEdge()).thenReturn(2);
        when(edgeIteratorState.getEdgeKey()).thenReturn(3);

        Map<Integer, List<Restriction>> restrictionsByEdgeKey = new HashMap<>();
        restrictionsByEdgeKey.put(3, List.of(restriction));
        Collection<RoadSection> roadSections = roadSectionMapper.map(
                accessibilityNetwork,
                isochroneMatches,
                restrictionsByEdgeKey);

        assertThat(roadSections)
                .isNotEmpty()
                .hasSize(1);

        RoadSection roadSection = roadSections.iterator().next();
        assertThat(roadSection.getId()).isEqualTo(1);

        assertThat(roadSection.getRoadSectionFragments()).hasSize(1);
        RoadSectionFragment roadSectionFragment = roadSection.getRoadSectionFragments().getFirst();

        assertThat(roadSectionFragment.getId()).isEqualTo(2);
        assertThat(roadSectionFragment.getRoadSection()).isEqualTo(roadSection);

        if (isReversed) {
            validateSegments(roadSectionFragment.getBackwardSegment(), roadSectionFragment, Direction.BACKWARD);
            assertThat(roadSectionFragment.getForwardSegment()).isNull();
        } else {
            validateSegments(roadSectionFragment.getForwardSegment(), roadSectionFragment, Direction.FORWARD);
            assertThat(roadSectionFragment.getBackwardSegment()).isNull();
        }

        loggerExtension.containsLog(Level.DEBUG, "Mapping iso labels to road sections");
        loggerExtension.containsLog(Level.DEBUG, "Mapped 1 iso labels to 1 road sections");

        // Done because this needs to be a modifiable collection.
        assertThat(roadSections.add(mock(RoadSection.class))).isTrue();
    }

    @SuppressWarnings("unchecked")
    private void validateSegments(DirectionalSegment segment, RoadSectionFragment roadSectionFragment, Direction direction) {

        assertThat(segment.getId()).isEqualTo(3);
        assertThat(segment.getDirection()).isEqualTo(direction);
        assertThat(segment.getRoadSectionFragment()).isEqualTo(roadSectionFragment);
        assertThat(segment.isAccessible()).isTrue();
        assertThat(segment.getLineString()).isEqualTo(geometry);
        assertThat(segment.getRestrictions()).isEqualTo(new Restrictions(List.of(restriction)));
    }

    @Test
    void map_containsTimeAnnotation() {

        AnnotationUtil.methodContainsAnnotation(
                roadSectionMapper.getClass(),
                Timed.class,
                "map",
                annotation -> {
                    assertThat(annotation).isNotNull();
                    assertThat(annotation.value()).isEqualTo("accessibilitymap.roadSection.mapping");
                }
        );
    }
}
