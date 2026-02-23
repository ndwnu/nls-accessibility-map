package nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason.ReasonType;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReasonGroup;
import nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.response.reason.AccessibilityReasonJsonMapperV2;
import nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.response.reason.restriction.AccessibilityRestrictionJsonMapperV2;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.ReasonJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityReasonsJsonMapperV2Test {

    private AccessibilityReasonsJsonMapperV2 accessibilityReasonsJsonMapperV2;

    @Mock
    private AccessibilityReason<?> accessibilityReason;

    @Mock
    @SuppressWarnings("unchecked")
    private AccessibilityReasonJsonMapperV2 accessibilityReasonJsonMapperV2;

    @Mock
    @SuppressWarnings("unchecked")
    private AccessibilityRestrictionJsonMapperV2 accessibilityRestrictionJsonMapperV2;

    @Mock
    private ReasonType reasonType;

    private CustomRestriction restriction;

    @Mock
    private ReasonJson reasonJson;

    @Mock
    private RestrictionJson restrictionJson;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setup() {

        restriction = new CustomRestriction();

        when(accessibilityReasonJsonMapperV2.getReasonType()).thenReturn(reasonType);
        when(accessibilityRestrictionJsonMapperV2.getRestrictionType()).thenReturn(restriction.getClass());

        accessibilityReasonsJsonMapperV2 = new AccessibilityReasonsJsonMapperV2(
                List.of(accessibilityReasonJsonMapperV2),
                List.of(accessibilityRestrictionJsonMapperV2)
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void mapToReasonJson() {

        when(accessibilityReason.getReasonType()).thenReturn(reasonType);

        when(accessibilityReasonJsonMapperV2.map(accessibilityReason, List.of(restrictionJson))).thenReturn(reasonJson);
        when(accessibilityRestrictionJsonMapperV2.map(restriction)).thenReturn(restrictionJson);

        when(accessibilityReason.getRestrictions()).thenReturn(Set.of(restriction));

        List<List<ReasonJson>> actual = accessibilityReasonsJsonMapperV2.map(
                List.of(new AccessibilityReasonGroup(List.of(accessibilityReason))));

        assertThat(actual).hasSize(1);
        assertThat(actual.getFirst()).containsExactly(reasonJson);
    }

    @Test
    @SuppressWarnings("unchecked")
    void mapToReasonJson_noRestrictionMapperFound() {

        Restriction genericRestriction = mock(Restriction.class);
        when(accessibilityReason.getReasonType()).thenReturn(reasonType);

        when(accessibilityReasonJsonMapperV2.map(accessibilityReason, List.of())).thenReturn(reasonJson);

        when(accessibilityReason.getRestrictions()).thenReturn(Set.of(genericRestriction));

        List<List<ReasonJson>> actual = accessibilityReasonsJsonMapperV2.map(
                List.of(new AccessibilityReasonGroup(List.of(accessibilityReason))));

        assertThat(actual).hasSize(1);
        assertThat(actual.getFirst()).containsExactly(reasonJson);
    }

    @Test
    void mapToReasonJson_noReasonMapperFound() {

        when(accessibilityReason.getReasonType()).thenReturn(mock(ReasonType.class));

        List<List<ReasonJson>> actual = accessibilityReasonsJsonMapperV2.map(
                List.of(new AccessibilityReasonGroup(List.of(accessibilityReason))));

        assertThat(actual).hasSize(1);
        assertThat(actual.getFirst()).isEmpty();
    }

    private static class CustomRestriction implements Restriction {

        @Override
        public boolean isRestrictive(AccessibilityRequest accessibilityRequest) {
            return false;
        }

        @Override
        public Double networkSnappedLatitude() {
            return 0.0;
        }

        @Override
        public Double networkSnappedLongitude() {
            return 0.0;
        }

        @Override
        public Integer roadSectionId() {
            return 0;
        }

        @Override
        public Direction direction() {
            return null;
        }

        @Override
        public Double fraction() {
            return 0.0;
        }
    }
}
