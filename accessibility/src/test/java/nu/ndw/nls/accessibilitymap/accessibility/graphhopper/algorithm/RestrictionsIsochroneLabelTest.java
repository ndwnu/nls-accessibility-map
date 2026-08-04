package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.algorithm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restrictions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RestrictionsIsochroneLabelTest {

    @Mock
    private Restriction restriction;

    @Test
    void hasRestrictions() {
        var restrictionsIsochroneLabel = new RestrictionsIsochroneLabel(1, 2, 3, null, 4, 5, 6, new Restrictions(Set.of(restriction)),
                false, false);
        assertThat(restrictionsIsochroneLabel.hasRestrictions()).isTrue();
    }

    @Test
    void hasRestrictions_noRestrictions() {
        var restrictionsIsochroneLabel = new RestrictionsIsochroneLabel(1, 2, 3, null, 4, 5, 6, new Restrictions(Set.of()),
                false, false);
        assertThat(restrictionsIsochroneLabel.hasRestrictions()).isFalse();

        restrictionsIsochroneLabel = new RestrictionsIsochroneLabel(1, 2, 3, null, 4, 5, 6, null, false, false);
        assertThat(restrictionsIsochroneLabel.hasRestrictions()).isFalse();
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            true, false, true
            false, true, true
            false , false, false
            true, true, false
            """)
    void isTraversingBackwardRelativeToRoad(boolean traversalInReverseFlow, boolean edgeIsReversed, boolean expectedResult) {
        var restrictionsIsochroneLabel = new RestrictionsIsochroneLabel(0, 0, 0, null, 0, 5, 0, new Restrictions(Set.of()),
                traversalInReverseFlow, edgeIsReversed);

        assertThat(restrictionsIsochroneLabel.isTraversingBackwardRelativeToRoad()).isEqualTo(expectedResult);
    }
}
