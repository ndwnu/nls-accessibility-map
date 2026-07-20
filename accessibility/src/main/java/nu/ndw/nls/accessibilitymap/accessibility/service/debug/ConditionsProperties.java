package nu.ndw.nls.accessibilitymap.accessibility.service.debug;

import java.util.Set;
import lombok.Builder;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClass;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.Category;

@Builder
public record ConditionsProperties(Set<TransportType> transportTypes, Set<Category> categories, String timeValidity,
                                   EmissionClass emissionClass, FuelType fuelType, Double vehicleLengthInCm, Double vehicleHeightInCm,
                                   Double vehicleWidthInCm, Double vehicleWeightInKg, Double vehicleAxleLoadInKg) {
}
