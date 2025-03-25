package nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Objects;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.request.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSign;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrafficSignTest {

    private TrafficSign trafficSign;

    @Mock
    private Restrictions restrictions;

    @Test
    void hasTimeWindowedSign() {

        trafficSign = TrafficSign.builder()
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

        trafficSign = TrafficSign.builder()
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

        trafficSign = TrafficSign.builder()
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

        assertThat(trafficSign.findFirstTimeWindowedSign().get().getText()).isEqualTo("1");
    }

    @Test
    void findFirstTimeWindowedSign_nothingFound() {

        trafficSign = TrafficSign.builder()
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
    @CsvSource(textBlock = """
            C6, true, true, true,
            C6, true, false, false,
            C6, false, true, true,
            C6, false, false, true,
            , true, true, true,
            , true, false, false,
            , false, true, true,
            , false, false, true""")
    void isRelevant(String trafficSignType, boolean hasRestrictions, boolean isRestrictive, boolean expectedResult) {

        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder().build();

        when(restrictions.hasActiveRestrictions(accessibilityRequest)).thenReturn(hasRestrictions);
        if(hasRestrictions) {
            when(restrictions.isRestrictive(accessibilityRequest)).thenReturn(isRestrictive);
        }

        trafficSign = TrafficSign.builder()
                .trafficSignType(Objects.nonNull(trafficSignType) ? TrafficSignType.fromRvvCode(trafficSignType) : null)
                .restrictions(restrictions)
                .build();

        assertThat(trafficSign.isRelevant(accessibilityRequest)).isEqualTo(expectedResult);
    }
}
