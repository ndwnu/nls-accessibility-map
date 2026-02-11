package nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;

import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClass;
import nu.ndw.nls.accessibilitymap.job.trafficsign.emission.dto.EuroClassification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmissionClassMapperTest {

    private EmissionClassMapper emissionClassMapper;

    @BeforeEach
    void setUp() {

        emissionClassMapper = new EmissionClassMapper();
    }

    @ParameterizedTest
    @EnumSource(EuroClassification.class)
    void map(EuroClassification euroClassification) {

        if (euroClassification == EuroClassification.UNKNOWN) {
            assertThat(catchThrowable(() -> emissionClassMapper.map(Set.of(euroClassification))))
                    .hasMessage("Euro classification '%s' is not supported." .formatted(euroClassification))
                    .isInstanceOf(IllegalStateException.class);
        } else {
            EmissionClass expectedEuroClassification = mapEmissionClass(euroClassification);

            Set<EmissionClass> emissionZones = emissionClassMapper.map(Set.of(euroClassification));

            assertThat(emissionZones).containsExactlyElementsOf(Set.of(expectedEuroClassification));
        }
    }

    private EmissionClass mapEmissionClass(EuroClassification euroClassifications) {

        return switch (euroClassifications) {
            case EuroClassification.EURO_1 -> EmissionClass.EURO_1;
            case EuroClassification.EURO_2 -> EmissionClass.EURO_2;
            case EuroClassification.EURO_3 -> EmissionClass.EURO_3;
            case EuroClassification.EURO_4 -> EmissionClass.EURO_4;
            case EuroClassification.EURO_5 -> EmissionClass.EURO_5;
            case EuroClassification.EURO_6 -> EmissionClass.EURO_6;

            case EuroClassification.UNKNOWN -> EmissionClass.UNKNOWN;
        };
    }
}
