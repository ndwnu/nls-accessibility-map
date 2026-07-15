package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.algorithm.limit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.util.EncodingManager;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.algorithm.RestrictionsIsochroneLabel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExploreLimitRestrictionTest {

    private ExploreLimitRestriction exploreLimitRestriction;

    @Mock
    private EncodingManager encodingManager;

    @Mock
    private Restriction restriction;

    @BeforeEach
    void setUp() {

        exploreLimitRestriction = new ExploreLimitRestriction();
    }

    @Test
    void isInLimit_withinLimit() {

        RestrictionsIsochroneLabel label = createLabel(new Restrictions());

        assertThat(exploreLimitRestriction.isInLimit(label, encodingManager)).isTrue();
    }

    @Test
    void isInLimit_notWithinLimit() {

        Restrictions restrictions = new Restrictions();
        restrictions.add(restriction);
        RestrictionsIsochroneLabel label = createLabel(restrictions);

        assertThat(exploreLimitRestriction.isInLimit(label, encodingManager)).isFalse();
    }

    @Test
    void debug() {

        RestrictionsIsochroneLabel label = createLabel(new Restrictions());

        assertThat(exploreLimitRestriction.debug(label, encodingManager))
                .isEqualTo("ExploreLimitRestriction{limit=1.0, restrictions=[], reached=false}");
    }

    @Test
    void debug_limitReached() {

        when(restriction.toString()).thenReturn("someRestriction");
        Restrictions restrictions = new Restrictions();
        restrictions.add(restriction);
        RestrictionsIsochroneLabel label = createLabel(restrictions);

        assertThat(exploreLimitRestriction.debug(label, encodingManager))
                .isEqualTo("ExploreLimitRestriction{limit=1.0, restrictions=[someRestriction], reached=true}");
    }

    private static RestrictionsIsochroneLabel createLabel(Restrictions restrictions) {
        return new RestrictionsIsochroneLabel(0, 5, 5, null, 0L, 0.0, 0.0, restrictions);
    }
}
