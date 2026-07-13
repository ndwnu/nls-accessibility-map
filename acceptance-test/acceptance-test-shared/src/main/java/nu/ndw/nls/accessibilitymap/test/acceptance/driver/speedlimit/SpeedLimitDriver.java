package nu.ndw.nls.accessibilitymap.test.acceptance.driver.speedlimit;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static nu.ndw.nls.accessibilitymap.test.acceptance.driver.oauth.OAuthDriver.SIMULATED_BEARER_TOKEN;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.speedlimit.dto.SpeedLimit;
import nu.ndw.nls.roadattributesapi.client.feign.generated.model.v1.DirectionEnumJson;
import nu.ndw.nls.roadattributesapi.client.feign.generated.model.v1.RoadSectionDirectionalSpeedLimitJson;
import nu.ndw.nls.roadattributesapi.client.feign.generated.model.v1.RoadSectionSpeedLimitJson;
import nu.ndw.nls.roadattributesapi.client.feign.generated.model.v1.SpeedLimitsRoadSectionResponseJson;
import nu.ndw.nls.springboot.test.component.driver.job.JobDriver;
import nu.ndw.nls.springboot.test.component.state.StateManagement;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@RequiredArgsConstructor
@Component
public class SpeedLimitDriver implements StateManagement {

    private final JsonMapper jsonMapper = new JsonMapper();

    private final JobDriver jobDriver;

    @SuppressWarnings("java:S3658")
    public void stubSpeedLimits(List<SpeedLimit> speedLimits, int nwbVersion) {

        SpeedLimitsRoadSectionResponseJson stubbedSpeedLimits = new SpeedLimitsRoadSectionResponseJson()
                .speedLimits(speedLimits.stream()
                        .map(speedLimit ->
                                new RoadSectionSpeedLimitJson()
                                        .nwbRoadSectionId(speedLimit.roadSectionId())
                                        .directionalSpeedLimit(
                                                buildDirectionalSpeedLimit(speedLimit)
                                        ))
                        .toList());

        stubFor(
                get(urlPathMatching(
                        "/api/rest/static-road-data/road-attributes/v1/speed-limits/%s".formatted(
                                LocalDate.parse(nwbVersion + "", DateTimeFormatter.ofPattern("yyyyMMdd"))
                                        .format(DateTimeFormatter.ISO_LOCAL_DATE))))
                        .withQueryParam("page", equalTo("0"))
                        .withQueryParam("size", equalTo("1000"))
                        .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE))
                        .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer %s".formatted(SIMULATED_BEARER_TOKEN)))
                        .willReturn(aResponse()
                                .withStatus(HttpStatus.OK.value())
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .withBody(jsonMapper.writeValueAsString(stubbedSpeedLimits))));
    }

    private List<@Valid RoadSectionDirectionalSpeedLimitJson> buildDirectionalSpeedLimit(SpeedLimit speedLimit) {

        return Stream.of(
                        speedLimit.hasForwardDirection()
                                ? new RoadSectionDirectionalSpeedLimitJson()
                                .direction(DirectionEnumJson.FORWARD)
                                .averageSpeedLimit(speedLimit.forwardAverageSpeedLimit())
                                : null,
                        speedLimit.hasBackwardDirection()
                                ? new RoadSectionDirectionalSpeedLimitJson()
                                .direction(DirectionEnumJson.BACKWARD)
                                .averageSpeedLimit(speedLimit.backwardAverageSpeedLimit())
                                : null
                )
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public void clearState() {
        // create empty rebuild speed limit cache
        jobDriver.run("job", "rebuildSpeedLimitCache");
    }
}
