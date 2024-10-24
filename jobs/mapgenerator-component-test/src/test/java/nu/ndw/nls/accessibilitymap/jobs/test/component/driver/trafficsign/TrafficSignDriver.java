package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.trafficsign;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonFeatureCollectionDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class TrafficSignDriver {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void stubTrafficSignRequest(Set<String> rvvCodes, List<TrafficSignGeoJsonDto> trafficSigns) {
        try {
            stubFor(
                    get(urlEqualTo(
                            "/api/rest/static-road-data/traffic-signs/v4/current-state%s%s%s"
                                    .formatted(
                                            "?status=PLACED",
                                            rvvCodes.stream()
                                                    .map("&rvvCode=%s"::formatted)
                                                    .collect(Collectors.joining()),
                                            "&countyCode=GM0307"
                                    )))
                            .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE))
                            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo("application/geo+json"))
                            .willReturn(aResponse()
                                    .withStatus(HttpStatus.OK.value())
                                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                    .withBody(objectMapper.writeValueAsString(
                                            TrafficSignGeoJsonFeatureCollectionDto.builder()
                                                    .features(trafficSigns)
                                                    .build()))));
        } catch (JsonProcessingException exception) {
            fail(exception);
        }
    }
}
