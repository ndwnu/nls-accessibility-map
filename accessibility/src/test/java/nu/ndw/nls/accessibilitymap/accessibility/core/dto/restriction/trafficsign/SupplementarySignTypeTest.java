package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign;

import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB254;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB256;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB259;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB401;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB411;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

class SupplementarySignTypeTest {

    private static final Set<SupplementarySignType> PRE_ANNOUNCEMENTS = EnumSet.of(OB401, OB411);

    private static final Set<SupplementarySignType> TIME_WINDOWED_TYPES = EnumSet.of(OB254, OB256, OB259);

    @Test
    void getWindowTimeTypes() {
        assertThat(SupplementarySignType.getWindowTimeTypes())
                .isSameAs(SupplementarySignType.getWindowTimeTypes())
                .isEqualTo(TIME_WINDOWED_TYPES);
    }

    @Test
    void getPreAnnouncementTypes() {
        assertThat(SupplementarySignType.getPreAnnouncementTypes())
                .isSameAs(SupplementarySignType.getPreAnnouncementTypes())
                .isEqualTo(PRE_ANNOUNCEMENTS);
    }

    @ParameterizedTest
    @EnumSource(value = SupplementarySignType.class, names = {"OB254","OB256","OB259"}, mode = EnumSource.Mode.EXCLUDE)
    void isTimeWindowed_false(SupplementarySignType supplementarySignType) {
        assertThat(supplementarySignType.isWindowTime()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = SupplementarySignType.class, names = {"OB254","OB256","OB259"}, mode = Mode.INCLUDE)
    void isTimeWindowed_true(SupplementarySignType supplementarySignType) {
        assertThat(supplementarySignType.isWindowTime()).isTrue();
    }

}