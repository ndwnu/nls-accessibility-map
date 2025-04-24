package nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import nu.ndw.nls.springboot.test.util.validation.ValidationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmissionZoneTest extends ValidationTest {

    private EmissionZone emissionZone;

    @BeforeEach
    void setUp() {

        emissionZone = EmissionZone.builder()
                .id("id")
                .type(EmissionZoneType.LOW_EMISSION_ZONE)
                .startTime(OffsetDateTime.parse("2022-03-11T09:00:00.000-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .endTime(OffsetDateTime.parse("2022-03-11T10:00:00.000-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .status(EmissionZoneStatus.ACTIVE)
                .exemptions(List.of(Exemption.builder()
                        .id("exemption")
                        .startTime(OffsetDateTime.parse("2022-03-11T09:00:00.000-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                        .endTime(OffsetDateTime.parse("2022-03-11T10:00:00.000-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                        .euroClassifications(Set.of(EuroClassification.EURO_1))
                        .vehicleWeightInKg(2)
                        .vehicleCategories(Set.of(VehicleCategory.M_1))
                        .build()))
                .restriction(Restriction.builder()
                        .id("restrictionId")
                        .vehicleCategories(Set.of(VehicleCategory.M_1))
                        .fuelType(FuelType.DIESEL)
                        .vehicleType(VehicleType.CAR)
                        .build())
                .build();
    }

    @ParameterizedTest
    @EnumSource(value = EmissionZoneStatus.class, names = "ACTIVE", mode = Mode.INCLUDE)
    void isActive(EmissionZoneStatus status) {

        emissionZone = emissionZone.withStatus(status);

        assertThat(emissionZone.isActive()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = EmissionZoneStatus.class, names = "ACTIVE", mode = EnumSource.Mode.EXCLUDE)
    void isActive_notActive(EmissionZoneStatus status) {

        emissionZone = emissionZone.withStatus(status);

        assertThat(emissionZone.isActive()).isFalse();
    }
    @Test
    void validate() {

        validate(emissionZone, List.of(), List.of());
    }

    @Test
    void validate_id_null() {

        emissionZone = emissionZone.withId(null);
        validate(emissionZone, List.of("id"), List.of("must not be null"));
    }

    @Test
    void validate_type_null() {

        emissionZone = emissionZone.withType(null);
        validate(emissionZone, List.of("type"), List.of("must not be null"));
    }

    @Test
    void validate_startTime_null() {

        emissionZone = emissionZone.withStartTime(null);
        validate(emissionZone, List.of("startTime"), List.of("must not be null"));
    }

    @Test
    void validate_status_null() {

        emissionZone = emissionZone.withStatus(null);
        validate(emissionZone, List.of("status"), List.of("must not be null"));
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