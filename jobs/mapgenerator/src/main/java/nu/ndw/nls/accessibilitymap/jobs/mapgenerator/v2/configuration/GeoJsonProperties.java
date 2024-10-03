package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.configuration;

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

    @NotNull
    private NlsEventSubjectType publisherEventSubjectType;

    @NotNull
    private GenerationProperties generation;

    @Data
    @Validated
    public static class GenerationProperties {

        /**
         * When true, will include the accessible line strings
         */
        private boolean includeAccessible;

        /**
         * When true, will include the inaccessible line strings
         */
        private boolean includeInaccessible;

        /**
         * When true, the output contains line strings that show the accessibility in the direction of the
         * forward or backward NWB road section.
         */
        private boolean outputDirectionality;
    }
}
