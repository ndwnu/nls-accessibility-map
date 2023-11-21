package nu.ndw.nls.accessibilitymap.jobs.trafficsigns.services;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
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

    public TrafficSignData getTrafficSigns() {
        AtomicReference<Instant> maxEventTimestamp = new AtomicReference<>(Instant.MIN);
        AtomicReference<LocalDate> maxNwbReferenceDate = new AtomicReference<>(LocalDate.MIN);
        Map<Long, List<TrafficSignJsonDtoV3>> trafficSigns = webClient.get()
                .uri(currentStateUri)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToFlux(TrafficSignJsonDtoV3.class)
                .toStream()
                .peek(t -> maxEventTimestamp.updateAndGet(currentMax -> max(t.getPublicationTimestamp(), currentMax)))
                .filter(t -> t.getLocation().getRoad() != null && t.getLocation().getRoad().getRoadSectionId() != null)
                .peek(t -> maxNwbReferenceDate.updateAndGet(currentMax -> max(t.getLocation().getRoad().getNwbVersion(),
                        currentMax)))
                .collect(Collectors.groupingBy(t -> Long.parseLong(t.getLocation().getRoad().getRoadSectionId())));
        return new TrafficSignData(trafficSigns, maxNwbReferenceDate.get(), maxEventTimestamp.get());
    }

    private Instant max(Instant publicationTimestamp, Instant currentMax) {
        return publicationTimestamp != null && publicationTimestamp.isAfter(currentMax) ? publicationTimestamp
                : currentMax;
    }

    private LocalDate max(String nwbVersion, LocalDate currentMax) {
        if (nwbVersion == null) {
            return currentMax;
        }
        LocalDate newVersion;
        try {
            newVersion = LocalDate.parse(nwbVersion);
        } catch (DateTimeParseException ignored) {
            return currentMax;
        }
        return newVersion.isAfter(currentMax) ? newVersion : currentMax;
    }

    public record TrafficSignData(Map<Long, List<TrafficSignJsonDtoV3>> trafficSignsByRoadSectionId,
                                  LocalDate maxNwbReferenceDate, Instant maxEventTimestamp) {

    }
}
