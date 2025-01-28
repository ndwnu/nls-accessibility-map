package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.issue;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

import com.github.tomakehurst.wiremock.extension.Parameters;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.springboot.test.component.util.data.TestDataProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IssueDriver {

    private final TestDataProvider testDataProvider;

    public void stubIssueApiRequest() {

        Map<String, Map<String, String>> jwtMatcherParameters = Map.of(
                "header", Map.of("alg", "RS256", "typ", "JWT"),
                "payload", Map.of("clientId", "nls-accessibility-map-api-service-account")
        );

        stubFor(post(urlPathMatching(
                "/api/rest/static-road-data/location-data-issues/v1/issues"))
                .andMatching("jwt-matcher", Parameters.of(jwtMatcherParameters))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(HttpStatus.ACCEPTED.value())
                        .withBody("{{request.body}}")));

        stubFor(post(urlPathMatching(
                "/api/rest/static-road-data/location-data-issues/v1/report/complete"))
                .andMatching("jwt-matcher", Parameters.of(jwtMatcherParameters))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(HttpStatus.ACCEPTED.value())));
    }

    public void verifyIssueCreated(String issueFile) {

        String issueBody = testDataProvider.readFromFile("issue", issueFile + ".json");

        verify(postRequestedFor(urlEqualTo("/api/rest/static-road-data/location-data-issues/v1/issues"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson(issueBody)));
    }

    public void verifyNumberOfCreatedIssues(int numberOfIssues) {

        verify(numberOfIssues, postRequestedFor(urlEqualTo("/api/rest/static-road-data/location-data-issues/v1/issues"))
                .withHeader("Content-Type", equalTo("application/json")));
    }

    public void verifyReportComplete(Set<String> trafficSigns) {

        verify(postRequestedFor(urlEqualTo("/api/rest/static-road-data/location-data-issues/v1/report/complete"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("""
                        {
                          "reporterReportId": "${json-unit.ignore}",
                          "reporterReportGroupId": "AsymmetricTrafficSignPlacement-%s"
                        }
                        """.formatted(String.join("-", trafficSigns)))));
    }
}
