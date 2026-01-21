package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.mapper.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClass;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.EmissionClassJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmissionClassMapperV2Test {

    private EmissionClassMapperV2 emissionClassMapperV2;

    @BeforeEach
    void setUp() {

        emissionClassMapperV2 = new EmissionClassMapperV2();
    }

    @ParameterizedTest
    @EnumSource(EmissionClassJson.class)
    @NullSource
    void map(EmissionClassJson emissionClassJson) {
        Set<EmissionClass> actualEmissionClasses = emissionClassMapperV2.map(emissionClassJson);
        if (emissionClassJson == EmissionClassJson.ZERO) {
            assertThat(actualEmissionClasses).isEmpty();
        } else if (Objects.isNull(emissionClassJson)) {
            assertThat(actualEmissionClasses).isNull();
        } else {
            assertThat(actualEmissionClasses).containsExactly(EmissionClass.valueOf(emissionClassJson.name()));
        }
    }
}
