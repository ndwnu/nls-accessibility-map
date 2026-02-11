package nu.ndw.nls.accessibilitymap.job.trafficsign.emission.client;

import io.github.resilience4j.retry.annotation.Retry;
import jakarta.validation.Valid;
import java.util.List;
import nu.ndw.nls.accessibilitymap.job.trafficsign.emission.client.configuration.EmissionZoneFeignClientConfiguration;
import nu.ndw.nls.accessibilitymap.job.trafficsign.emission.dto.EmissionZone;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        value = "EmissionZoneClient",
        url = "${nu.ndw.nls.accessibilitymap.trafficsigns.emission-zone.client.url}",
        configuration = EmissionZoneFeignClientConfiguration.class)
@Retry(name = "emissionZone")
@Validated
public interface EmissionZoneClient {

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Valid
    List<EmissionZone> findAll();
}
