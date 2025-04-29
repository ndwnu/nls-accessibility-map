package nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClass;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import nu.ndw.nls.springboot.test.util.validation.ValidationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmissionZoneExemptionTest extends ValidationTest {

    private EmissionZoneExemption emissionZoneExemption;

    @BeforeEach
    void setUp() {

        emissionZoneExemption = EmissionZoneExemption.builder()
                .startTime(OffsetDateTime.MIN)
                .endTime(OffsetDateTime.MAX)
                .emissionClasses(Set.of(EmissionClass.EURO_5, EmissionClass.EURO_6))
                .transportTypes(Set.of(TransportType.CAR, TransportType.TAXI))
                .vehicleWeightInKg(Maximum.builder().value(3d).build())
                .build();
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
                       2022-03-11T08:59:59Z, false,
                       2022-03-11T09:00:00Z, true,
                       2022-03-11T09:59:59Z, true,
                       2022-03-11T10:00:00Z, false,
            """)
    void isActive(OffsetDateTime now, boolean expectedActive) {

        emissionZoneExemption = emissionZoneExemption
                .withStartTime(OffsetDateTime.parse("2022-03-11T09:00:00Z"))
                .withEndTime(OffsetDateTime.parse("2022-03-11T10:00:00Z"));

        assertThat(emissionZoneExemption.isActive(now)).isEqualTo(expectedActive);
    }

    @ParameterizedTest
    @CsvSource(nullValues = "null", textBlock = """
            2d,     EURO5,   CAR,    true,
            4d,     EURO5,   CAR,    false,
            2d,     EURO4,   CAR,    false,
            2d,     EURO5,   BUS,    false,
            null,   EURO5,   CAR,    true,
            2d,     null,   CAR,    true,
            2d,     EURO5,   null,   true,
            """)
    void isExempt(
            Double vehicleWeightInKg,
            String emissionClass,
            String transportType,
            boolean expectedExemption) {

        emissionZoneExemption = emissionZoneExemption
                .withTransportTypes(Set.of(TransportType.CAR, TransportType.TAXI))
                .withEmissionClasses(Set.of(EmissionClass.EURO_5, EmissionClass.EURO_6))
                .withVehicleWeightInKg(Maximum.builder().value(3d).build());

        assertThat(emissionZoneExemption.isExempt(
                vehicleWeightInKg,
                Objects.nonNull(emissionClass) ? Set.of(emissionClass.equals("EURO5") ? EmissionClass.EURO_5 : EmissionClass.EURO_4) : Set.of(),
                Objects.nonNull(transportType) ? Set.of(TransportType.valueOf(transportType)) : Set.of()))
                .isEqualTo(expectedExemption);
    }

    @Test
    void isExempt_noEmissionClass() {

        assertThat(catchThrowable(() -> emissionZoneExemption.isExempt(2d, null, Set.of(TransportType.CAR))))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void isExempt_noTransportTypes() {

        assertThat(catchThrowable(() -> emissionZoneExemption.isExempt(2d, Set.of(EmissionClass.EURO_5), null)))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void validate() {

        validate(emissionZoneExemption, List.of(), List.of());
    }

    @Test
    void validate_startTime_null() {

        emissionZoneExemption = emissionZoneExemption.withStartTime(null);
        validate(emissionZoneExemption, List.of("startTime"), List.of("must not be null"));
    }

    @Test
    void validate_endTime_null() {

        emissionZoneExemption = emissionZoneExemption.withEndTime(null);
        validate(emissionZoneExemption, List.of("endTime"), List.of("must not be null"));
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    void validate_emissionClasses_emptyOrNull(Set<EmissionClass> emissionClasses) {

        emissionZoneExemption = emissionZoneExemption.withEmissionClasses(emissionClasses);
        validate(emissionZoneExemption, List.of("emissionClasses"), List.of("must not be empty"));
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    void validate_transportTypes_emptyOrNull(Set<TransportType> transportTypes) {

        emissionZoneExemption = emissionZoneExemption.withTransportTypes(transportTypes);
        validate(emissionZoneExemption, List.of("transportTypes"), List.of("must not be empty"));
    }

    @Test
    void validate_vehicleWeightInKg_null() {

        emissionZoneExemption = emissionZoneExemption.withVehicleWeightInKg(null);
        validate(emissionZoneExemption, List.of("vehicleWeightInKg"), List.of("must not be null"));
    }

    @Override
    protected Class<?> getClassToTest() {
        return emissionZoneExemption.getClass();
    }
}