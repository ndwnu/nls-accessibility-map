package nu.ndw.nls.accessibilitymap.job.speedlimits.client;

import io.github.resilience4j.retry.annotation.Retry;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.roadattributesapi.client.feign.generated.model.v1.SpeedLimitsRoadSectionResponseJson;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * The retry can not be placed on {@link SpeedLimitsClient} because resilience4j only matches methods declared on the annotated type.
 */
@Service
@RequiredArgsConstructor
public class SpeedLimitsService {

    private final SpeedLimitsClient speedLimitsClient;

    @Retry(name = "speedLimit")
    public ResponseEntity<SpeedLimitsRoadSectionResponseJson> getSpeedLimits(LocalDate nwbVersion, int page, int pageSize) {
        return speedLimitsClient.getSpeedLimits(nwbVersion, page, pageSize);
    }
}
