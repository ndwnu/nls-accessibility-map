package nu.ndw.nls.accessibilitymap.trafficsignclient.dtos;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record TrafficSignData(Map<Long, List<TrafficSignGeoJsonDto>> trafficSignsByRoadSectionId,
                              LocalDate maxNwbReferenceDate, Instant maxEventTimestamp) {

    public List<TrafficSignGeoJsonDto> getTrafficSignsByRoadSectionId(Long id) {
        return trafficSignsByRoadSectionId.getOrDefault(id, List.of());
    }
}
