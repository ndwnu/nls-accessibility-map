package nu.ndw.nls.accessibilitymap.accessibility.model;

import lombok.Builder;

@Builder
public record VehicleProperties(Double length, Double width, Double height, Double axleLoad, Double weight,
                                boolean carAccessForbidden, boolean hgvAccessForbidden, boolean busAccessForbidden,
                                boolean hgvAndBusAccessForbidden, boolean tractorAccessForbidden,
                                boolean slowVehicleAccessForbidden, boolean trailerAccessForbidden,
                                boolean motorcycleAccessForbidden, boolean motorVehicleAccessForbidden,
                                boolean lcvAndHgvAccessForbidden, boolean carAccessForbiddenWt,
                                boolean hgvAccessForbiddenWt, boolean hgvAndBusAccessForbiddenWt,
                                boolean motorVehicleAccessForbiddenWt, boolean lcvAndHgvAccessForbiddenWt) {

}
