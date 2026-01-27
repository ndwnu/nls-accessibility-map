package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSign;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.mockito.Mockito;

class TrafficSignTest {

    @Test
    void hasTimeWindowedSign() {

        TrafficSign trafficSign = TrafficSign.builder()
                .textSigns(List.of(
                        TextSign.builder()
                                .type(TextSignType.EXCLUDING)
                                .build(),
                        TextSign.builder()
                                .type(TextSignType.TIME_PERIOD)
                                .build()
                ))
                .build();

        assertThat(trafficSign.hasTimeWindowedSign()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = TextSignType.class, mode = Mode.EXCLUDE, names = "TIME_PERIOD")
    void hasTimeWindowedSign_hasNoTimedWindowSign(TextSignType textSignType) {

        TrafficSign trafficSign = TrafficSign.builder()
                .textSigns(List.of(
                        TextSign.builder()
                                .type(textSignType)
                                .build()
                ))
                .build();

        assertThat(trafficSign.hasTimeWindowedSign()).isFalse();
    }

    @Test
    void findFirstTimeWindowedSign() {

        TrafficSign trafficSign = TrafficSign.builder()
                .textSigns(List.of(
                        TextSign.builder()
                                .type(TextSignType.TIME_PERIOD)
                                .text("1")
                                .build(),
                        TextSign.builder()
                                .type(TextSignType.TIME_PERIOD)
                                .text("2")
                                .build()
                ))
                .build();

        assertThat(trafficSign.findFirstTimeWindowedSign().get().text()).isEqualTo("1");
    }

    @Test
    void findFirstTimeWindowedSign_nothingFound() {

        TrafficSign trafficSign = TrafficSign.builder()
                .textSigns(List.of(
                        TextSign.builder()
                                .type(TextSignType.EXCLUDING)
                                .text("1")
                                .build()
                ))
                .build();

        assertThat(trafficSign.findFirstTimeWindowedSign()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
            "true, true, true",
            "true, false, false",
            "false, false, false"
    })
    void isRestrictive(boolean notExcluded, boolean isRestrictive, boolean expectedResult) {

        TrafficSign trafficSign = TrafficSign.builder().build();
        AccessibilityRequest accessibilityRequest = mock(AccessibilityRequest.class);

        try (var trafficSignExclusionCalculator = Mockito.mockStatic(TrafficSignExclusionCalculator.class)) {
            try (var trafficSignRestrictionCalculator = Mockito.mockStatic(TrafficSignRestrictionCalculator.class)) {
                trafficSignExclusionCalculator.when(() -> TrafficSignExclusionCalculator.isNotExcluded(trafficSign, accessibilityRequest))
                        .thenReturn(notExcluded);
                trafficSignRestrictionCalculator.when(() -> TrafficSignRestrictionCalculator.isRestrictive(
                                trafficSign,
                                accessibilityRequest))
                        .thenReturn(isRestrictive);

                assertThat(trafficSign.isRestrictive(accessibilityRequest)).isEqualTo(expectedResult);
            }
        }
    }
}
