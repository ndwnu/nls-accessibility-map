package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model;

import java.util.function.Consumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties.VehiclePropertiesBuilder;

@Getter
@RequiredArgsConstructor
public enum GenerateGeoJsonType {
    C6(b -> b.carAccessForbiddenWt(true)),
    C7(b -> b.hgvAccessForbiddenWt(true)),
    C7B(b -> b.hgvAndBusAccessForbiddenWt(true)),
    C12(b -> b.motorVehicleAccessForbiddenWt(true)),
    C22C(b -> b.lcvAndHgvAccessForbiddenWt(true));

    private final Consumer<VehiclePropertiesBuilder> vehiclePropertiesConfigurer;

}
