package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.roadsectionfraction.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.roadsectionfraction.model.RoadSectionFragment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoadSectionFragmentServiceCombineSequentialsTest {

    private static final int FRACTION_START_0 = 0;
    private static final double FRACTION_0_2 = 0.2;
    private static final double FRACTION_0_4 = 0.4;
    private static final int FRACTION_END_1 = 1;
    private static final boolean DATA_TRUE = true;
    private static final boolean DATA_FALSE = false;
    private static final double FRACTION_0_6 = 0.6;
    /**
     * Test combineSequentials
     */
    private final RoadSectionFragmentService roadSectionFragmentService = new RoadSectionFragmentService();

    BiPredicate<Boolean, Boolean> combiningPossible = (previousDataBoolean, nextDataBoolean) ->
            previousDataBoolean && nextDataBoolean;

    BinaryOperator<Boolean> combiner = (previousDataBoolean, nextDataBoolean) ->
            previousDataBoolean;

    @Test
    void combineSequentials_ok_emptyList() {
        assertThat(roadSectionFragmentService.combineSequentials(Collections.emptyList(), combiningPossible, combiner))
                .isEqualTo(Collections.emptyList());
    }

    @Test
    void combineSequentials_ok_nullList() {
        assertThat(roadSectionFragmentService.combineSequentials(null, combiningPossible, combiner))
                .isEqualTo(Collections.emptyList());
    }

    @Test
    void combineSequentials_ok_allCombineAbleDataMatches() {
        assertThat(roadSectionFragmentService.combineSequentials(List.of(
                createFragment(FRACTION_START_0, FRACTION_0_2, DATA_TRUE),
                createFragment(FRACTION_0_2, FRACTION_0_4, DATA_TRUE),
                createFragment(FRACTION_0_4, FRACTION_END_1, DATA_TRUE)), combiningPossible, combiner))
                .isEqualTo(List.of(createFragment(FRACTION_START_0, FRACTION_END_1, DATA_TRUE)));
    }

    @Test
    void combineSequentials_ok_nothingCombineAbleSequentialDataNotMatching() {
        RoadSectionFragment<Boolean> fragmentA = createFragment(FRACTION_START_0, FRACTION_0_2, DATA_TRUE);
        RoadSectionFragment<Boolean> fragmentB = createFragment(FRACTION_0_2, FRACTION_0_4, DATA_FALSE);
        RoadSectionFragment<Boolean> fragmentC = createFragment(FRACTION_0_4, FRACTION_END_1, DATA_TRUE);

        assertThat(roadSectionFragmentService.combineSequentials(List.of(
                fragmentA, fragmentB, fragmentC), combiningPossible, combiner))
                .isEqualTo(List.of(fragmentA, fragmentB, fragmentC));
    }

    @Test
    void combineSequentials_ok_startPartlyCombineAbleSequentialDataMatches() {
        RoadSectionFragment<Boolean> fragmentA = createFragment(FRACTION_START_0, FRACTION_0_2, DATA_TRUE);
        RoadSectionFragment<Boolean> fragmentB = createFragment(FRACTION_0_2, FRACTION_0_4, DATA_TRUE);
        RoadSectionFragment<Boolean> fragmentC = createFragment(FRACTION_0_4, FRACTION_END_1, DATA_FALSE);

        assertThat(roadSectionFragmentService.combineSequentials(List.of(
                fragmentA, fragmentB, fragmentC), combiningPossible, combiner))
                .isEqualTo(List.of(createFragment(FRACTION_START_0, FRACTION_0_4, DATA_TRUE), fragmentC));
    }


    @Test
    void combineSequentials_ok_endPartlyCombineAbleSequentialDataMatches() {
        RoadSectionFragment<Boolean> fragmentA = createFragment(FRACTION_START_0, FRACTION_0_2, DATA_FALSE);
        RoadSectionFragment<Boolean> fragmentB = createFragment(FRACTION_0_2, FRACTION_0_4, DATA_TRUE);
        RoadSectionFragment<Boolean> fragmentC = createFragment(FRACTION_0_4, FRACTION_END_1, DATA_TRUE);

        assertThat(roadSectionFragmentService.combineSequentials(List.of(
                fragmentA, fragmentB, fragmentC), combiningPossible, combiner))
                .isEqualTo(List.of(fragmentA, createFragment(FRACTION_0_2, FRACTION_END_1, DATA_TRUE)));
    }

    @Test
    void combineSequentials_ok_notCombineableFragmentsNotSequential() {
        RoadSectionFragment<Boolean> fragmentA = createFragment(FRACTION_START_0, FRACTION_0_2, DATA_TRUE);
        RoadSectionFragment<Boolean> fragmentC = createFragment(FRACTION_0_4, FRACTION_END_1, DATA_TRUE);

        assertThat(roadSectionFragmentService.combineSequentials(List.of(
                fragmentA, fragmentC), combiningPossible, combiner))
                .isEqualTo(List.of(fragmentA, fragmentC));
    }


    @Test
    void combineSequentials_ok_startNotCombineableFragmentsNotSequential() {
        RoadSectionFragment<Boolean> fragmentA = createFragment(FRACTION_START_0, FRACTION_0_2, DATA_TRUE);
        RoadSectionFragment<Boolean> fragmentC = createFragment(FRACTION_0_4, FRACTION_0_6, DATA_TRUE);
        RoadSectionFragment<Boolean> fragmentD = createFragment(FRACTION_0_6, FRACTION_END_1, DATA_TRUE);

        assertThat(roadSectionFragmentService.combineSequentials(List.of(
                fragmentA, fragmentC, fragmentD), combiningPossible, combiner))
                .isEqualTo(List.of(fragmentA, createFragment(FRACTION_0_4, FRACTION_END_1, DATA_TRUE)));
    }

    @Test
    void combineSequentials_ok_endNotCombineableFragmentsNotSequential() {
        RoadSectionFragment<Boolean> fragmentA = createFragment(FRACTION_START_0, FRACTION_0_2, DATA_TRUE);
        RoadSectionFragment<Boolean> fragmentB = createFragment(FRACTION_0_2, FRACTION_0_4, DATA_TRUE);
        RoadSectionFragment<Boolean> fragmentD = createFragment(FRACTION_0_6, FRACTION_END_1, DATA_TRUE);

        assertThat(roadSectionFragmentService.combineSequentials(List.of(
                fragmentA, fragmentB, fragmentD), combiningPossible, combiner))
                .isEqualTo(List.of(createFragment(FRACTION_START_0, FRACTION_0_4, DATA_TRUE), fragmentD));
    }

    private RoadSectionFragment<Boolean> createFragment(double from, double to, boolean data) {
        return RoadSectionFragment.<Boolean>builder()
                    .fromFraction(from)
                    .toFraction(to)
                    .data(data)
                .build();
    }


}