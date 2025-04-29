package nu.ndw.nls.accessibilitymap.accessibility.services;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoadSectionTrafficSignAssignerTest {

    private static final int SEGMENT_ID = 1;
    private RoadSectionTrafficSignAssigner assigner;
    @Mock
    private RoadSection roadSection;
    @Mock
    private DirectionalSegment segment;
    @Mock
    private RoadSectionFragment fragment;
    @Mock
    private TrafficSign trafficSign;

    private Map<Integer, List<TrafficSign>> trafficSignsByEdgeKey;

    @BeforeEach
    void setUp() {
        assigner = new RoadSectionTrafficSignAssigner();
        trafficSignsByEdgeKey = new HashMap<>();
    }

    @Test
    void assignTrafficSigns_WhenTrafficSignsExistForSegments() {

        trafficSignsByEdgeKey.put(SEGMENT_ID, List.of(trafficSign));
        when(fragment.getSegments()).thenReturn(List.of(segment));
        when(roadSection.getRoadSectionFragments()).thenReturn(List.of(fragment));
        when(segment.getId()).thenReturn(SEGMENT_ID);
        assigner.assignTrafficSigns(roadSection, trafficSignsByEdgeKey);

        verify(segment).setTrafficSigns(List.of(trafficSign));

    }

    @Test
    void assignTrafficSigns_WhenNoTrafficSignsExistForSegments() {

        trafficSignsByEdgeKey.put(SEGMENT_ID, List.of(trafficSign));
        when(fragment.getSegments()).thenReturn(List.of(segment));
        when(roadSection.getRoadSectionFragments()).thenReturn(List.of(fragment));
        when(segment.getId()).thenReturn(2);

        assigner.assignTrafficSigns(roadSection, trafficSignsByEdgeKey);

        verify(segment).setTrafficSigns(null);
    }
}
