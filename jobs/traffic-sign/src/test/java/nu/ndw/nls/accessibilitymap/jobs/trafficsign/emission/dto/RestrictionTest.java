package nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto;

import java.util.List;
import java.util.Set;
import nu.ndw.nls.springboot.test.util.validation.ValidationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RestrictionTest extends ValidationTest {

    private Restriction restriction;

    @BeforeEach
    void setUp() {

        restriction = Restriction.builder()
                .id("restrictionId")
                .vehicleCategories(Set.of(VehicleCategory.M_1))
                .fuelType(FuelType.DIESEL)
                .vehicleType(VehicleType.CAR)
                .build();
    }

    @Test
    void validate() {

        validate(restriction, List.of(), List.of());
    }

    @Test
    void validate_id_null() {

        restriction = restriction.withId(null);
        validate(restriction, List.of("id"), List.of("must not be null"));
    }

    @Override
    protected Class<?> getClassToTest() {
        return restriction.getClass();
    }
}