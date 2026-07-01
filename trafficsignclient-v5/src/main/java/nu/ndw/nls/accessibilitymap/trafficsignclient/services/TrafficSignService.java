package nu.ndw.nls.accessibilitymap.trafficsignclient.services;

import static nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TrafficSignEventDtoV4Json.StatusEnum.PLACED;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.api.v1.CurrentStateControllerV5ApiClient;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TrafficSignFeatureCollectionV5Json;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TrafficSignGeoJsonDtoV5Json;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrafficSignService {

    private final CurrentStateControllerV5ApiClient currentStateControllerV5ApiClient;

    public List<TrafficSignGeoJsonDtoV5Json> getTrafficSigns(Set<String> rvvCodes) {
        return findTrafficSignByRvvCodes(rvvCodes.stream()
                .sorted()
                .toList());
    }

    private List<TrafficSignGeoJsonDtoV5Json> findTrafficSignByRvvCodes(List<String> rvvCodes) {

        ResponseEntity<TrafficSignFeatureCollectionV5Json> currentStateResponseEntity =
                currentStateControllerV5ApiClient.getCurrentStateDefault(
                    null,
                    "GM0307",
                    rvvCodes,
                    null,
                    PLACED.getValue(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null);

        TrafficSignFeatureCollectionV5Json body = currentStateResponseEntity.getBody();
        if (body == null) {
            return Collections.emptyList();
        }

        return body.getFeatures();
    }
}
