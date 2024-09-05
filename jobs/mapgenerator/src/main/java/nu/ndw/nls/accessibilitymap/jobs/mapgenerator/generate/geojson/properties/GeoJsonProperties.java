package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import nu.ndw.nls.events.NlsEventSubjectType;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
public class GeoJsonProperties {

    @NotBlank
    private String pathDatePattern;

    @NotBlank
    private String rvvCode;

    @NotNull
    private NlsEventSubjectType publisherEventSubjectType;

    @NotNull
    private GenerationProperties generation;


    enum AccessibleTrafficSignOfInaccessibility {
        OUTPUT_AS_10M_LINE_STRINGS,
        OUTPUT_AS_POINTS_WITH_BEARING_PROPERTY,
    }

    @Data
    @Validated
    public static class GenerationProperties {

        /**
         * When true, will include the accessible line strings
         */
        private boolean includeAcccessible;

        /**
         * When true, will include the inaccessible line strings
         */
        private boolean includeInaccessible;

        /**
         * When true, the output contains line strings that show the accessibility in the direction of the
         * forward or backward NWB road section.
         */
        private boolean outputDirectionality;

        /**
         * Some traffic signs cause one direction to be inaccessible, but the road section itself is still accessible
         * from the other direction. With this parameter we control how to output the end result:
         * - As 10 meter LineString, this visually shows the existence of the traffic sign and also includes the
         *   direction (bearing)
         * - As point, in this case the bearing should be added to make it easier for use when map matching the point
         *   into a map
         */
        private AccessibleTrafficSignOfInaccessibility type;

    }
}
