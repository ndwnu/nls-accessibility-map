package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers.EffectiveAccessibleDirectionalRoadSectionFragmentMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers.EffectiveAccessibleDirectionalRoadSectionsMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSectionAndTrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSectionAndTrafficSignGroupedById;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.EffectiveAccessibleDirectionalRoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.EffectiveAccessibleDirectionalRoadSections;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.roadsectionfraction.model.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.roadsectionfraction.services.RoadSectionFragmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EffectiveAccessibilityServiceTest {

    @Mock
    private EffectiveAccessibleDirectionalRoadSectionFragmentMapper
            effectiveAccessibleDirectionalRoadSectionFragmentMapper;

    @Mock
    private RoadSectionFragmentService roadSectionFragmentService;

    @Mock
    private EffectiveAccessibleDirectionalRoadSectionsMapper effectiveAccessibleDirectionalRoadSectionsMapper;

    @Mock
    private RoadSectionFragmentSplitService roadSectionFragmentSplitService;

    @InjectMocks
    private EffectiveAccessibilityService effectiveAccessibilityService;

    @Mock
    private DirectionalRoadSectionAndTrafficSignGroupedById directionalRoadSectionAndTrafficSignGroupedById;

    @Mock
    private DirectionalRoadSectionAndTrafficSign directionalRoadSectionAndTrafficSignBackward;

    @Mock
    private DirectionalRoadSectionAndTrafficSign directionalRoadSectionAndTrafficSignForward;

    @Mock
    private List<RoadSectionFragment<EffectiveAccessibleDirectionalRoadSection>>
            effectiveAccessibleDirectionalRoadSectionsBackward;

    @Mock
    private List<RoadSectionFragment<EffectiveAccessibleDirectionalRoadSection>>
            effectiveAccessibleDirectionalRoadSectionsForward;

    @Mock
    private List<RoadSectionFragment<EffectiveAccessibleDirectionalRoadSections>>
            splitAndCombinedDataResult;

    @Mock
    private List<RoadSectionFragment<EffectiveAccessibleDirectionalRoadSections>> combineSequentialsResult;

    @Captor
    private ArgumentCaptor<BinaryOperator<EffectiveAccessibleDirectionalRoadSections>> combineSequentialCombinerCaptor;

    @Captor
    private ArgumentCaptor<BiPredicate<EffectiveAccessibleDirectionalRoadSections,
            EffectiveAccessibleDirectionalRoadSections>> combiningPossibleCaptor;

    @Mock
    private RoadSectionFragment<EffectiveAccessibleDirectionalRoadSections> effectiveAccessibleRoadSectionsFragmentA;

    @Mock
    private RoadSectionFragment<EffectiveAccessibleDirectionalRoadSections> effectiveAccessibleRoadSectionsFragmentB;

    @Mock
    private DirectionalRoadSectionAndTrafficSign directionalRoadSectionAndTrafficSignA;

    @Mock
    private DirectionalRoadSectionAndTrafficSign directionalRoadSectionAndTrafficSignB;

    @Mock
    private EffectiveAccessibleDirectionalRoadSections effectiveAccessibleDirectionalRoadSectionsA;
    private EffectiveAccessibleDirectionalRoadSections effectiveAccessibleDirectionalRoadSectionsB;

    @Test
    void determineEffectiveAccessibility_ok() {

        when(directionalRoadSectionAndTrafficSignGroupedById.getBackward())
                .thenReturn(directionalRoadSectionAndTrafficSignBackward);
        when(directionalRoadSectionAndTrafficSignGroupedById.getForward())
                .thenReturn(directionalRoadSectionAndTrafficSignForward);

        when(effectiveAccessibleDirectionalRoadSectionFragmentMapper.map(directionalRoadSectionAndTrafficSignBackward))
                .thenReturn(effectiveAccessibleDirectionalRoadSectionsBackward);
        when(effectiveAccessibleDirectionalRoadSectionFragmentMapper.map(directionalRoadSectionAndTrafficSignForward))
                .thenReturn(effectiveAccessibleDirectionalRoadSectionsForward);

        when(roadSectionFragmentService.splitAndCombineData(effectiveAccessibleDirectionalRoadSectionsBackward,
                effectiveAccessibleDirectionalRoadSectionsForward, effectiveAccessibleDirectionalRoadSectionsMapper))
                .thenReturn(splitAndCombinedDataResult);

        when(roadSectionFragmentService.combineSequentials(eq(splitAndCombinedDataResult), combiningPossibleCaptor.capture(),
                combineSequentialCombinerCaptor.capture()))
                .thenReturn(combineSequentialsResult);

        when(combineSequentialsResult.stream()).thenReturn(Stream.of(effectiveAccessibleRoadSectionsFragmentA,
                effectiveAccessibleRoadSectionsFragmentB));

        when(roadSectionFragmentSplitService.split(effectiveAccessibleRoadSectionsFragmentA))
                .thenReturn(List.of(directionalRoadSectionAndTrafficSignA));

        when(roadSectionFragmentSplitService.split(effectiveAccessibleRoadSectionsFragmentB))
                .thenReturn(List.of(directionalRoadSectionAndTrafficSignB));

        assertThat(effectiveAccessibilityService.determineEffectiveAccessibility(
                directionalRoadSectionAndTrafficSignGroupedById))
                    .isEqualTo(List.of(directionalRoadSectionAndTrafficSignA, directionalRoadSectionAndTrafficSignB ));


        // combiningPossible should be implemented as Objects::equals
        BiPredicate<EffectiveAccessibleDirectionalRoadSections, EffectiveAccessibleDirectionalRoadSections>
                combiningPossible = combiningPossibleCaptor.getValue();

        // Two quick checks that verify that the lambda is indeed checking for equality
        assertThat(combiningPossible.test(EffectiveAccessibleDirectionalRoadSections.builder().build(),
                EffectiveAccessibleDirectionalRoadSections.builder().build())).isTrue();
        assertThat(combiningPossible.test(
                EffectiveAccessibleDirectionalRoadSections.builder().accessibility(true).build(),
                EffectiveAccessibleDirectionalRoadSections.builder().accessibility(false).build())).isFalse();

        // combineSequentialCombinerCaptor should pick the left data, which should be equal as stated by the
        // combiningPossible that is implemented with equality. However, to verify that it always picks the first
        // argument we feed it two different options and verify that it always returns the first argument
        EffectiveAccessibleDirectionalRoadSections a = EffectiveAccessibleDirectionalRoadSections.builder()
                .accessibility(true)
                .build();
        EffectiveAccessibleDirectionalRoadSections b = EffectiveAccessibleDirectionalRoadSections.builder()
                .accessibility(false)
                .build();

        var combineSequential = combineSequentialCombinerCaptor.getValue();
        assertThat(combineSequential.apply(a, b)).isEqualTo(a);
        assertThat(combineSequential.apply(b, a)).isEqualTo(b);
    }
}