package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;

import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties.VehiclePropertiesBuilder;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.commands.model.CmdGenerateGeoJsonType;
import org.springframework.stereotype.Component;

@Component
public class VehicleTypeVehiclePropertiesMapper {

    /**
     * Mapper now fully relies on the {@link CmdGenerateGeoJsonType#getVehiclePropertiesConfigurer} method, but having
     * the mapper allows us to also support more complex mappings in the future
     *
     * @param type geojson generation type
     * @return properties used for determining accessibility
     */
    public VehicleProperties map(CmdGenerateGeoJsonType type) {
        VehiclePropertiesBuilder builder = VehicleProperties.builder();
        type.getVehiclePropertiesConfigurer().accept(builder);
        return builder.build();
    }

}
