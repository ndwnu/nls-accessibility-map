package nu.ndw.nls.accessibilitymap.jobs.trafficsigns.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos.LocationJsonDtoV3;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos.RoadJsonDtoV3;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos.TrafficSignJsonDtoV3;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.services.TrafficSignService.TrafficSignData;
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
    void getTrafficSigns_ok_filteredAndGrouped() {
        TrafficSignJsonDtoV3 trafficSign1 = TrafficSignJsonDtoV3.builder()
                .lastEventOn(Instant.parse("2023-11-07T15:37:23Z"))
                .location(LocationJsonDtoV3.builder()
                        .build())
                .build();
        TrafficSignJsonDtoV3 trafficSign2 = TrafficSignJsonDtoV3.builder()
                .lastEventOn(Instant.parse("2023-11-07T15:36:23Z"))
                .location(LocationJsonDtoV3.builder()
                        .road(RoadJsonDtoV3.builder()
                                .nwbVersion("2023-11-01")
                                .build())
                        .build())
                .build();
        TrafficSignJsonDtoV3 trafficSign3 = TrafficSignJsonDtoV3.builder()
                .lastEventOn(Instant.parse("2023-11-07T15:35:23Z"))
                .location(LocationJsonDtoV3.builder()
                        .road(RoadJsonDtoV3.builder()
                                .roadSectionId("2")
                                .nwbVersion("2023-10-01")
                                .build())
                        .build())
                .build();
        TrafficSignJsonDtoV3 trafficSign4 = TrafficSignJsonDtoV3.builder()
                .location(LocationJsonDtoV3.builder()
                        .road(RoadJsonDtoV3.builder()
                                .roadSectionId("1")
                                .nwbVersion("2023-09-01")
                                .build())
                        .build())
                .build();
        TrafficSignJsonDtoV3 trafficSign5 = TrafficSignJsonDtoV3.builder()
                .location(LocationJsonDtoV3.builder()
                        .road(RoadJsonDtoV3.builder()
                                .roadSectionId("1")
                                .nwbVersion("20231101")
                                .build())
                        .build())
                .build();
        TrafficSignJsonDtoV3 trafficSign6 = TrafficSignJsonDtoV3.builder()
                .location(LocationJsonDtoV3.builder()
                        .road(RoadJsonDtoV3.builder()
                                .roadSectionId("2")
                                .build())
                        .build())
                .build();

        mockWebClient();
        when(responseSpec.bodyToFlux(TrafficSignJsonDtoV3.class)).thenReturn(Flux.just(trafficSign1, trafficSign2,
                trafficSign3, trafficSign4, trafficSign5, trafficSign6));

        TrafficSignData result = trafficSignService.getTrafficSigns();

        assertEquals(Map.of(1L, List.of(trafficSign4, trafficSign5), 2L, List.of(trafficSign3, trafficSign6)),
                result.trafficSignsByRoadSectionId());
        assertEquals(Instant.parse("2023-11-07T15:37:23Z"), result.maxEventTimestamp());
        assertEquals(LocalDate.of(2023, 10, 1), result.maxNwbReferenceDate());
    }

    @Test
    void getTrafficSigns_ok_emptyFlux() {
        mockWebClient();
        when(responseSpec.bodyToFlux(TrafficSignJsonDtoV3.class)).thenReturn(Flux.empty());

        TrafficSignData result = trafficSignService.getTrafficSigns();

        assertEquals(Map.of(), result.trafficSignsByRoadSectionId());
        assertEquals(Instant.MIN, result.maxEventTimestamp());
        assertEquals(LocalDate.MIN, result.maxNwbReferenceDate());
    }

    private void mockWebClient() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(CURRENT_STATE_URI)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }
}
