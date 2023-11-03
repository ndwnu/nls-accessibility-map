package nu.ndw.nls.accessibilitymap.jobs.trafficsigns.services;

import java.util.Comparator;
import java.util.stream.Stream;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos.TrafficSignJsonDtoV3;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class TrafficSignService {

    private final WebClient webClient;
    private final String currentStateUri;

    public TrafficSignService(WebClient webClient, @Value("${traffic-sign.current-state-uri}") String currentStateUri) {
        this.webClient = webClient;
        this.currentStateUri = currentStateUri;
    }

    public Stream<TrafficSignJsonDtoV3> getTrafficSigns() {
        return webClient.get()
                .uri(currentStateUri)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToFlux(TrafficSignJsonDtoV3.class)
                .filter(t -> t.getLocation().getRoad() != null && t.getLocation().getRoad().getRoadSectionId() != null)
                .toStream()
                .sorted(Comparator.comparing(h -> h.getLocation().getRoad().getRoadSectionId()));
    }
}
