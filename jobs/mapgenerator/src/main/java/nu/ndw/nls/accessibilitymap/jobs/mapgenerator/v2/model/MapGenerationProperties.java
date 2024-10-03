package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.Setter;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.trafficsign.TrafficSignType;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Builder
@Validated
public final class MapGenerationProperties {

    private int exportVersion;

    private int nwbVersion;

    private boolean produceMessage;

    @Min(1)
    @Default
    private int trafficSignsRequestBatchSize = 500;

    @NotNull
    @Default
    @Min(1)
    @Max(value = 1, message = "We only support max one trafficSign because of older parts of the code cannot deal with multiple traffic signs at the same time.")
    private final Set<TrafficSignType> trafficSigns = new HashSet<>();
}
