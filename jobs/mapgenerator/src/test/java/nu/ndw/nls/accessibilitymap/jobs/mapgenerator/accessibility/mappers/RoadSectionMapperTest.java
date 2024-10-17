package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.EdgeIteratorState;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink;
import nu.ndw.nls.routingmapmatcher.model.IsochroneMatch;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.locationtech.jts.geom.LineString;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoadSectionMapperTest {

    private RoadSectionMapper roadSectionMapper;

    @Mock
    private NetworkGraphHopper networkGraphHopper;

    @Mock
    private EncodingManager encodingManager;

    @Mock
    private IntEncodedValue intEncodedValue;

    @Mock
    private IsochroneMatch isochroneMatch;

    @Mock
    private EdgeIteratorState edgeIteratorState;

    @Mock
    private TrafficSign trafficSignForward;
    @Mock
    private TrafficSign trafficSignBackward;

    @Mock
    private LineString geometry;

    @BeforeEach
    void setUp() {

        roadSectionMapper = new RoadSectionMapper(networkGraphHopper);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            true,
            false
            """)
    void mapToRoadSections(boolean isReversed) {

        List<IsochroneMatch> isochroneMatches = List.of(isochroneMatch);
        Map<Integer, TrafficSign> trafficSignById = Map.of(
                10, trafficSignForward,
                20, trafficSignBackward
        );

        when(networkGraphHopper.getEncodingManager()).thenReturn(encodingManager);
        when(encodingManager.getIntEncodedValue(AccessibilityLink.TRAFFIC_SIGN_ID)).thenReturn(intEncodedValue);
        when(isochroneMatch.getMatchedLinkId()).thenReturn(1);
        when(isochroneMatch.getEdge()).thenReturn(edgeIteratorState);
        when(edgeIteratorState.getEdge()).thenReturn(2);
        when(edgeIteratorState.getEdgeKey()).thenReturn(3);

        if (isReversed) {
            when(edgeIteratorState.getReverse(intEncodedValue)).thenReturn(20);
        } else {
            when(edgeIteratorState.get(intEncodedValue)).thenReturn(10);
        }
        when(isochroneMatch.isReversed()).thenReturn(isReversed);
        when(isochroneMatch.getGeometry()).thenReturn(geometry);

        Collection<RoadSection> roadSections = roadSectionMapper.mapToRoadSections(isochroneMatches, trafficSignById);

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
            validateSegments(roadSectionFragment.getBackwardSegment(), roadSectionFragment, Direction.BACKWARD, trafficSignBackward);
            assertThat(roadSectionFragment.getForwardSegment()).isNull();
        } else {
            validateSegments(roadSectionFragment.getForwardSegment(), roadSectionFragment, Direction.FORWARD, trafficSignForward);
            assertThat(roadSectionFragment.getBackwardSegment()).isNull();
        }
    }

    private void validateSegments(
            DirectionalSegment segment,
            RoadSectionFragment roadSectionFragment,
            Direction direction,
            TrafficSign trafficSign) {

        assertThat(segment.getId()).isEqualTo(3);
        assertThat(segment.getDirection()).isEqualTo(direction);
        assertThat(segment.getTrafficSign()).isEqualTo(trafficSign);
        assertThat(segment.getRoadSectionFragment()).isEqualTo(roadSectionFragment);
        assertThat(segment.isAccessible()).isTrue();
        assertThat(segment.getLineString()).isEqualTo(geometry);
    }
}