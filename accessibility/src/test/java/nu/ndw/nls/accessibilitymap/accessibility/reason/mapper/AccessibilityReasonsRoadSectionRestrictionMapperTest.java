package nu.ndw.nls.accessibilitymap.accessibility.reason.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.roadsection.RoadSectionRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibleReason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityReasonsRoadSectionRestrictionMapperTest {

    private AccessibilityReasonsRoadSectionRestrictionMapper mapper;

    @BeforeEach
    void setUp() {

        mapper = new AccessibilityReasonsRoadSectionRestrictionMapper();
    }

    @Test
    void mapRestrictions() {

        RoadSectionRestriction roadSectionRestriction = RoadSectionRestriction.builder().build();

        List<AccessibilityReason<?>> accessibilityReasons = mapper.mapRestrictions(new Restrictions(Set.of(roadSectionRestriction)));

        assertThat(accessibilityReasons).hasSize(1);

        assertThat(accessibilityReasons.getFirst()).isInstanceOf(AccessibleReason.class);
        AccessibleReason accessibilityReason = (AccessibleReason) accessibilityReasons.getFirst();

        assertThat(accessibilityReason.getRestrictions()).containsExactly(roadSectionRestriction);
        assertThat(accessibilityReason.getValue()).isFalse();
    }

    @Test
    void mapRestrictions_invalidRestrictionType() {

        Restriction restriction = mock(Restriction.class);

        List<AccessibilityReason<?>> accessibilityReasons = mapper.mapRestrictions(new Restrictions(Set.of(restriction)));

        assertThat(accessibilityReasons).isEmpty();
    }
}
