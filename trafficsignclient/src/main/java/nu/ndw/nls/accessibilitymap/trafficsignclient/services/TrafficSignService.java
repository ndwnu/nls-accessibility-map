package nu.ndw.nls.accessibilitymap.trafficsignclient.services;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.CurrentStateStatus;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignData;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.repositories.TrafficSignRepository;
import nu.ndw.nls.accessibilitymap.trafficsignclient.utils.MaxNwbVersionTracker;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrafficSignService {

    private final TrafficSignRepository trafficSignRepository;

    public TrafficSignData getTrafficSigns(Set<String> rvvCodes) {
        MaxNwbVersionTracker maxNwbVersionTracker = new MaxNwbVersionTracker();

        Map<Long, List<TrafficSignGeoJsonDto>> trafficSigns;
        Instant fetchTimestamp = Instant.now();
        try (Stream<TrafficSignGeoJsonDto> stream = findTrafficSignByRvvCodes(rvvCodes)) {
            trafficSigns = stream
                    .filter(this::hasRoadSectionId)
                    .map(maxNwbVersionTracker::updateMaxNwbVersionAndContinue)
                    .collect(Collectors.groupingBy(t -> Long.parseLong(
                            String.valueOf(t.getProperties().getRoadSectionId()))));
        }

        return new TrafficSignData(trafficSigns, maxNwbVersionTracker.getMaxNwbReferenceDate(), fetchTimestamp);
    }

    private Stream<TrafficSignGeoJsonDto> findTrafficSignByRvvCodes(Set<String> rvvCodes) {
        return trafficSignRepository.findCurrentState(
                CurrentStateStatus.PLACED, rvvCodes);

    }

    private boolean hasRoadSectionId(TrafficSignGeoJsonDto t) {
        return t.getProperties().getRoadSectionId() != null;
    }

}
