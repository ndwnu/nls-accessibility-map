package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.oauth;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.springboot.test.component.state.StateManagement;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuthDriver implements StateManagement {

    public static final String SIMULATED_BEARER_TOKEN = "MTQ0NjJkZmQ5OTM2NDE1ZTZjNGZmZjI3";

    public void prepareTokenRetrieval() {

        stubFor(WireMock.post(urlEqualTo("/auth/realms/ndw/protocol/openid-connect/token"))
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo("application/x-www-form-urlencoded;charset=UTF-8"))
                .withRequestBody(equalTo("grant_type=client_credentials"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withHeader(HttpHeaders.AUTHORIZATION, "Bearer xyz123")
                        .withBody(String.format("""
                                {
                                  "access_token":"%s",
                                  "token_type":"Bearer",
                                  "expires_in":3600,
                                  "refresh_token":"IwOGYzYTlmM2YxOTQ5MGE3YmNmMDFkNTVk",
                                  "scope":"create"
                                }
                                """, SIMULATED_BEARER_TOKEN))));
    }

    @Override
    public void clearState() {
        // cleanup is done in the WireMockService
    }

    @Override
    public void prepareState() {

        StateManagement.super.prepareState();
        prepareTokenRetrieval();
    }
}
