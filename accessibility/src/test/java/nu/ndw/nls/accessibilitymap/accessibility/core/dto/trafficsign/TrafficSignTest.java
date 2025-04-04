package nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSign;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrafficSignTest {

    @Mock
    private Restrictions restrictions;

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
}
