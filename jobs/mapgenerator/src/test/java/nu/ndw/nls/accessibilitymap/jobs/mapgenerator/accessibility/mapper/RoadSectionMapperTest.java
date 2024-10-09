package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.EdgeIteratorState;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.model.Direction;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.model.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.model.RoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.model.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.model.trafficsign.TrafficSign;
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
    private TrafficSign trafficSign;

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
                10, trafficSign
        );

        when(networkGraphHopper.getEncodingManager()).thenReturn(encodingManager);
        when(encodingManager.getIntEncodedValue(AccessibilityLink.TRAFFIC_SIGN_ID)).thenReturn(intEncodedValue);
        when(isochroneMatch.getMatchedLinkId()).thenReturn(1);
        when(isochroneMatch.getEdge()).thenReturn(edgeIteratorState);
        when(edgeIteratorState.getEdge()).thenReturn(2);
        when(edgeIteratorState.getEdgeKey()).thenReturn(3);
        when(edgeIteratorState.get(intEncodedValue)).thenReturn(10);
        when(isochroneMatch.isReversed()).thenReturn(isReversed);
        when(isochroneMatch.getGeometry()).thenReturn(geometry);

        Collection<RoadSection> roadSections = roadSectionMapper.mapToRoadSections(isochroneMatches, trafficSignById);

        assertThat(roadSections).isNotEmpty();
        assertThat(roadSections).hasSize(1);

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
    }

    private void validateSegments(DirectionalSegment segment, RoadSectionFragment roadSectionFragment, Direction direction) {

        assertThat(segment.getId()).isEqualTo(3);
        assertThat(segment.getDirection()).isEqualTo(direction);
        assertThat(segment.getTrafficSign()).isEqualTo(trafficSign);
        assertThat(segment.getRoadSectionFragment()).isEqualTo(roadSectionFragment);
        assertThat(segment.isAccessible()).isTrue();
        assertThat(segment.getLineString()).isEqualTo(geometry);
    }
}