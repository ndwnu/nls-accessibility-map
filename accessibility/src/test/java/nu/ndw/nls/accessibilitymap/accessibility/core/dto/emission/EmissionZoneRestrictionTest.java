package nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import nu.ndw.nls.springboot.test.util.validation.ValidationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmissionZoneRestrictionTest extends ValidationTest {

    private EmissionZoneRestriction emissionZoneRestriction;

    @BeforeEach
    void setUp() {

        emissionZoneRestriction = EmissionZoneRestriction.builder()
                .id("id")
                .vehicleWeightInKg(Maximum.builder().value(3d).build())
                .fuelTypes(Set.of(FuelType.DIESEL, FuelType.ELECTRIC))
                .transportTypes(Set.of(TransportType.CAR, TransportType.TAXI))
                .build();
    }

    @ParameterizedTest
    @CsvSource(nullValues = "null", textBlock = """
            2d,     2d,         DIESEL, DIESEL,         CAR,    CAR,    true,
            
            2d,     4d,         DIESEL, DIESEL,         CAR,    CAR,    false,
            2d,     null,       DIESEL, DIESEL,         CAR,    CAR,    true,
            null,   null,       DIESEL, DIESEL,         CAR,    CAR,    true,
            null,   2d,         DIESEL, DIESEL,         CAR,    CAR,    true,
            
            2d,     2d,         DIESEL, ETHANOL,        CAR,    CAR,    false,
            2d,     2d,         DIESEL, null,           CAR,    CAR,    true,
            2d,     2d,         null,   null,           CAR,    CAR,    true,
            2d,     2d,         null,   DIESEL,         CAR,    CAR,    true,
            
            2d,     2d,         DIESEL, DIESEL,         CAR,    BUS,    false,
            2d,     2d,         DIESEL, DIESEL,         CAR,    null,   true,
            2d,     2d,         DIESEL, DIESEL,         null,   null,   true,
            2d,     2d,         DIESEL, DIESEL,         null,   CAR,    true,
            """)
    void isRelevant(
            Double vehicleWeightInKgRestriction,
            Double vehicleWeightInKg,
            String fuelTypeRestriction,
            String fuelType,
            String transportTypeRestriction,
            String transportType,
            boolean expectedExemption) {

        emissionZoneRestriction = emissionZoneRestriction
                .withVehicleWeightInKg(
                        Objects.nonNull(vehicleWeightInKgRestriction) ? Maximum.builder().value(vehicleWeightInKgRestriction).build()
                                : null)
                .withFuelTypes(Objects.nonNull(fuelTypeRestriction) ? Set.of(FuelType.valueOf(fuelTypeRestriction)) : Set.of())
                .withTransportTypes(
                        Objects.nonNull(transportTypeRestriction) ? Set.of(TransportType.valueOf(transportTypeRestriction)) : Set.of());

        assertThat(emissionZoneRestriction.isRelevant(
                vehicleWeightInKg,
                Objects.nonNull(fuelType) ? Set.of(FuelType.valueOf(fuelType)) : Set.of(),
                Objects.nonNull(transportType) ? Set.of(TransportType.valueOf(transportType)) : Set.of()))
                .isEqualTo(expectedExemption);
    }

    @Test
    void isRelevant_noFuelTypes() {

        assertThat(catchThrowable(() -> emissionZoneRestriction.isRelevant(2d, null, Set.of(TransportType.CAR))))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void isRelevant_noTransportTypes() {

        assertThat(catchThrowable(() -> emissionZoneRestriction.isRelevant(2d, Set.of(FuelType.DIESEL), null)))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void validate() {

        validate(emissionZoneRestriction, List.of(), List.of());
    }

    @Test
    void validate_id_null() {

        emissionZoneRestriction = emissionZoneRestriction.withId(null);
        validate(emissionZoneRestriction, List.of("id"), List.of("must not be null"));
    }

    @Test
    void validate_fuelTypes_null() {

        emissionZoneRestriction = emissionZoneRestriction.withFuelTypes(null);
        validate(emissionZoneRestriction, List.of("fuelTypes"), List.of("must not be null"));
    }

    @Test
    void validate_transportTypes_null() {

        emissionZoneRestriction = emissionZoneRestriction.withTransportTypes(null);
        validate(emissionZoneRestriction, List.of("transportTypes"), List.of("must not be null"));
    }

    @Override
    protected Class<?> getClassToTest() {

        return emissionZoneRestriction.getClass();
    }
}