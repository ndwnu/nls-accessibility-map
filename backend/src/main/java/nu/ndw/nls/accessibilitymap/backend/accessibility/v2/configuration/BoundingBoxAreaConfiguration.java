package nu.ndw.nls.accessibilitymap.backend.accessibility.v2.configuration;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "nu.ndw.nls.accessibilitymap.bounding-box-area")
@NoArgsConstructor
@Validated
@Getter
@Setter
public class BoundingBoxAreaConfiguration {

    private static final double DEFAULT_SEARCH_MULTIPLIER = 1.5;

    private static final double DEFAULT_SEARCH_DISTANCE_GAP_FROM_REQUESTED_SEARCH_AREA_IN_METERS = 10_000.0;

    private double searchDistanceMultiplier = DEFAULT_SEARCH_MULTIPLIER;

    private double searchDistanceGapFromRequestedSearchAreaInMeters = DEFAULT_SEARCH_DISTANCE_GAP_FROM_REQUESTED_SEARCH_AREA_IN_METERS;
}
