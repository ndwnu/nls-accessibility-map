package nu.ndw.nls.accessibilitymap.accessibility.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoadSectionTrafficSignAssignerTest {

    private RoadSectionTrafficSignAssigner assigner;

    @Mock
    private RoadSection roadSection;

    @Mock
    private DirectionalSegment segment;

    @Mock
    private RoadSectionFragment fragment;

    @Mock
    private Restriction restriction;

    private Map<Integer, List<Restriction>> trafficSignsByEdgeKey;

    @BeforeEach
    void setUp() {
        assigner = new RoadSectionTrafficSignAssigner();
        trafficSignsByEdgeKey = new HashMap<>();
    }

    @Test
    void assignTrafficSigns_WhenRestrictionExistForSegments() {

        trafficSignsByEdgeKey.put(1, List.of(restriction));
        when(fragment.getSegments()).thenReturn(List.of(segment));
        when(roadSection.getRoadSectionFragments()).thenReturn(List.of(fragment));
        when(segment.getId()).thenReturn(1);
        assigner.assignRestriction(roadSection, trafficSignsByEdgeKey);

        verify(segment).setRestrictions(List.of(restriction));
    }

    @Test
    void assignTrafficSigns_WhenNoRestrictionExistForSegments() {

        trafficSignsByEdgeKey.put(1, List.of(restriction));
        when(fragment.getSegments()).thenReturn(List.of(segment));
        when(roadSection.getRoadSectionFragments()).thenReturn(List.of(fragment));
        when(segment.getId()).thenReturn(2);

        assigner.assignRestriction(roadSection, trafficSignsByEdgeKey);

        verify(segment).setRestrictions(null);
    }
}
