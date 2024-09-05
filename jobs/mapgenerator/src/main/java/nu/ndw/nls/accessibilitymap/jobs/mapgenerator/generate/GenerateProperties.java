package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.nio.file.Path;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.properties.GeoJsonProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.commands.model.CmdGenerateGeoJsonType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix ="nu.ndw.nls.accessibilitymap.jobs.generate")
public class GenerateProperties {

    @NotNull
    private Path rootGenerationDestination;

    @NotNull
    private Map<CmdGenerateGeoJsonType, GeoJsonProperties> geojson;

    @Min(50)
    @Max(54)
    private double startLocationLatitude;

    @Min(3)
    @Max(8)
    private double startLocationLongitude;

    @Min(1)
    private double searchDistanceInMeters;

    /**
     * When true, will instruct the object mapper to pretty print the output. Usually
     * only enabled when developing the application
     */
    private boolean prettyPrintJson;

}
