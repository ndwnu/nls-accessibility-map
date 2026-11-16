package nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper.emission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;

import java.util.Objects;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import nu.ndw.nls.accessibilitymap.job.trafficsign.emission.dto.VehicleCategory;
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

    @Test
    void map_noVehicleCategoryNoMaximumWeightInKgNoMaximum() {
        Maximum maximumWeight = maximumWeightMapper.map(null, null);
        assertThat(maximumWeight).isNull();
    }

    @ParameterizedTest
    @CsvSource(nullValues = "null", textBlock = """
            null,   null,   null
            N_1,    null,   3500
            null,   123,    123
            N_1,    123,    123
            N_1,    3500,   3500
            N_1,    3501,   3500
            N_2,    null,   null
            N_2,    123,    123
            """)
    void map_vehicleCategoryAndMaximumWeight(String vehicleCategoryString, Double maximumWeightInKg, Double expectedValue) {
        Set<VehicleCategory> vehicleCategory =
                Objects.nonNull(vehicleCategoryString) ? Set.of(VehicleCategory.fromValue(vehicleCategoryString)) : null;

        Maximum maximumWeight = maximumWeightMapper.map(vehicleCategory, maximumWeightInKg);

        if (Objects.isNull(expectedValue)) {
            assertThat(maximumWeight).isNull();
        } else {
            assertThat(maximumWeight).isEqualTo(Maximum.builder().value(expectedValue).build());
        }
    }

    @ParameterizedTest
    @EnumSource(VehicleCategory.class)
    @NullSource
    void map_vehicleCategory(VehicleCategory vehicleCategory) {

        if (vehicleCategory == VehicleCategory.UNKNOWN) {
            assertThat(catchThrowable(() -> maximumWeightMapper.map(Set.of(vehicleCategory))))
                    .hasMessage("Unknown vehicle category '%s'.".formatted(vehicleCategory))
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
    void map_vehicleCategory_multipleCategoriesWithoutLimit() {

        Maximum maximumWeight = maximumWeightMapper.map(Set.of(VehicleCategory.N_1, VehicleCategory.N_2, VehicleCategory.N_3));

        assertThat(maximumWeight).isNull();
    }

    @Test
    void map_vehicleCategory_noCategories() {

        Maximum maximumWeight = maximumWeightMapper.map(Set.of());

        assertThat(maximumWeight).isNull();
    }

    private Double maxWeight(VehicleCategory vehicleCategory) {
        if (Objects.isNull(vehicleCategory)) {
            return null;
        }
        return vehicleCategory == VehicleCategory.N_1 ? 3_500D : null;
    }
}
