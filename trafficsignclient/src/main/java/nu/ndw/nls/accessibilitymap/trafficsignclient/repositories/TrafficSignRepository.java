package nu.ndw.nls.accessibilitymap.trafficsignclient.repositories;

import com.fasterxml.jackson.core.JsonPointer;
import io.micrometer.common.util.StringUtils;
import java.net.URI;
import java.util.Set;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.trafficsignclient.TrafficSignConfiguration;
import nu.ndw.nls.accessibilitymap.trafficsignclient.TrafficSignProperties;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.CurrentStateStatus;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Flux;

@Repository
@RequiredArgsConstructor
public class TrafficSignRepository {

    private static final String QUERY_PARAM_TOWN_CODE = "townCode";
    private static final String QUERY_PARAM_STATUS = "status";
    private static final String QUERY_PARAM_RVV_CODE = "rvvCode";
    private static final String APPLICATION_GEO_JSON = "application/geo+json";
    private static final String FEATURES_JSON_POINTER = "/features";

    private final TrafficSignConfiguration trafficSignConfiguration;
    private final TrafficSignProperties trafficSignProperties;

    private final StreamingJsonBodyExtractor streamingJsonBodyExtractor;

    public Stream<TrafficSignGeoJsonDto> findCurrentState(CurrentStateStatus currentStateStatus,
            Set<String> rvvCodes) {

        return trafficSignConfiguration.getWebClient()
                .get()
                .uri(uriBuilder -> buildUri(uriBuilder, currentStateStatus, rvvCodes))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.valueOf(APPLICATION_GEO_JSON))
                .exchangeToFlux(response -> response
                        .body(streamingJsonBodyExtractor
                                .toFlux(TrafficSignGeoJsonDto.class, JsonPointer.compile(FEATURES_JSON_POINTER)))
                        .onErrorResume(e -> Flux.empty()))
                .toStream();
    }

    private URI buildUri(UriBuilder uriBuilder, CurrentStateStatus currentStateStatus, Set<String> rvvCodes) {
        UriBuilder builder = uriBuilder.path(trafficSignProperties.getApi().getCurrentStatePath());
        if (currentStateStatus != null) {
            builder.queryParam(QUERY_PARAM_STATUS, currentStateStatus);
        }
        rvvCodes.stream()
                .filter(StringUtils::isNotBlank)
                .forEach(rvvCode -> builder.queryParam(QUERY_PARAM_RVV_CODE, rvvCode));

        if (StringUtils.isNotBlank(trafficSignProperties.getApi().getTownCode())) {
            builder.queryParam(QUERY_PARAM_TOWN_CODE, trafficSignProperties.getApi().getTownCode());
        }
        return builder.build();
    }

}