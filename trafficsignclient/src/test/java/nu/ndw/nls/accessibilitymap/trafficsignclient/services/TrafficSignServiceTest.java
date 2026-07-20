package nu.ndw.nls.accessibilitymap.trafficsignclient.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.api.v1.CurrentStateControllerV5ApiClient;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TrafficSignFeatureCollectionV5Json;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TrafficSignGeoJsonDtoV5Json;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class TrafficSignServiceTest {

    private static final String PLACED = "PLACED";

    @Mock
    private CurrentStateControllerV5ApiClient currentStateControllerV5ApiClient;

    @InjectMocks
    private TrafficSignService trafficSignService;

    @Mock
    private ResponseEntity<TrafficSignFeatureCollectionV5Json> currentStateResponseEntity;

    @Mock
    private TrafficSignFeatureCollectionV5Json trafficSignFeatureCollectionV5Json;

    @Mock
    private List<TrafficSignGeoJsonDtoV5Json> features;

    @Test
    void getTrafficSigns() {
        when(currentStateControllerV5ApiClient.getCurrentStateDefault(
                null,
                null,
                List.of("C4", "C7"),
                null,
                PLACED,
                null,
                null,
                null,
                null,
                null,
                null)).thenReturn(currentStateResponseEntity);

        when(currentStateResponseEntity.getBody()).thenReturn(trafficSignFeatureCollectionV5Json);

        when(trafficSignFeatureCollectionV5Json.getFeatures()).thenReturn(features);

        assertThat(trafficSignService.getTrafficSigns(Set.of("C7", "C4")))
                .isEqualTo(features);
    }

    @Test
    void getTrafficSigns_noBody() {
        when(currentStateControllerV5ApiClient.getCurrentStateDefault(
                null,
                null,
                List.of("C4", "C7"),
                null,
                PLACED,
                null,
                null,
                null,
                null,
                null,
                null)).thenReturn(currentStateResponseEntity);

        when(currentStateResponseEntity.getBody()).thenReturn(null);

        assertThat(trafficSignService.getTrafficSigns(Set.of("C7", "C4")))
                .isEmpty();
    }
}
