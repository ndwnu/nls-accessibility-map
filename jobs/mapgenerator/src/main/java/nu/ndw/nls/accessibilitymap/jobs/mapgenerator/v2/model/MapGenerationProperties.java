package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.properties.GeoJsonProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.trafficsign.TrafficSignType;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Builder
@Validated
public final class MapGenerationProperties {

    private int exportVersion;

    private int nwbVersion;

    private boolean publishEvents;

    @NotNull
    private TrafficSignType trafficSignType;

    @NotNull
    private boolean includeOnlyTimeWindowedSigns;

    @NotNull
    private GeoJsonProperties geoJsonProperties;
}
