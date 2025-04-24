package nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import nu.ndw.nls.springboot.test.util.validation.ValidationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExemptionTest extends ValidationTest {

    private Exemption exemption;

    @BeforeEach
    void setUp() {

        exemption = Exemption.builder()
                        .id("exemption")
                        .startTime(OffsetDateTime.parse("2022-03-11T09:00:00.000-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                        .endTime(OffsetDateTime.parse("2022-03-11T10:00:00.000-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                        .euroClassifications(Set.of(EuroClassification.EURO_1))
                        .vehicleWeightInKg(2)
                        .vehicleCategories(Set.of(VehicleCategory.M_1))
                        .build();
    }

    @Test
    void validate() {

        validate(exemption, List.of(), List.of());
    }

    @Test
    void validate_id_null() {

        exemption = exemption.withId(null);
        validate(exemption, List.of("id"), List.of("must not be null"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void validate_euroClassifications_null(Set<EuroClassification> euroClassifications) {

        exemption = exemption.withEuroClassifications(euroClassifications);
        validate(exemption, List.of("euroClassifications"), List.of("must not be empty"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void validate_vehicleCategories_null(Set<VehicleCategory> vehicleCategories) {

        exemption = exemption.withVehicleCategories(vehicleCategories);
        validate(exemption, List.of("vehicleCategories"), List.of("must not be empty"));
    }

    @Override
    protected Class<?> getClassToTest() {
        return exemption.getClass();
    }
}