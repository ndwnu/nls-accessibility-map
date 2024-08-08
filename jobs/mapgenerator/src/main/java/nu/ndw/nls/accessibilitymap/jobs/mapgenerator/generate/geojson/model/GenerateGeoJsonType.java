package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model;

import java.util.function.Consumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties.VehiclePropertiesBuilder;

@Getter
@RequiredArgsConstructor
public enum GenerateGeoJsonType {
    C6(b -> b.carAccessForbidden(true)),
    C7(b -> b.hgvAccessForbidden(true)),
    C7B(b -> b.hgvAndBusAccessForbidden(true)),
    C12(b -> b.motorVehicleAccessForbidden(true)),
    C22C(b -> b.lcvAndHgvAccessForbidden(true));

    private final Consumer<VehiclePropertiesBuilder> vehiclePropertiesConfigurer;

}
