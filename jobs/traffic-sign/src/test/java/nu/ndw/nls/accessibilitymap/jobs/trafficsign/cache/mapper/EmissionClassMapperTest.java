package nu.ndw.nls.accessibilitymap.jobs.trafficsign.cache.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;

import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClass;
import nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto.EuroClassification;
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
                    .withFailMessage("Unknown euro classification '%s'." .formatted(euroClassification))
                    .isInstanceOf(IllegalStateException.class);
        } else {
            EmissionClass expectedEuroClassification = mapEmissionClass(euroClassification);

            Set<EmissionClass> emissionZones = emissionClassMapper.map(Set.of(euroClassification));

            assertThat(emissionZones).containsExactlyElementsOf(Set.of(expectedEuroClassification));
        }
    }

    private EmissionClass mapEmissionClass(EuroClassification euroClassifications) {

        return switch (euroClassifications) {
            case EuroClassification.EURO_1 -> EmissionClass.ONE;
            case EuroClassification.EURO_2 -> EmissionClass.TWO;
            case EuroClassification.EURO_3 -> EmissionClass.THREE;
            case EuroClassification.EURO_4 -> EmissionClass.FOUR;
            case EuroClassification.EURO_5 -> EmissionClass.FIVE;
            case EuroClassification.EURO_6 -> EmissionClass.SIX;

            case nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto.EuroClassification.UNKNOWN -> EmissionClass.UNKNOWN;
        };
    }
}