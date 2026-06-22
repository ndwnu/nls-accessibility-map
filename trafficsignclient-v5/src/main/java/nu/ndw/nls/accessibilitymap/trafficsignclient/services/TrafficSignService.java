package nu.ndw.nls.accessibilitymap.trafficsignclient.services;

import static nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TrafficSignEventDtoV4Json.StatusEnum.PLACED;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignData;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.api.v1.CurrentStateControllerV5ApiClient;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TrafficSignFeatureCollectionV5Json;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TrafficSignGeoJsonDtoV5Json;
import nu.ndw.nls.accessibilitymap.trafficsignclient.utils.MaxNwbVersionTracker;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrafficSignService {

    private final CurrentStateControllerV5ApiClient currentStateControllerV5ApiClient;

    public TrafficSignData getTrafficSigns(Set<String> rvvCodes) {
        MaxNwbVersionTracker maxNwbVersionTracker = new MaxNwbVersionTracker();

        Map<Long, List<TrafficSignGeoJsonDtoV5Json>> trafficSigns;
        Instant fetchTimestamp = Instant.now();

        try (Stream<TrafficSignGeoJsonDtoV5Json> trafficSignGeoJsons = findTrafficSignByRvvCodes(rvvCodes.stream()
                .sorted()
                .toList()).stream()) {
            trafficSigns = trafficSignGeoJsons.filter(this::hasRoadSectionId)
                    .map(maxNwbVersionTracker::updateMaxNwbVersionAndContinue)
                    .collect(Collectors.groupingBy(trafficSignGeoJsonDtoV5Json ->
                                Long.valueOf(trafficSignGeoJsonDtoV5Json.getProperties().getRoadSectionId())));
        }

        return new TrafficSignData(trafficSigns, maxNwbVersionTracker.getMaxNwbReferenceDate(), fetchTimestamp);
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

    private boolean hasRoadSectionId(TrafficSignGeoJsonDtoV5Json trafficSignGeoJsonDtoV5Json) {
        return trafficSignGeoJsonDtoV5Json.getProperties() != null
               && trafficSignGeoJsonDtoV5Json.getProperties().getRoadSectionId() != null;
    }
}
