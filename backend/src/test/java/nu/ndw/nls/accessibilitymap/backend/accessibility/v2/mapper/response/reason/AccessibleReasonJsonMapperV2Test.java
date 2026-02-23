package nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.response.reason;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason.ReasonType;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibleReason;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.AccessibleReasonJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.ReasonConditionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.ReasonJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.ReasonJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.ReasonUnitSymbolJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibleReasonJsonMapperV2Test {

    private AccessibleReasonJsonMapperV2 accessibleReasonJsonMapperV2;

    @Mock
    private RestrictionJson restrictionJson;

    @BeforeEach
    void setUp() {
        accessibleReasonJsonMapperV2 = new AccessibleReasonJsonMapperV2();
    }

    @Test
    void map() {
        AccessibleReason accessibleReason = AccessibleReason.builder()
                .value(true)
                .build();

        ReasonJson reasonJson = accessibleReasonJsonMapperV2.map(accessibleReason, List.of(restrictionJson));

        assertThat(reasonJson)
                .isInstanceOf(AccessibleReasonJson.class)
                .isEqualTo(getExpected());
    }

    private AccessibleReasonJson getExpected() {
        return new AccessibleReasonJson()
                .type(TypeEnum.ACCESSIBLE_REASON)
                .value(true)
                .condition(ReasonConditionJson.EQUALS)
                .unitSymbol(ReasonUnitSymbolJson.BOOLEAN)
                .becauseOf(List.of(restrictionJson));
    }

    @Test
    void getReasonType() {
        assertThat(accessibleReasonJsonMapperV2.getReasonType()).isEqualTo(ReasonType.ACCESSIBLE_REASON);
    }
}
