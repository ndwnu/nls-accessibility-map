package nu.ndw.nls.accessibilitymap.jobs.data.analyser.emission.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EuroClassificationTest {

    @ParameterizedTest
    @EnumSource(value = EuroClassification.class)
    void fromValue(EuroClassification euroClassification) {

        assertThat(EuroClassification.fromValue(euroClassification.getValue())).isEqualTo(euroClassification);
    }

    @Test
    void fromValue_invalidValue() {

        assertThat(EuroClassification.fromValue("invalidValue")).isEqualTo(EuroClassification.UNKNOWN);
    }
}