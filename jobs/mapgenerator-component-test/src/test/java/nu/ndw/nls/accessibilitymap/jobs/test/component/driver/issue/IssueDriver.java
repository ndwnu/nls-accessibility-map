package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.issue;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import java.util.Map;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class IssueDriver {

    ObjectMapper objectMapper = new ObjectMapper();

    public void stubIssueApiRequest() {

        Map<String, Map<String, String>> jwtMatcherParameters = Map.of(
                "header", Map.of("alg", "RS256", "typ", "JWT"),
                "payload", Map.of("clientId", "nls-accessibility-map-api-service-account")
        );
        stubFor(
                post(urlPathMatching(
                        "/api/rest/static-road-data/location-data-issues/v1/issues"))
                        .andMatching("jwt-matcher", Parameters.of(jwtMatcherParameters))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withStatus(HttpStatus.ACCEPTED.value())
                                .withBody("{{request.body}}")));
    }

    @SneakyThrows
    public int getNumberOfIssuesCreated() {

        Unirest.setTimeouts(0, 0);
        HttpResponse<String> response = Unirest.post("http://localhost:8888/__admin/requests/count")
                .header("Content-Type", "application/json")
                .body("""
                        {
                          "method": "POST",
                          "urlPathPattern": "/api/rest/static-road-data/location-data-issues/v1/issues"
                          }
                        }""")
                .asString();

        Count count = objectMapper.readValue(response.getBody(), Count.class);
        return count.count();
    }

    private record Count(int count, boolean requestJournalDisabled) {

    }
}
