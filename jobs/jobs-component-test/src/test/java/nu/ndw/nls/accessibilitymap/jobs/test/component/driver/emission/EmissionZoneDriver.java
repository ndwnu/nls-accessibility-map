package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.emission;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

import lombok.RequiredArgsConstructor;
import nu.ndw.nls.springboot.test.component.util.data.TestDataProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class EmissionZoneDriver {

    private final TestDataProvider testDataProvider;

    public void stubEmissionZone() {

        stubFor(
                get(urlPathMatching(
                        "/api/area/ibbm/emission-zones/v1"))
                        .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE))
                        .willReturn(aResponse()
                                .withStatus(HttpStatus.OK.value())
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .withBody(testDataProvider.readFromFile("emissionZoneApi", "response.json"))));

    }
}
