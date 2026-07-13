package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SupplementaryTrafficSignTest {

    @Mock
    private SupplementarySignType supplementarySignType;

    @Test
    void hasWindowTime_true() {
        when(supplementarySignType.isWindowTime()).thenReturn(true);
        SupplementaryTrafficSign supplementaryTrafficSign = SupplementaryTrafficSign.builder()
                .type(supplementarySignType)
                .build();

        assertThat(supplementaryTrafficSign.hasWindowTime()).isTrue();
    }

    @Test
    void hasWindowTime_false() {
        when(supplementarySignType.isWindowTime()).thenReturn(false);
        SupplementaryTrafficSign supplementaryTrafficSign = SupplementaryTrafficSign.builder()
                .type(supplementarySignType)
                .build();

        assertThat(supplementaryTrafficSign.hasWindowTime()).isFalse();
    }
}