package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.algorithm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restrictions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RestrictionsIsochroneLabelTest {

    @Mock
    private Restriction restriction;

    @Test
    void hasRestrictions() {
        var restrictionsIsochroneLabel = new RestrictionsIsochroneLabel(1, 2, 3, null, 4, 5, 6, new Restrictions(Set.of(restriction)));
        assertThat(restrictionsIsochroneLabel.hasRestrictions()).isTrue();
    }

    @Test
    void hasRestrictions_noRestrictions() {
        var restrictionsIsochroneLabel = new RestrictionsIsochroneLabel(1, 2, 3, null, 4, 5, 6, new Restrictions(Set.of()));
        assertThat(restrictionsIsochroneLabel.hasRestrictions()).isFalse();

        restrictionsIsochroneLabel = new RestrictionsIsochroneLabel(1, 2, 3, null, 4, 5, 6, null);
        assertThat(restrictionsIsochroneLabel.hasRestrictions()).isFalse();
    }
}
