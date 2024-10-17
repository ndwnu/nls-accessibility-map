package nu.ndw.nls.accessibilitymap.trafficsignclient.repositories;

import java.util.Set;
import nu.ndw.nls.accessibilitymap.config.ClientConfiguration;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.CurrentStateStatus;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonFeatureCollectionDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name="${nu.ndw.nls.accessibilitymap.trafficsignclient.api:trafficsignclient}",
        url="${nu.ndw.nls.accessibilitymap.trafficsignclient.api.url:"
                + "http://localhost:8888/api/rest/static-road-data/traffic-signs/v4}",
        configuration = ClientConfiguration.class)
public interface TrafficSignRepository {

    @GetMapping(
            value = "/current-state",
            produces = {  MediaType.APPLICATION_JSON_VALUE },
            consumes = { "application/geo+json" }
    )
    TrafficSignGeoJsonFeatureCollectionDto findCurrentState(
            @RequestParam(value = "status", required = false) CurrentStateStatus currentStateStatus,
            @RequestParam(value = "rvvCode", required = false) Set<String> rvvCodes,
            @RequestParam(value = "roadSectionId", required = false) Set<Long> roadSectionIds,
            @RequestParam(value = "countyCode", required = false) Set<String> countyCodes);
}