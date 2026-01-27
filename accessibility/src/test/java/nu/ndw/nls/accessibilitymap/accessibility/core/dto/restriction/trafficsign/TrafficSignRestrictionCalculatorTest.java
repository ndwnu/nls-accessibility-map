package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSign;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrafficSignRestrictionCalculatorTest {

    private AccessibilityRequest accessibilityRequest;

    private TrafficSign trafficSign;

    @Mock
    private TransportRestrictions transportRestrictions;

    @BeforeEach
    void setUp() {

        trafficSign = TrafficSign.builder()
                .transportRestrictions(transportRestrictions)
                .build();

        accessibilityRequest = AccessibilityRequest.builder()
                .build();
    }

    @ParameterizedTest
    @CsvSource({
            "true, true, true",
            "true, false, false",
            "false, true, false",
            "false, false, false"
    })
    void isRestrictive_hasTransportRestrictions(boolean hasRestrictions, boolean isRestrictive, boolean expectedResult) {

        when(transportRestrictions.hasActiveRestrictions(accessibilityRequest)).thenReturn(hasRestrictions);
        if (hasRestrictions) {
            when(transportRestrictions.isRestrictive(accessibilityRequest)).thenReturn(isRestrictive);
        }

        assertThat(TrafficSignRestrictionCalculator.isRestrictive(trafficSign, accessibilityRequest)).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @CsvSource({
            "true, true, true",
            "true, false, false",
            "false, true, false",
            "false, false, false"
    })
    void isRestrictive_hasTrafficSignType(boolean requestHasTrafficSignTypes, boolean containsTrafficSignType, boolean expectedResult) {

        TrafficSignType trafficSignType = mock(TrafficSignType.class);
        trafficSign = trafficSign.withTrafficSignType(trafficSignType);

        if (requestHasTrafficSignTypes) {
            if (containsTrafficSignType) {
                accessibilityRequest = accessibilityRequest.withTrafficSignTypes(Set.of(trafficSignType));
            } else {
                accessibilityRequest = accessibilityRequest.withTrafficSignTypes(Set.of(mock(TrafficSignType.class)));
            }
        } else {
            accessibilityRequest = accessibilityRequest.withTrafficSignTypes(null);
        }

        assertThat(TrafficSignRestrictionCalculator.isRestrictive(trafficSign, accessibilityRequest)).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @CsvSource({
            "true, true, true",
            "true, false, false",
            "false, true, false",
            "false, false, false"
    })
    void isRestrictive_hasTrafficSignTextTypes(boolean requestHasTrafficSignTextTypes, boolean matchesTextSignType, boolean expectedResult) {

        TextSignType textSignType = mock(TextSignType.class);
        TextSign textSign = mock(TextSign.class);

        trafficSign = trafficSign.withTextSigns(List.of(textSign));

        if (requestHasTrafficSignTextTypes) {
            when(textSign.type()).thenReturn(textSignType);
            if (matchesTextSignType) {
                accessibilityRequest = accessibilityRequest.withTrafficSignTextSignTypes(Set.of(textSignType));
            } else {
                accessibilityRequest = accessibilityRequest.withTrafficSignTextSignTypes(Set.of(mock(TextSignType.class)));
            }
        } else {
            accessibilityRequest = accessibilityRequest.withTrafficSignTextSignTypes(null);
        }

        assertThat(TrafficSignRestrictionCalculator.isRestrictive(trafficSign, accessibilityRequest)).isEqualTo(expectedResult);
    }
}
