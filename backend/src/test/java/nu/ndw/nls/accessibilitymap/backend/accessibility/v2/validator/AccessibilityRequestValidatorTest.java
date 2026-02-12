package nu.ndw.nls.accessibilitymap.backend.accessibility.v2.validator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.AccessibilityRequestJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.BoundingBoxAreaRequestJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.EmissionClassJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.FuelTypeJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.LocationJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.VehicleCharacteristicsJson;
import nu.ndw.nls.springboot.web.error.exceptions.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class AccessibilityRequestValidatorTest {

    private AccessibilityRequestValidator accessibilityRequestValidator;

    @BeforeEach
    void setUp() {

        accessibilityRequestValidator = new AccessibilityRequestValidator();
    }

    @Test
    void verify() {
        AccessibilityRequestJson accessibilityRequestJson = AccessibilityRequestJson.builder()
                .vehicle(VehicleCharacteristicsJson.builder()
                        .emissionClass(EmissionClassJson.ZERO)
                        .fuelTypes(List.of(FuelTypeJson.ELECTRIC))
                        .build())
                .build();

        assertThat(accessibilityRequestValidator.verify(accessibilityRequestJson)).isTrue();
    }

    @Test
    void verify_missingEmissionClass() {
        AccessibilityRequestJson accessibilityRequestJson = AccessibilityRequestJson.builder()
                .vehicle(VehicleCharacteristicsJson.builder()
                        .emissionClass(null)
                        .fuelTypes(List.of(FuelTypeJson.ELECTRIC))
                        .build())
                .build();

        try {
            accessibilityRequestValidator.verify(accessibilityRequestJson);
        } catch (ApiException exception) {

            assertThat(exception.getErrorId()).isEqualTo(UUID.fromString("06d84c7c-7be2-4f79-a8fd-00264f06267d"));
            assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(exception.getTitle()).isEqualTo("Invalid Request");
            assertThat(exception.getDescription())
                    .isEqualTo("If one of the environmental zone parameters is set, the other must be set as well.");
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    void verify_missingFuelTypes(List<FuelTypeJson> fuelTypes) {
        AccessibilityRequestJson accessibilityRequestJson = AccessibilityRequestJson.builder()
                .vehicle(VehicleCharacteristicsJson.builder()
                        .emissionClass(EmissionClassJson.ZERO)
                        .fuelTypes(fuelTypes)
                        .build())
                .build();

        try {
            accessibilityRequestValidator.verify(accessibilityRequestJson);
        } catch (ApiException exception) {

            assertThat(exception.getErrorId()).isEqualTo(UUID.fromString("06d84c7c-7be2-4f79-a8fd-00264f06267d"));
            assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(exception.getTitle()).isEqualTo("Invalid Request");
            assertThat(exception.getDescription())
                    .isEqualTo("If one of the environmental zone parameters is set, the other must be set as well.");
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    void verify_missingFuelTypesAndEmissionClass(List<FuelTypeJson> fuelTypes) {
        AccessibilityRequestJson accessibilityRequestJson = AccessibilityRequestJson.builder()
                .vehicle(VehicleCharacteristicsJson.builder()
                        .emissionClass(null)
                        .fuelTypes(fuelTypes)
                        .build())
                .build();

        try {
            accessibilityRequestValidator.verify(accessibilityRequestJson);
        } catch (ApiException exception) {

            assertThat(exception.getErrorId()).isEqualTo(UUID.fromString("06d84c7c-7be2-4f79-a8fd-00264f06267d"));
            assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(exception.getTitle()).isEqualTo("Invalid Request");
            assertThat(exception.getDescription())
                    .isEqualTo("If one of the environmental zone parameters is set, the other must be set as well.");
        }
    }


    @Test
    void verify_boundingBoxAreaRequestFromMustBeDefined() {
        AccessibilityRequestJson accessibilityRequestJson = AccessibilityRequestJson.builder()
                .vehicle(VehicleCharacteristicsJson.builder()
                        .emissionClass(EmissionClassJson.ZERO)
                        .fuelTypes(List.of(FuelTypeJson.ELECTRIC))
                        .build())
                .area(BoundingBoxAreaRequestJson.builder().build())
                .from(LocationJson.builder().build())
                .build();

        assertThat(accessibilityRequestValidator.verify(accessibilityRequestJson)).isTrue();
    }

    @Test
    void verify_boundingBoxAreaRequestFromMustBeDefined_fromNotDefined() {
        AccessibilityRequestJson accessibilityRequestJson = AccessibilityRequestJson.builder()
                .vehicle(VehicleCharacteristicsJson.builder()
                        .emissionClass(EmissionClassJson.ZERO)
                        .fuelTypes(List.of(FuelTypeJson.ELECTRIC))
                        .build())
                .area(BoundingBoxAreaRequestJson.builder().build())
                .build();

        try {
            accessibilityRequestValidator.verify(accessibilityRequestJson);
        } catch (ApiException exception) {

            assertThat(exception.getErrorId()).isEqualTo(UUID.fromString("43b85771-22ad-490b-95c8-e659fb3fc915"));
            assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(exception.getTitle()).isEqualTo("Invalid Request");
            assertThat(exception.getDescription())
                    .isEqualTo("When using a bounding box area request, the from parameter must also be set.");
        }
    }

}
