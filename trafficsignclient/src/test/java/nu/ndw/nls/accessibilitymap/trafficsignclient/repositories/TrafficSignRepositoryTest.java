package nu.ndw.nls.accessibilitymap.trafficsignclient.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import nu.ndw.nls.accessibilitymap.trafficsignclient.TrafficSignConfiguration;
import nu.ndw.nls.accessibilitymap.trafficsignclient.TrafficSignProperties;
import nu.ndw.nls.accessibilitymap.trafficsignclient.TrafficSignProperties.TrafficSignApiProperties;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.CurrentStateStatus;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignJsonDtoV3;
import nu.ndw.nls.accessibilitymap.trafficsignclient.repositories.TrafficSignRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Flux;

@ExtendWith(MockitoExtension.class)
class TrafficSignRepositoryTest {

    private static final String CURRENT_STATE_URI = "/current-state";

    private static final String QUERY_PARAM_STATUS = "status";
    private static final String QUERY_PARAM_RVV_CODE = "rvv-code";

    private static final String VALUE_VVCODE_A1 = "abc";
    private static final String QUERT_PARAM_TOWN_CODE = "town-code";
    private static final String VALUE_TOWN_CODE = "GM0307";
    private static final CurrentStateStatus VALUE_CURRENT_STATE_STATUS_PLACED = CurrentStateStatus.PLACED;
    private static final String QUERY_PARAM_TOWN_CODE = "town-code";

    @Mock
    private WebClient webClient;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;
    @Mock
    private Flux<TrafficSignJsonDtoV3> trafficSignJsonDtoV3Flux;
    @Mock
    private Stream<TrafficSignJsonDtoV3> trafficSignJsonDtoV3Stream;
    @Mock
    private UriBuilder uriBuilder;

    @Mock
    private TrafficSignConfiguration trafficSignConfiguration;
    @Mock
    private TrafficSignProperties trafficSignProperties;
    @Mock
    private TrafficSignApiProperties trafficSignApiProperties;
    @Mock
    private URI buildUrl;

    @InjectMocks
    private TrafficSignRepository trafficSignRepository;

    @Test
    void getTrafficSigns_ok() {
        when(trafficSignConfiguration.getWebClient()).thenReturn(webClient);
        when(trafficSignProperties.getApi()).thenReturn(trafficSignApiProperties);
        when(trafficSignApiProperties.getCurrentStatePath()).thenReturn(CURRENT_STATE_URI);
        when(trafficSignApiProperties.getTownCode()).thenReturn(VALUE_TOWN_CODE);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(uriBuilder.path(CURRENT_STATE_URI)).thenReturn(uriBuilder);
        when(uriBuilder.queryParam(QUERY_PARAM_STATUS, VALUE_CURRENT_STATE_STATUS_PLACED)).thenReturn(uriBuilder);
        when(uriBuilder.queryParam(QUERY_PARAM_RVV_CODE, VALUE_VVCODE_A1)).thenReturn(uriBuilder);
        when(uriBuilder.queryParam(QUERT_PARAM_TOWN_CODE, VALUE_TOWN_CODE)).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(buildUrl);

        doAnswer(invocationOnMock -> {
            Function<UriBuilder, URI> uriBuilderURIFunction = invocationOnMock.getArgument(0);
            assertEquals(buildUrl, uriBuilderURIFunction.apply(uriBuilder));
            return requestHeadersSpec;
        }).when(requestHeadersUriSpec).uri(any(Function.class));

        when(requestHeadersSpec.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(TrafficSignJsonDtoV3.class)).thenReturn(trafficSignJsonDtoV3Flux);
        when(trafficSignJsonDtoV3Flux.toStream()).thenReturn(trafficSignJsonDtoV3Stream);

        assertEquals(trafficSignJsonDtoV3Stream,
                trafficSignRepository.findCurrentState(CurrentStateStatus.PLACED, Set.of(VALUE_VVCODE_A1)));

        verify(uriBuilder).path(CURRENT_STATE_URI);
        verify(uriBuilder).queryParam(QUERY_PARAM_STATUS, VALUE_CURRENT_STATE_STATUS_PLACED);
        verify(uriBuilder).queryParam(QUERY_PARAM_RVV_CODE, VALUE_VVCODE_A1);
        verify(uriBuilder).queryParam(QUERY_PARAM_TOWN_CODE, VALUE_TOWN_CODE);
        verify(uriBuilder).build();
    }

}
