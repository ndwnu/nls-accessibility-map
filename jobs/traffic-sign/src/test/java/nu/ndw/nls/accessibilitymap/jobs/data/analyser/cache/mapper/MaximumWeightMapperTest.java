package nu.ndw.nls.accessibilitymap.jobs.data.analyser.cache.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import nu.ndw.nls.accessibilitymap.jobs.data.analyser.emission.dto.VehicleCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MaximumWeightMapperTest {

    private MaximumWeightMapper maximumWeightMapper;

    @BeforeEach
    void setUp() {

        maximumWeightMapper = new MaximumWeightMapper();
    }

    @ParameterizedTest
    @CsvSource(nullValues = "null", textBlock = """
            null, null, null
            M_2, null, 5000
            null, 123, 123
            M_2, 123, 123
            M_2, 5123, 5000
            """)
    void map_vehicleCategoryAndMaximumWeight(String vehicleCategoryString, Double maximumWeightInKg, Double expectedValue) {
        Set<VehicleCategory> vehicleCategory =
                Objects.nonNull(vehicleCategoryString) ? Set.of(VehicleCategory.fromValue(vehicleCategoryString)) : null;

        Maximum maximumWeight = maximumWeightMapper.map(vehicleCategory, maximumWeightInKg);

        if(Objects.nonNull(expectedValue)) {
            assertThat(maximumWeight).isEqualTo(Maximum.builder().value(expectedValue).build());
        } else {
            assertThat(maximumWeight).isNull();
        }
    }

    @ParameterizedTest
    @EnumSource(VehicleCategory.class)
    @NullSource
    void map_vehicleCategory(VehicleCategory vehicleCategory) {

        if (vehicleCategory == VehicleCategory.UNKNOWN) {
            assertThat(catchThrowable(() -> maximumWeightMapper.map(Set.of(vehicleCategory))))
                    .hasMessage("Unknown vehicle category '%s'." .formatted(vehicleCategory))
                    .isInstanceOf(IllegalStateException.class);
        } else {
            Double expectedMaxWeight = maxWeight(vehicleCategory);
            Maximum maximumWeight = maximumWeightMapper.map(Objects.nonNull(vehicleCategory) ? Set.of(vehicleCategory) : null);

            if (Objects.nonNull(expectedMaxWeight)) {
                assertThat(maximumWeight).isEqualTo(Maximum.builder().value(expectedMaxWeight).build());
            } else {
                assertThat(maximumWeight).isNull();
            }
        }
    }

    @Test
    void map_vehicleCategory_minimumValueExpectedIfMultipleCategoriesSupplied() {

        Set<VehicleCategory> vehicleCategories = Arrays.stream(VehicleCategory.values())
                .filter(vehicleCategory -> !vehicleCategory.equals(VehicleCategory.UNKNOWN))
                .collect(Collectors.toSet());
        Maximum maximumWeight = maximumWeightMapper.map(vehicleCategories);

        assertThat(maximumWeight).isEqualTo(Maximum.builder().value(5_000D).build());
    }

    private Double maxWeight(VehicleCategory vehicleCategory) {
        if (Objects.isNull(vehicleCategory)) {
            return null;
        }
        return switch (vehicleCategory) {
            case M_2 -> 5_000D;
            case N_1 -> 35_000D;
            case N_2 -> 12_000D;
            case M_3, N_3 -> Double.MAX_VALUE;
            default -> null;
        };
    }

}