package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Builder
@Validated
public final class MapGenerationProperties {

    private int exportVersion;

    private int nwbVersion;

    @Min(1)
    @Default
    private int trafficSignsRequestBatchSize = 500;

    @NotNull
    @Default
    private final Set<TrafficSignType> trafficSigns = new HashSet<>();
}
