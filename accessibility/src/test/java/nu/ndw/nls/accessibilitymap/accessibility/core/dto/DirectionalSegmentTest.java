package nu.ndw.nls.accessibilitymap.accessibility.core.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restrictions;
import org.junit.jupiter.api.Test;

class DirectionalSegmentTest {

    @Test
    void hasRestrictions() {

        DirectionalSegment directionalSegment = DirectionalSegment.builder()
                .restrictions(mock(Restrictions.class))
                .build();

        assertThat(directionalSegment.hasRestrictions()).isTrue();
    }

    @Test
    void hasRestrictions_noRestrictions() {

        DirectionalSegment directionalSegment = DirectionalSegment.builder().build();

        assertThat(directionalSegment.hasRestrictions()).isFalse();
    }
}
