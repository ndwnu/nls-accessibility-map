package nu.ndw.nls.accessibilitymap.job.speedlimits.client;

import nu.ndw.nls.accessibilitymap.job.speedlimits.client.configuration.SpeedLimitsFeignClientConfiguration;
import nu.ndw.nls.roadattributesapi.client.feign.generated.api.v1.SpeedLimitsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "SpeedLimitsClient",
        url = "${nu.ndw.nls.roadattributesapi.speedLimits.url}",
        path = "${nu.ndw.nls.roadattributesapi.speedLimits.path:/api/rest/static-road-data/road-attributes/v1}",
        configuration = SpeedLimitsFeignClientConfiguration.class)
public interface SpeedLimitsClient extends SpeedLimitsApi {

}
