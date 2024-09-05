package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSectionAndTrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.EffectiveAccessibleDirectionalRoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.EffectiveAccessibleDirectionalRoadSections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EffectiveAccessibleDirectionalRoadSectionsMapperTest {

    @Mock
    private AccessibilityMapper accessibilityMapper;

    @InjectMocks
    private EffectiveAccessibleDirectionalRoadSectionsMapper effectiveAccessibleDirectionalRoadSectionsMapper;

    @Mock
    private EffectiveAccessibleDirectionalRoadSection effectiveAccessibleDirectionalRoadSectionBackward;

    @Mock
    private EffectiveAccessibleDirectionalRoadSection effectiveAccessibleDirectionalRoadSectionForward;

    @Mock
    private DirectionalRoadSectionAndTrafficSign directionalRoadSectionAndTrafficSignBackward;

    @Mock
    private DirectionalRoadSectionAndTrafficSign directionalRoadSectionAndTrafficSignForward;

    @Test
    void map_ok_backwardOnlyAccessible() {
        when(effectiveAccessibleDirectionalRoadSectionBackward.getRoadSection())
                .thenReturn(directionalRoadSectionAndTrafficSignBackward);

        when(effectiveAccessibleDirectionalRoadSectionBackward.getAccessibility())
                .thenReturn(Boolean.TRUE);

        when(accessibilityMapper.map(Boolean.TRUE)).thenReturn(Boolean.TRUE);

        assertThat(effectiveAccessibleDirectionalRoadSectionsMapper.apply(
                effectiveAccessibleDirectionalRoadSectionBackward,
                null)).isEqualTo(
                        EffectiveAccessibleDirectionalRoadSections.builder()
                            .accessibility(true)
                            .backward(directionalRoadSectionAndTrafficSignBackward)
                            .forward(null)
                            .build());
    }

    @Test
    void map_ok_forwardOnlyAccessible() {
        when(effectiveAccessibleDirectionalRoadSectionForward.getRoadSection())
                .thenReturn(directionalRoadSectionAndTrafficSignForward);

        when(effectiveAccessibleDirectionalRoadSectionForward.getAccessibility())
                .thenReturn(Boolean.TRUE);

        when(accessibilityMapper.map(Boolean.TRUE)).thenReturn(Boolean.TRUE);

        assertThat(effectiveAccessibleDirectionalRoadSectionsMapper.apply(
                null,
                effectiveAccessibleDirectionalRoadSectionForward)).isEqualTo(
                EffectiveAccessibleDirectionalRoadSections.builder()
                        .accessibility(true)
                        .backward(null)
                        .forward(directionalRoadSectionAndTrafficSignForward)
                        .build());
    }

    @Test
    void map_ok_backwardsInaccessibleForwardAccessible() {
        when(effectiveAccessibleDirectionalRoadSectionBackward.getRoadSection())
                .thenReturn(directionalRoadSectionAndTrafficSignBackward);
        when(effectiveAccessibleDirectionalRoadSectionBackward.getAccessibility())
                .thenReturn(Boolean.FALSE);
        when(accessibilityMapper.map(Boolean.FALSE)).thenReturn(Boolean.FALSE);

        when(effectiveAccessibleDirectionalRoadSectionForward.getRoadSection())
                .thenReturn(directionalRoadSectionAndTrafficSignForward);
        when(effectiveAccessibleDirectionalRoadSectionForward.getAccessibility())
                .thenReturn(Boolean.TRUE);
        when(accessibilityMapper.map(Boolean.TRUE)).thenReturn(Boolean.TRUE);

        assertThat(effectiveAccessibleDirectionalRoadSectionsMapper.apply(
                effectiveAccessibleDirectionalRoadSectionBackward,
                effectiveAccessibleDirectionalRoadSectionForward)).isEqualTo(
                EffectiveAccessibleDirectionalRoadSections.builder()
                        .accessibility(true)
                        .backward(directionalRoadSectionAndTrafficSignBackward)
                        .forward(directionalRoadSectionAndTrafficSignForward)
                        .build());
    }

    @Test
    void map_ok_backwardsAccessibleForwardInaccessible() {
        when(effectiveAccessibleDirectionalRoadSectionBackward.getRoadSection())
                .thenReturn(directionalRoadSectionAndTrafficSignBackward);
        when(effectiveAccessibleDirectionalRoadSectionBackward.getAccessibility())
                .thenReturn(Boolean.TRUE);
        when(accessibilityMapper.map(Boolean.TRUE)).thenReturn(Boolean.TRUE);

        when(effectiveAccessibleDirectionalRoadSectionForward.getRoadSection())
                .thenReturn(directionalRoadSectionAndTrafficSignForward);

        assertThat(effectiveAccessibleDirectionalRoadSectionsMapper.apply(
                effectiveAccessibleDirectionalRoadSectionBackward,
                effectiveAccessibleDirectionalRoadSectionForward)).isEqualTo(
                EffectiveAccessibleDirectionalRoadSections.builder()
                        .accessibility(true)
                        .backward(directionalRoadSectionAndTrafficSignBackward)
                        .forward(directionalRoadSectionAndTrafficSignForward)
                        .build());
    }

    @Test
    void map_ok_backwardsInaccessibleForwardInaccessible() {
        when(effectiveAccessibleDirectionalRoadSectionBackward.getRoadSection())
                .thenReturn(directionalRoadSectionAndTrafficSignBackward);
        when(effectiveAccessibleDirectionalRoadSectionBackward.getAccessibility())
                .thenReturn(Boolean.FALSE);
        when(accessibilityMapper.map(Boolean.FALSE)).thenReturn(Boolean.FALSE);

        when(effectiveAccessibleDirectionalRoadSectionForward.getRoadSection())
                .thenReturn(directionalRoadSectionAndTrafficSignForward);
        when(effectiveAccessibleDirectionalRoadSectionForward.getAccessibility())
                .thenReturn(Boolean.FALSE);
        when(accessibilityMapper.map(Boolean.FALSE)).thenReturn(Boolean.FALSE);

        assertThat(effectiveAccessibleDirectionalRoadSectionsMapper.apply(
                effectiveAccessibleDirectionalRoadSectionBackward,
                effectiveAccessibleDirectionalRoadSectionForward)).isEqualTo(
                EffectiveAccessibleDirectionalRoadSections.builder()
                        .accessibility(false)
                        .backward(directionalRoadSectionAndTrafficSignBackward)
                        .forward(directionalRoadSectionAndTrafficSignForward)
                        .build());
    }
}