package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
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

        when(transportRestrictions.isRestrictive(accessibilityRequest)).thenReturn(hasRestrictions);
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
    void isRestrictive_hasTrafficSignTextTypes(boolean requestHasSupplementarySignTypes, boolean matchesTextSignType, boolean expectedResult) {

        SupplementaryTrafficSign supplementaryTrafficSign = mock(SupplementaryTrafficSign.class);
        SupplementarySignType supplementarySignType = mock(SupplementarySignType.class);

        trafficSign = trafficSign.withSupplementaryTrafficSigns(List.of(supplementaryTrafficSign));

        if (requestHasSupplementarySignTypes) {
            when(supplementaryTrafficSign.type()).thenReturn(supplementarySignType);
            if (matchesTextSignType) {
                accessibilityRequest = accessibilityRequest.withTrafficSignSupplementarySignTypes(Set.of(supplementarySignType));
            } else {
                accessibilityRequest = accessibilityRequest.withTrafficSignSupplementarySignTypes(Set.of(mock(SupplementarySignType.class)));
            }
        } else {
            accessibilityRequest = accessibilityRequest.withTrafficSignSupplementarySignTypes(null);
        }

        assertThat(TrafficSignRestrictionCalculator.isRestrictive(trafficSign, accessibilityRequest)).isEqualTo(expectedResult);
    }
}
