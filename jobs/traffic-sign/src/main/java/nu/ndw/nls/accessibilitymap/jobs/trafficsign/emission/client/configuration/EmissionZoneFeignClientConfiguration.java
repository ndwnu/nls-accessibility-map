package nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.client.configuration;

import feign.RequestInterceptor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.springboot.security.oauth2.client.services.OAuth2ClientCredentialsTokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;

@AllArgsConstructor
@Slf4j
public class EmissionZoneFeignClientConfiguration {

    @Bean(name = "emissionZoneApiRequestInterceptor")
    public RequestInterceptor requestInterceptor(
            OAuth2ClientCredentialsTokenService oAuth2ClientCredentialsTokenService,
            EmissionZoneOAuthConfiguration emissionZoneOAuthConfiguration) {

        return requestTemplate ->
                requestTemplate.header(
                        HttpHeaders.AUTHORIZATION,
                        String.format("Bearer %s", oAuth2ClientCredentialsTokenService.getAccessToken(emissionZoneOAuthConfiguration.getRegistrationId())));
    }
}
