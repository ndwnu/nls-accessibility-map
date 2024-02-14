package nu.ndw.nls.accessibilitymap.trafficsignclient.repositories;

import io.micrometer.common.util.StringUtils;
import java.net.URI;
import java.util.Set;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.trafficsignclient.TrafficSignConfiguration;
import nu.ndw.nls.accessibilitymap.trafficsignclient.TrafficSignProperties;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.CurrentStateStatus;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignJsonDtoV3;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.util.UriBuilder;

@Repository
@RequiredArgsConstructor
public class TrafficSignRepository {

    private static final String QUERY_PARAM_TOWN_CODE = "town-code";
    private static final String QUERY_PARAM_STATUS = "status";
    private static final String QUERY_PARAM_RVV_CODE = "rvv-code";

    private final TrafficSignConfiguration trafficSignConfiguration;

    private final TrafficSignProperties trafficSignProperties;

    public Stream<TrafficSignJsonDtoV3> findCurrentState(CurrentStateStatus currentStateStatus, Set<String> rvvCodes) {
        return trafficSignConfiguration.getWebClient()
                .get()
                .uri(uriBuilder -> buildUri(uriBuilder, currentStateStatus, rvvCodes))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToFlux(TrafficSignJsonDtoV3.class)
                .toStream();
    }

    private URI buildUri(UriBuilder uriBuilder, CurrentStateStatus currentStateStatus, Set<String> rvvCodes) {
        UriBuilder builder = uriBuilder.path(trafficSignProperties.getApi().getCurrentStatePath());
        if (currentStateStatus != null) {
            builder.queryParam(QUERY_PARAM_STATUS, currentStateStatus);
        }
        rvvCodes.forEach(rvvCode -> {
            if (StringUtils.isNotBlank(rvvCode)) {
                builder.queryParam(QUERY_PARAM_RVV_CODE, rvvCode);
            }

        });
        if (StringUtils.isNotBlank(trafficSignProperties.getApi().getTownCode())) {
            builder.queryParam(QUERY_PARAM_TOWN_CODE, trafficSignProperties.getApi().getTownCode());
        }
        return builder.build();
    }
}
