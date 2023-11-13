package nu.ndw.nls.accessibilitymap.jobs.trafficsigns.services;

import java.time.Instant;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
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

    public TrafficSignResponse getTrafficSigns() {
        AtomicReference<Instant> maxLastEventOn = new AtomicReference<>(Instant.MIN);
        Stream<TrafficSignJsonDtoV3> trafficSigns = webClient.get()
                .uri(currentStateUri)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToFlux(TrafficSignJsonDtoV3.class)
                .toStream()
                .peek(t -> maxLastEventOn.updateAndGet(currentMax -> max(t.getLastEventOn(), currentMax)))
                .filter(t -> t.getLocation().getRoad() != null && t.getLocation().getRoad().getRoadSectionId() != null)
                .sorted(Comparator.comparing(h -> h.getLocation().getRoad().getRoadSectionId()));
        return new TrafficSignResponse(trafficSigns, maxLastEventOn::get);
    }

    private Instant max(Instant lastEventOn, Instant currentMax) {
        return lastEventOn != null && lastEventOn.isAfter(currentMax) ? lastEventOn : currentMax;
    }

    public record TrafficSignResponse(Stream<TrafficSignJsonDtoV3> trafficSigns, Supplier<Instant> maxLastEventOn) {

    }
}
