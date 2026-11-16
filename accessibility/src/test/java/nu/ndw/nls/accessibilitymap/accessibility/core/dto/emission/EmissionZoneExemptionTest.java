package nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;

class EmissionZoneExemptionTest extends ValidationTest {

    private EmissionZoneExemption emissionZoneExemption;

    @BeforeEach
    void setUp() {

        emissionZoneExemption = EmissionZoneExemption.builder()
                .startTime(OffsetDateTime.MIN)
                .endTime(OffsetDateTime.MAX)
                .emissionClasses(Set.of(EmissionClass.EURO_5, EmissionClass.EURO_6))
                .transportTypes(Set.of(TransportType.CAR, TransportType.TAXI))
                .build();
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            now,                    expected active
            2022-03-11T08:59:59Z,   false
            2022-03-11T09:00:00Z,   true
            2022-03-11T09:59:59Z,   true
            2022-03-11T10:00:00Z,   false
            """)
    void isActive(OffsetDateTime now, boolean expectedActive) {

        emissionZoneExemption = emissionZoneExemption
                .withStartTime(OffsetDateTime.parse("2022-03-11T09:00:00Z"))
                .withEndTime(OffsetDateTime.parse("2022-03-11T10:00:00Z"));

        assertThat(emissionZoneExemption.isActive(now)).isEqualTo(expectedActive);
    }

    @ParameterizedTest
    @CsvSource(nullValues = "null", useHeadersInDisplayName = true, textBlock = """
            vehicle weight, exemption maximum weight,   emission class, transport type, expected exempt
            2,              3,                          EURO_5,         CAR,            true
            3,              3,                          EURO_5,         CAR,            true
            4,              3,                          EURO_5,         CAR,            false
            2,              3,                          EURO_6,         CAR,            true
            2,              3,                          EURO_4,         CAR,            false
            2,              3,                          EURO_5,         BUS,            false
            null,           3,                          EURO_5,         CAR,            true
            2,              null,                       EURO_5,         CAR,            true
            null,           null,                       EURO_5,         CAR,            true
            2,              3,                          null,           CAR,            true
            2,              3,                          EURO_5,         null,           true
            """)
    void isExempt(
            Double vehicleWeightInKg,
            Double exemptionMaximumVehicleWeightInKg,
            EmissionClass emissionClass,
            TransportType transportType,
            boolean expectedExempt) {

        emissionZoneExemption = emissionZoneExemption.withVehicleWeightInKg(Objects.nonNull(exemptionMaximumVehicleWeightInKg)
                ? Maximum.builder().value(exemptionMaximumVehicleWeightInKg).build()
                : null);

        assertThat(emissionZoneExemption.isExempt(
                vehicleWeightInKg,
                Objects.nonNull(emissionClass) ? Set.of(emissionClass) : Set.of(),
                Objects.nonNull(transportType) ? Set.of(transportType) : Set.of()))
                .isEqualTo(expectedExempt);
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

    @Override
    protected Class<?> getClassToTest() {
        return emissionZoneExemption.getClass();
    }
}
