package nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClass;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import nu.ndw.nls.springboot.test.util.validation.ValidationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmissionZoneTest extends ValidationTest {

    private EmissionZone emissionZone;

    @Mock
    private OffsetDateTime timestamp;

    @Mock
    private Set<TransportType> transportTypes;

    @Mock
    private Set<EmissionClass> emissionClasses;

    @Mock
    private Set<FuelType> fuelTypes;

    @Mock
    private EmissionZoneRestriction restriction;

    @Mock
    private EmissionZoneExemption exemption;

    @BeforeEach
    void setUp() {

        emissionZone = EmissionZone.builder()
                .startTime(OffsetDateTime.MIN)
                .endTime(OffsetDateTime.MAX)
                .exemptions(Set.of(EmissionZoneExemption.builder()
                        .startTime(OffsetDateTime.MIN)
                        .endTime(OffsetDateTime.MAX)
                        .emissionClasses(Set.of(EmissionClass.EURO_5, EmissionClass.EURO_6))
                        .transportTypes(Set.of(TransportType.CAR, TransportType.TAXI))
                        .vehicleWeightInKg(Maximum.builder().value(3d).build())
                        .build()))
                .restriction(EmissionZoneRestriction.builder()
                        .id("id")
                        .vehicleWeightInKg(Maximum.builder().value(3d).build())
                        .fuelTypes(Set.of(FuelType.DIESEL, FuelType.ELECTRIC))
                        .transportTypes(Set.of(TransportType.CAR, TransportType.TAXI))
                        .build())
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

        emissionZone = emissionZone
                .withStartTime(OffsetDateTime.parse("2022-03-11T09:00:00Z"))
                .withEndTime(OffsetDateTime.parse("2022-03-11T10:00:00Z"));

        assertThat(emissionZone.isActive(now)).isEqualTo(expectedActive);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void isRelevant(boolean expectedRelevant) {

        when(restriction.isRelevant(2d, fuelTypes, transportTypes)).thenReturn(expectedRelevant);

        emissionZone = emissionZone.withRestriction(restriction);

        assertThat(emissionZone.isRelevant(2d, fuelTypes, transportTypes)).isEqualTo(expectedRelevant);
    }

    @Test
    void isRelevant_noFuelTypes() {

        assertThat(catchThrowable(() -> emissionZone.isRelevant(2d, null, transportTypes)))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void isRelevant_noTransportTypes() {

        assertThat(catchThrowable(() -> emissionZone.isRelevant(2d, fuelTypes, null)))
                .isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            true,   true,   true
            false,  false,  false
            true,   false,  false
            false,  true,   false
            """)
    void isExempt(boolean isActive, boolean isExempt, boolean expectedRelevant) {

        when(exemption.isActive(timestamp)).thenReturn(isActive);
        if (isActive) {
            when(exemption.isExempt(2d, emissionClasses, transportTypes)).thenReturn(isExempt);
        }

        emissionZone = emissionZone.withExemptions(Set.of(exemption));

        assertThat(emissionZone.isExempt(timestamp, 2d, emissionClasses, transportTypes)).isEqualTo(expectedRelevant);
    }

    @Test
    void isExempt_noTimestamp() {

        assertThat(catchThrowable(() -> emissionZone.isExempt(null, 2d, emissionClasses, transportTypes)))
                .isInstanceOf(NullPointerException.class);
    }
    @Test
    void isExempt_noFuelTypes() {

        assertThat(catchThrowable(() -> emissionZone.isExempt(timestamp, 2d, null, transportTypes)))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void isExempt_noTransportTypes() {

        assertThat(catchThrowable(() -> emissionZone.isExempt(timestamp, 2d, emissionClasses, null)))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void validate() {

        validate(emissionZone, List.of(), List.of());
    }

    @Test
    void validate_startTime_null() {

        emissionZone = emissionZone.withStartTime(null);
        validate(emissionZone, List.of("startTime"), List.of("must not be null"));
    }

    @Test
    void validate_endTime_null() {

        emissionZone = emissionZone.withEndTime(null);
        validate(emissionZone, List.of("endTime"), List.of("must not be null"));
    }

    @Test
    void validate_exemptions_null() {

        emissionZone = emissionZone.withExemptions(null);
        validate(emissionZone, List.of("exemptions"), List.of("must not be null"));
    }

    @Test
    void validate_restriction_null() {

        emissionZone = emissionZone.withRestriction(null);
        validate(emissionZone, List.of("restriction"), List.of("must not be null"));
    }

    @Override
    protected Class<?> getClassToTest() {
        return emissionZone.getClass();
    }
}