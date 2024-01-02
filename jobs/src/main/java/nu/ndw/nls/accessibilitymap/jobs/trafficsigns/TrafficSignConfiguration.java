package nu.ndw.nls.accessibilitymap.jobs.trafficsigns;

import lombok.Getter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Getter
@Configuration
@EnableConfigurationProperties(TrafficSignProperties.class)
public class TrafficSignConfiguration {

    private final TrafficSignProperties trafficSignProperties;

    private final WebClient webClient;

    public TrafficSignConfiguration(TrafficSignProperties trafficSignProperties, WebClient.Builder webClientBuilder) {
        this.trafficSignProperties = trafficSignProperties;

        this.webClient =  webClientBuilder
                .baseUrl(trafficSignProperties.getApi().getBaseUrl().toString())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

}
