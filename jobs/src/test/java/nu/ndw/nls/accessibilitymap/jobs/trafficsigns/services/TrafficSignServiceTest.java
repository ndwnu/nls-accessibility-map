package nu.ndw.nls.accessibilitymap.jobs.trafficsigns.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos.LocationJsonDtoV3;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos.RoadJsonDtoV3;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos.TrafficSignJsonDtoV3;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.services.TrafficSignService.TrafficSignResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@ExtendWith(MockitoExtension.class)
class TrafficSignServiceTest {

    private static final String CURRENT_STATE_URI = "/current-state";
    private static final Instant MAX_LAST_EVENT_ON = Instant.parse("2023-11-07T15:37:23Z");

    private TrafficSignService trafficSignService;

    @Mock
    private WebClient webClient;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        trafficSignService = new TrafficSignService(webClient, CURRENT_STATE_URI);
    }

    @Test
    void getTrafficSigns_ok_filteredAndSorted() {
        TrafficSignJsonDtoV3 trafficSign1 = TrafficSignJsonDtoV3.builder()
                .lastEventOn(MAX_LAST_EVENT_ON)
                .location(LocationJsonDtoV3.builder()
                        .build())
                .build();
        TrafficSignJsonDtoV3 trafficSign2 = TrafficSignJsonDtoV3.builder()
                .lastEventOn(MAX_LAST_EVENT_ON.minusSeconds(60))
                .location(LocationJsonDtoV3.builder()
                        .road(RoadJsonDtoV3.builder()
                                .build())
                        .build())
                .build();
        TrafficSignJsonDtoV3 trafficSign3 = TrafficSignJsonDtoV3.builder()
                .lastEventOn(MAX_LAST_EVENT_ON.minusSeconds(120))
                .location(LocationJsonDtoV3.builder()
                        .road(RoadJsonDtoV3.builder()
                                .roadSectionId("2")
                                .build())
                        .build())
                .build();
        TrafficSignJsonDtoV3 trafficSign4 = TrafficSignJsonDtoV3.builder()
                .location(LocationJsonDtoV3.builder()
                        .road(RoadJsonDtoV3.builder()
                                .roadSectionId("1")
                                .build())
                        .build())
                .build();

        mockWebClient();
        when(responseSpec.bodyToFlux(TrafficSignJsonDtoV3.class))
                .thenReturn(Flux.just(trafficSign1, trafficSign2, trafficSign3, trafficSign4));

        TrafficSignResponse response = trafficSignService.getTrafficSigns();

        assertEquals(List.of(trafficSign4, trafficSign3), response.trafficSigns().toList());
        assertEquals(MAX_LAST_EVENT_ON, response.maxLastEventOn().get());
    }

    @Test
    void getTrafficSigns_ok_emptyFlux() {
        mockWebClient();
        when(responseSpec.bodyToFlux(TrafficSignJsonDtoV3.class)).thenReturn(Flux.empty());

        TrafficSignResponse response = trafficSignService.getTrafficSigns();

        assertEquals(List.of(), response.trafficSigns().toList());
        assertEquals(Instant.MIN, response.maxLastEventOn().get());
    }

    private void mockWebClient() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(CURRENT_STATE_URI)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }
}
