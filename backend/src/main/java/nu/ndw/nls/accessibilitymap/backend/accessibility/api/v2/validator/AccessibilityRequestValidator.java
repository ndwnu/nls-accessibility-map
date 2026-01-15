package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.validator;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import nu.ndw.nls.accessibilitymap.generated.model.v2.AccessibilityRequestJson;
import nu.ndw.nls.accessibilitymap.generated.model.v2.EmissionClassJson;
import nu.ndw.nls.accessibilitymap.generated.model.v2.FuelTypeJson;
import nu.ndw.nls.springboot.web.error.exceptions.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class AccessibilityRequestValidator {

    public boolean verify(AccessibilityRequestJson accessibilityRequest) {

        return meetsEmissionClassAndFuelTypeRequirements()
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
            if ((emissionClass == null && fuelTypes != null && !fuelTypes.isEmpty())
                || ((fuelTypes == null || fuelTypes.isEmpty()) && emissionClass != null)) {
                throw new ApiException(
                        UUID.randomUUID(),
                        HttpStatus.BAD_REQUEST,
                        "Invalid Request",
                        "If one of the environmental zone parameters is set, the other must be set as well.");
            }
            return true;
        };
    }
}

