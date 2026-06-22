package nu.ndw.nls.accessibilitymap.trafficsignclient.dtos;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TrafficSignGeoJsonDtoV5Json;

public record TrafficSignData(Map<Long, List<TrafficSignGeoJsonDtoV5Json>> trafficSignsByRoadSectionId,
                              LocalDate maxNwbReferenceDate, Instant maxEventTimestamp) {

    public List<TrafficSignGeoJsonDtoV5Json> getTrafficSignsByRoadSectionId(Long id) {
        return trafficSignsByRoadSectionId.getOrDefault(id, List.of());
    }
}
