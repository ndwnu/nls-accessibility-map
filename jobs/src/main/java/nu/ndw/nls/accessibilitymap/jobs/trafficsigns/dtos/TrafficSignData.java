package nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record TrafficSignData(Map<Long, List<TrafficSignJsonDtoV3>> trafficSignsByRoadSectionId,
                              LocalDate maxNwbReferenceDate, Instant maxEventTimestamp) {

    public List<TrafficSignJsonDtoV3> getTrafficSignsByRoadSectionId(Long id) {
        return trafficSignsByRoadSectionId.getOrDefault(id, List.of());
    }
}
