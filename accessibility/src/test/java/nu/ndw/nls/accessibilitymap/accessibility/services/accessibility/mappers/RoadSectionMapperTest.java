package nu.ndw.nls.accessibilitymap.accessibility.services.accessibility.mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.graphhopper.util.EdgeIteratorState;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.routingmapmatcher.model.IsochroneMatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.locationtech.jts.geom.LineString;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoadSectionMapperTest {

    private static final int ROAD_SECTION_ID = 1;
    private static final int EDGE_ID = 2;
    private static final int EDGE_KEY = 3;
    private RoadSectionMapper roadSectionMapper;


    @Mock
    private IsochroneMatch isochroneMatch;

    @Mock
    private EdgeIteratorState edgeIteratorState;

    @Mock
    private TrafficSign trafficSignForward;
    @Mock
    private TrafficSign trafficSignBackward;

    @Mock
    private Map<Integer, List<TrafficSign>> trafficSignsByEdgeKey;

    @Mock
    private LineString geometry;

    @BeforeEach
    void setUp() {
        roadSectionMapper = new RoadSectionMapper();
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            true,
            false
            """)
    void mapToRoadSections(boolean isReversed) {

        List<IsochroneMatch> isochroneMatches = List.of(isochroneMatch);

        when(isochroneMatch.getMatchedLinkId()).thenReturn(ROAD_SECTION_ID);
        when(isochroneMatch.getEdge()).thenReturn(edgeIteratorState);
        when(edgeIteratorState.getEdge()).thenReturn(EDGE_ID);
        when(edgeIteratorState.getEdgeKey()).thenReturn(EDGE_KEY);

        when(isochroneMatch.isReversed()).thenReturn(isReversed);
        when(isochroneMatch.getGeometry()).thenReturn(geometry);
        when(trafficSignsByEdgeKey.containsKey(EDGE_KEY)).thenReturn(true);

        if (isReversed) {
            when(trafficSignsByEdgeKey.get(EDGE_KEY)).thenReturn(List.of(trafficSignBackward));
        } else {
            when(trafficSignsByEdgeKey.get(EDGE_KEY)).thenReturn(List.of(trafficSignForward));
        }
        Collection<RoadSection> roadSections = roadSectionMapper.mapToRoadSections(isochroneMatches, trafficSignsByEdgeKey);

        assertThat(roadSections)
                .isNotEmpty()
                .hasSize(1);

        RoadSection roadSection = roadSections.iterator().next();
        assertThat(roadSection.getId()).isEqualTo(ROAD_SECTION_ID);

        assertThat(roadSection.getRoadSectionFragments()).hasSize(1);
        RoadSectionFragment roadSectionFragment = roadSection.getRoadSectionFragments().getFirst();

        assertThat(roadSectionFragment.getId()).isEqualTo(EDGE_ID);
        assertThat(roadSectionFragment.getRoadSection()).isEqualTo(roadSection);

        if (isReversed) {
            validateSegments(roadSectionFragment.getBackwardSegment(), roadSectionFragment, Direction.BACKWARD, trafficSignBackward);
            assertThat(roadSectionFragment.getForwardSegment()).isNull();
        } else {
            validateSegments(roadSectionFragment.getForwardSegment(), roadSectionFragment, Direction.FORWARD, trafficSignForward);
            assertThat(roadSectionFragment.getBackwardSegment()).isNull();
        }
    }

    @SuppressWarnings("unchecked")
    private void validateSegments(
            DirectionalSegment segment,
            RoadSectionFragment roadSectionFragment,
            Direction direction,
            TrafficSign trafficSign) {

        assertThat(segment.getId()).isEqualTo(EDGE_KEY);
        assertThat(segment.getDirection()).isEqualTo(direction);
        assertThat(segment.getTrafficSigns()).isEqualTo(List.of(trafficSign));
        assertThat(segment.getRoadSectionFragment()).isEqualTo(roadSectionFragment);
        assertThat(segment.isAccessible()).isTrue();
        assertThat(segment.getLineString()).isEqualTo(geometry);
    }
}
