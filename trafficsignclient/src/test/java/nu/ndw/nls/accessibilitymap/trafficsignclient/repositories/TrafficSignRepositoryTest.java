package nu.ndw.nls.accessibilitymap.trafficsignclient.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.net.URI;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import nu.ndw.nls.accessibilitymap.trafficsignclient.TrafficSignConfiguration;
import nu.ndw.nls.accessibilitymap.trafficsignclient.TrafficSignProperties;
import nu.ndw.nls.accessibilitymap.trafficsignclient.TrafficSignProperties.TrafficSignApiProperties;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.CurrentStateStatus;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
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
    private static final String QUERY_PARAM_RVV_CODE = "rvvCode";
    private static final String VALUE_VVCODE_A1 = "abc";
    private static final String VALUE_TOWN_CODE = "GM0307";
    private static final String QUERY_PARAM_TOWN_CODE = "townCode";
    private static final String APPLICATION_GEO_JSON = "application/geo+json";

    @Mock
    private TrafficSignConfiguration trafficSignConfiguration;
    @Mock
    private TrafficSignProperties trafficSignProperties;
    @Mock
    private TrafficSignApiProperties trafficSignApiProperties;

    @Mock
    private WebClient webClient;
    @Mock
    private WebClient.RequestHeadersSpec reqHeaderSpec;
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;


    @Mock
    private UriBuilder uriBuilder;
    @Mock
    private URI buildUrl;
    @Mock
    private Stream<TrafficSignGeoJsonDto> trafficSignJsonDtoStream;

    @InjectMocks
    private TrafficSignRepository trafficSignRepository;

    @Test
    void getTrafficSigns_ok() {
        // Mock configuration and setup
        when(trafficSignConfiguration.getWebClient()).thenReturn(webClient);
        when(trafficSignProperties.getApi()).thenReturn(trafficSignApiProperties);
        when(trafficSignApiProperties.getCurrentStatePath()).thenReturn(CURRENT_STATE_URI);
        when(trafficSignApiProperties.getTownCode()).thenReturn(VALUE_TOWN_CODE);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(uriBuilder.path(CURRENT_STATE_URI)).thenReturn(uriBuilder);
        when(uriBuilder.queryParam(QUERY_PARAM_STATUS, CurrentStateStatus.PLACED)).thenReturn(uriBuilder);
        when(uriBuilder.queryParam(QUERY_PARAM_RVV_CODE, VALUE_VVCODE_A1)).thenReturn(uriBuilder);
        when(uriBuilder.queryParam(QUERY_PARAM_TOWN_CODE, VALUE_TOWN_CODE)).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(buildUrl);

        doAnswer(invocationOnMock -> {
            Function<UriBuilder, URI> uriBuilderURIFunction = invocationOnMock.getArgument(0);
            assertEquals(buildUrl, uriBuilderURIFunction.apply(uriBuilder));
            return reqHeaderSpec;
        }).when(requestHeadersUriSpec).uri(any(Function.class));

        when(reqHeaderSpec.header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)).thenReturn(reqHeaderSpec);
        when(reqHeaderSpec.accept(MediaType.valueOf(APPLICATION_GEO_JSON))).thenReturn(reqHeaderSpec);

        TrafficSignGeoJsonDto sampleDto = new TrafficSignGeoJsonDto();
        Flux<TrafficSignGeoJsonDto> sampleFlux = Flux.just(sampleDto);

        when(reqHeaderSpec.exchangeToFlux(any(Function.class))).thenReturn(sampleFlux);

        trafficSignJsonDtoStream = sampleFlux.toStream();

        Stream<TrafficSignGeoJsonDto> actualStream = trafficSignRepository.findCurrentState(
                CurrentStateStatus.PLACED, Set.of(VALUE_VVCODE_A1));
        assertEquals(trafficSignJsonDtoStream.toList(), actualStream.toList());

        verify(uriBuilder).path(CURRENT_STATE_URI);
        verify(uriBuilder).queryParam(QUERY_PARAM_STATUS, CurrentStateStatus.PLACED);
        verify(uriBuilder).queryParam(QUERY_PARAM_RVV_CODE, VALUE_VVCODE_A1);
        verify(uriBuilder).queryParam(QUERY_PARAM_TOWN_CODE, VALUE_TOWN_CODE);
        verify(uriBuilder).build();
    }
}