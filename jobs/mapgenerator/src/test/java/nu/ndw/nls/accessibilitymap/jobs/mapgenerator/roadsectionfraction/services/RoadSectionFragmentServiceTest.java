package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.roadsectionfraction.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.roadsectionfraction.model.RoadSectionFragment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoadSectionFragmentServiceTest {

    private static final double FRACTION_A = 0.2;
    private static final double FRACTION_B = 0.4;
    private static final int FRACTION_START_0 = 0;
    private static final int FRACTION_END_1 = 1;

    private static final RoadSectionFragment<Integer> ONE_FULL_ROAD_SECTION = RoadSectionFragment.<Integer>builder()
                    .fromFraction(FRACTION_START_0)
                    .toFraction(FRACTION_END_1)
                    .data(1)
                .build();

    private static final RoadSectionFragment<Integer> ROAD_SECTION_SPLIT_A_START = RoadSectionFragment.<Integer>builder()
            .fromFraction(FRACTION_START_0)
            .toFraction(FRACTION_A)
            .data(2)
            .build();

    private static final RoadSectionFragment<Integer> ROAD_SECTION_SPLIT_A_END = RoadSectionFragment.<Integer>builder()
            .fromFraction(FRACTION_A)
            .toFraction(FRACTION_END_1)
            .data(3)
            .build();

    private static final RoadSectionFragment<Integer> ROAD_SECTION_SPLIT_B_START = RoadSectionFragment.<Integer>builder()
            .fromFraction(FRACTION_START_0)
            .toFraction(FRACTION_B)
            .data(4)
            .build();

    private static final RoadSectionFragment<Integer> ROAD_SECTION_SPLIT_B_END = RoadSectionFragment.<Integer>builder()
            .fromFraction(FRACTION_B)
            .toFraction(FRACTION_END_1)
            .data(5)
            .build();

    private final BiFunction<Integer, Integer, Integer> combiner = (a, b) -> {
        if (a != null && b != null) {
            return a + b;
        } else if (a != null){
            return a;
        } else {
            return b;
        }
    };

    private final RoadSectionFragmentService roadSectionFragmentService = new RoadSectionFragmentService();

    @Mock
    private BiFunction<Integer, Integer, Integer> inputVerifyingCombiner;


    /**
     * Test splitAndCombine
     */
    @Test
    void splitAndCombine_both_null() {
        assertThat(roadSectionFragmentService.splitAndCombineData(null, null, combiner))
                .isEqualTo(Collections.emptyList());
    }

    @Test
    void splitAndCombine_bothEmpty() {
        assertThat(roadSectionFragmentService.splitAndCombineData(Collections.emptyList(),
                Collections.emptyList(), combiner))
                .isEqualTo(Collections.emptyList());
    }

    @Test
    void splitAndCombine_firstNullSecondEmpty() {
        assertThat(roadSectionFragmentService.splitAndCombineData(null, Collections.emptyList(), combiner))
                .isEqualTo(Collections.emptyList());
    }

    @Test
    void splitAndCombine_firstEmptySecondNull() {
        assertThat(roadSectionFragmentService.splitAndCombineData(Collections.emptyList(), null, combiner))
                .isEqualTo(Collections.emptyList());
    }

    @Test
    void splitAndCombine_firstFullSecondEmpty() {
        assertThat(roadSectionFragmentService.splitAndCombineData(List.of(ONE_FULL_ROAD_SECTION),
                Collections.emptyList(), combiner)).isEqualTo(List.of(ONE_FULL_ROAD_SECTION));
    }

    @Test
    void splitAndCombine_firstEmptySecondFull() {
        assertThat(roadSectionFragmentService.splitAndCombineData(Collections.emptyList(),
                List.of(ONE_FULL_ROAD_SECTION), combiner))
                .isEqualTo(List.of(ONE_FULL_ROAD_SECTION));
    }

    @Test
    void splitAndCombine_firstFullSecondFull() {
        assertThat(roadSectionFragmentService.splitAndCombineData(List.of(ONE_FULL_ROAD_SECTION),
                List.of(ONE_FULL_ROAD_SECTION), combiner))
                .isEqualTo(List.of(ONE_FULL_ROAD_SECTION
                        .withData(ONE_FULL_ROAD_SECTION.getData()+ONE_FULL_ROAD_SECTION.getData())));
    }

    @Test
    void splitAndCombine_firstSplitSecondEmpty() {
        assertThat(roadSectionFragmentService.splitAndCombineData(List.of(ROAD_SECTION_SPLIT_A_START,
                ROAD_SECTION_SPLIT_A_END), Collections.emptyList(), combiner))
                .isEqualTo(List.of(ROAD_SECTION_SPLIT_A_START, ROAD_SECTION_SPLIT_A_END));
    }

    @Test
    void splitAndCombine_firstEmptySecondSplit() {
        assertThat(roadSectionFragmentService.splitAndCombineData(Collections.emptyList(),
                List.of(ROAD_SECTION_SPLIT_A_START, ROAD_SECTION_SPLIT_A_END), combiner))
                .isEqualTo(List.of(ROAD_SECTION_SPLIT_A_START, ROAD_SECTION_SPLIT_A_END));
    }

    @Test
    void splitAndCombine_firstFullSecondSplit() {
        assertThat(roadSectionFragmentService.splitAndCombineData(List.of(ONE_FULL_ROAD_SECTION),
                List.of(ROAD_SECTION_SPLIT_A_START, ROAD_SECTION_SPLIT_A_END), combiner))
                .isEqualTo(List.of(
                        RoadSectionFragment.<Integer>builder()
                                .fromFraction(FRACTION_START_0)
                                .toFraction(FRACTION_A)
                                .data(3)
                        .build(),
                        RoadSectionFragment.<Integer>builder()
                                .fromFraction(FRACTION_A)
                                .toFraction(FRACTION_END_1)
                                .data(4)
                                .build()));
    }

    @Test
    void splitAndCombine_firstSplitSecondFull() {
        assertThat(roadSectionFragmentService.splitAndCombineData(List.of(ROAD_SECTION_SPLIT_A_START,
                        ROAD_SECTION_SPLIT_A_END), List.of(ONE_FULL_ROAD_SECTION), combiner))
                .isEqualTo(List.of(
                        RoadSectionFragment.<Integer>builder()
                                .fromFraction(FRACTION_START_0)
                                .toFraction(FRACTION_A)
                                .data(3)
                                .build(),
                        RoadSectionFragment.<Integer>builder()
                                .fromFraction(FRACTION_A)
                                .toFraction(FRACTION_END_1)
                                .data(4)
                                .build()));
    }


    @Test
    void splitAndCombine_firstSplitSecondSplitDifferentFractions() {
        assertThat(roadSectionFragmentService.splitAndCombineData(List.of(ROAD_SECTION_SPLIT_A_START,
                        ROAD_SECTION_SPLIT_A_END), List.of(ROAD_SECTION_SPLIT_B_START, ROAD_SECTION_SPLIT_B_END),
                combiner))
                .isEqualTo(List.of(
                        RoadSectionFragment.<Integer>builder()
                                .fromFraction(FRACTION_START_0)
                                .toFraction(FRACTION_A)
                                .data(6)
                                .build(),
                        RoadSectionFragment.<Integer>builder()
                                .fromFraction(FRACTION_A)
                                .toFraction(FRACTION_B)
                                .data(7)
                                .build(),
                        RoadSectionFragment.<Integer>builder()
                                .fromFraction(FRACTION_B)
                                .toFraction(FRACTION_END_1)
                                .data(8)
                                .build()));
    }

    @Test
    void splitAndCombine_firstSplitWithOnlyStartSecondSplitDifferentFractions() {
        assertThat(roadSectionFragmentService.splitAndCombineData(List.of(ROAD_SECTION_SPLIT_A_START),
                List.of(ROAD_SECTION_SPLIT_B_START, ROAD_SECTION_SPLIT_B_END),
                combiner))
                .isEqualTo(List.of(
                        RoadSectionFragment.<Integer>builder()
                                .fromFraction(FRACTION_START_0)
                                .toFraction(FRACTION_A)
                                .data(6)
                                .build(),
                        RoadSectionFragment.<Integer>builder()
                                .fromFraction(FRACTION_A)
                                .toFraction(FRACTION_B)
                                .data(4)
                                .build(),
                        RoadSectionFragment.<Integer>builder()
                                .fromFraction(FRACTION_B)
                                .toFraction(FRACTION_END_1)
                                .data(5)
                                .build()));
    }

    @Test
    void splitAndCombine_firstSplitWithOnlyStartSecondSplitDifferentFractionsA() {
        assertThat(roadSectionFragmentService.splitAndCombineData(List.of(ROAD_SECTION_SPLIT_A_END),
                List.of(ROAD_SECTION_SPLIT_B_START, ROAD_SECTION_SPLIT_B_END),
                combiner))
                .isEqualTo(List.of(
                        RoadSectionFragment.<Integer>builder()
                                .fromFraction(FRACTION_START_0)
                                .toFraction(FRACTION_A)
                                .data(4)
                                .build(),
                        RoadSectionFragment.<Integer>builder()
                                .fromFraction(FRACTION_A)
                                .toFraction(FRACTION_B)
                                .data(7)
                                .build(),
                        RoadSectionFragment.<Integer>builder()
                                .fromFraction(FRACTION_B)
                                .toFraction(FRACTION_END_1)
                                .data(8)
                                .build()));
    }

    @Test
    void splitAndCombine_firstSplitSecondSplitWithOnlyStartDifferentFractions() {
        assertThat(roadSectionFragmentService.splitAndCombineData(List.of(ROAD_SECTION_SPLIT_A_START,
                        ROAD_SECTION_SPLIT_A_END), List.of(ROAD_SECTION_SPLIT_B_START, ROAD_SECTION_SPLIT_B_END),
                combiner))
                .isEqualTo(List.of(
                        RoadSectionFragment.<Integer>builder()
                                .fromFraction(FRACTION_START_0)
                                .toFraction(FRACTION_A)
                                .data(6)
                                .build(),
                        RoadSectionFragment.<Integer>builder()
                                .fromFraction(FRACTION_A)
                                .toFraction(FRACTION_B)
                                .data(7)
                                .build(),
                        RoadSectionFragment.<Integer>builder()
                                .fromFraction(FRACTION_B)
                                .toFraction(FRACTION_END_1)
                                .data(8)
                                .build()));
    }


    @Test
    void splitAndCombine_firstSplitSecondSplitSameFractions() {
        assertThat(roadSectionFragmentService.splitAndCombineData(List.of(ROAD_SECTION_SPLIT_A_START,
                        ROAD_SECTION_SPLIT_A_END), List.of(ROAD_SECTION_SPLIT_A_START, ROAD_SECTION_SPLIT_A_END),
                combiner))
                .isEqualTo(List.of(ROAD_SECTION_SPLIT_A_START.withData( ROAD_SECTION_SPLIT_A_START.getData() +
                                                                        ROAD_SECTION_SPLIT_A_START.getData()),
                            ROAD_SECTION_SPLIT_A_END.withData(  ROAD_SECTION_SPLIT_A_END.getData() +
                                                                ROAD_SECTION_SPLIT_A_END.getData())));
    }

    @Test
    void splitAndCombine_missingFragmentAreaInBetweenInputLowerFractionsFirst() {
        assertThat(roadSectionFragmentService.splitAndCombineData(List.of(ROAD_SECTION_SPLIT_A_START),
                List.of(ROAD_SECTION_SPLIT_B_END), combiner))
                .isEqualTo(List.of(
                        RoadSectionFragment.<Integer>builder()
                                .fromFraction(FRACTION_START_0)
                                .toFraction(FRACTION_A)
                                .data(2)
                                .build(),

                        // missing area between 0.2 and 0.4

                        RoadSectionFragment.<Integer>builder()
                                .fromFraction(FRACTION_B)
                                .toFraction(FRACTION_END_1)
                                .data(5)
                                .build()));
    }

    @Test
    void splitAndCombine_missingFragmentAreaInBetweenInputHigherFractionsFirst() {
        // The output should always be in order of up following fragments
        assertThat(roadSectionFragmentService.splitAndCombineData(List.of(ROAD_SECTION_SPLIT_B_END),
                List.of(ROAD_SECTION_SPLIT_A_START), combiner))
                .isEqualTo(List.of(
                        RoadSectionFragment.<Integer>builder()
                                .fromFraction(FRACTION_START_0)
                                .toFraction(FRACTION_A)
                                .data(2)
                                .build(),

                        // missing area between 0.2 and 0.4

                        RoadSectionFragment.<Integer>builder()
                                .fromFraction(FRACTION_B)
                                .toFraction(FRACTION_END_1)
                                .data(5)
                                .build()));
    }


    @Test
    void splitAndCombine_missingStartFragmentArea() {
        assertThat(roadSectionFragmentService.splitAndCombineData(List.of(),
                List.of(ROAD_SECTION_SPLIT_B_END),combiner))
                .isEqualTo(List.of(
                         // missing start area between 0 and 0.4

                        RoadSectionFragment.<Integer>builder()
                                .fromFraction(FRACTION_B)
                                .toFraction(FRACTION_END_1)
                                .data(5)
                                .build()));
    }

    /**
     * Test the order of combiner arguments in splitAndCombine
     */
    @Test
    void splitAndCombine_combinerResultAFirstParameter() {
        when(inputVerifyingCombiner.apply(1, null)).thenReturn(1);
        assertThat(roadSectionFragmentService.splitAndCombineData(List.of(ONE_FULL_ROAD_SECTION),
                Collections.emptyList(), inputVerifyingCombiner)).isEqualTo(List.of(ONE_FULL_ROAD_SECTION));

        verify(inputVerifyingCombiner).apply(1,null);
    }

    @Test
    void splitAndCombine_combinerResultBSecondParameter() {
        when(inputVerifyingCombiner.apply(null, 1)).thenReturn(1);
        assertThat(roadSectionFragmentService.splitAndCombineData(
                Collections.emptyList(), List.of(ONE_FULL_ROAD_SECTION), inputVerifyingCombiner))
                .isEqualTo(List.of(ONE_FULL_ROAD_SECTION));

        verify(inputVerifyingCombiner).apply(null,1);
    }



}