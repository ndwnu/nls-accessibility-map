package nu.ndw.nls.accessibilitymap.jobs.trafficsigns.services;

import java.util.Comparator;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos.TrafficSignJsonDtoV3;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class TrafficSignService {

    @Value("${itTest}")
    private boolean itTest;
    private final WebClient webClient;

    @SneakyThrows
    public Stream<TrafficSignJsonDtoV3> getTrafficSigns() {
        String url = itTest ? "/current-state?status=PLACED&town-code=GM0307" : "/current-state?status=PLACED";
        return webClient.get()
                .uri(url)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToFlux(TrafficSignJsonDtoV3.class)
                .filter(t -> t.getLocation().getRoad() != null)
                .filter(t -> t.getLocation().getRoad().getRoadSectionId() != null)
                .toStream()
                .sorted(Comparator.comparing(h -> h.getLocation().getRoad().getRoadSectionId()));
    }
}
