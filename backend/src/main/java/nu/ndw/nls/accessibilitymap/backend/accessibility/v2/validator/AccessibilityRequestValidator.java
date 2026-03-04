package nu.ndw.nls.accessibilitymap.backend.accessibility.v2.validator;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.AccessibilityRequestJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.BoundingBoxAreaRequestJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.EmissionClassJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.FuelTypeJson;
import nu.ndw.nls.springboot.web.error.exceptions.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class AccessibilityRequestValidator {

    public boolean verify(AccessibilityRequestJson accessibilityRequest) {

        return meetsEmissionClassAndFuelTypeRequirements()
                .and(whenBoundingBoxAreaRequestFromMustBeDefined())
                .test(accessibilityRequest);
    }

    /**
     * Ensures that the parameters related to environmental zone restrictions are consistent. If one of the parameters is set and the other
     * is not, an exception is thrown.
     */
    @SuppressWarnings("java:S1067")
    private Predicate<AccessibilityRequestJson> meetsEmissionClassAndFuelTypeRequirements() {

        return accessibilityRequest -> {
            EmissionClassJson emissionClass = accessibilityRequest.getVehicle().getEmissionClass();
            List<FuelTypeJson> fuelTypes = accessibilityRequest.getVehicle().getFuelTypes();
            if ((Objects.isNull(emissionClass) && !CollectionUtils.isEmpty(fuelTypes))
                || (CollectionUtils.isEmpty(fuelTypes) && Objects.nonNull(emissionClass))) {
                throw new ApiException(
                        UUID.fromString("8091b9dc-c15f-4b74-8b45-0bf7605042be"),
                        HttpStatus.BAD_REQUEST,
                        "Invalid Request",
                        "If one of the environmental zone parameters is set, the other must be set as well.");
            }
            return true;
        };
    }

    @SuppressWarnings("java:S1067")
    private Predicate<AccessibilityRequestJson> whenBoundingBoxAreaRequestFromMustBeDefined() {

        return accessibilityRequest -> {
            if (accessibilityRequest.getArea() instanceof BoundingBoxAreaRequestJson
                && Objects.isNull(accessibilityRequest.getFrom())) {
                throw new ApiException(
                        UUID.fromString("43b85771-22ad-490b-95c8-e659fb3fc915"),
                        HttpStatus.BAD_REQUEST,
                        "Invalid Request",
                        "When using a bounding box area request, the from parameter must also be set.");
            }
            return true;
        };
    }
}

