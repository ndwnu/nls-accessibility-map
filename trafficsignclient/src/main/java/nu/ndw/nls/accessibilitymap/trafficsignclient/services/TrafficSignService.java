package nu.ndw.nls.accessibilitymap.trafficsignclient.services;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.CurrentStateStatus;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignData;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignJsonDtoV3;
import nu.ndw.nls.accessibilitymap.trafficsignclient.repositories.TrafficSignRepository;
import nu.ndw.nls.accessibilitymap.trafficsignclient.utils.MaxEventTimestampTracker;
import nu.ndw.nls.accessibilitymap.trafficsignclient.utils.MaxNwbVersionTracker;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrafficSignService {

    private final TrafficSignRepository trafficSignRepository;

    public TrafficSignData getTrafficSigns(Set<String> rvvCodes) {
        MaxEventTimestampTracker maxEventTimestampTracker = new MaxEventTimestampTracker();
        MaxNwbVersionTracker maxNwbVersionTracker = new MaxNwbVersionTracker();

        Map<Long, List<TrafficSignJsonDtoV3>> trafficSigns;

        try (Stream<TrafficSignJsonDtoV3> stream = findTrafficSignByRvvCodes(rvvCodes)) {
            trafficSigns = stream.map(maxEventTimestampTracker::updateMaxEventTimeStampAndContinue)
                    .filter(this::hasRoadSectionId)
                    .map(maxNwbVersionTracker::updateMaxNwbVersionAndContinue)
                    .collect(Collectors.groupingBy(t -> Long.parseLong(t.getLocation().getRoad().getRoadSectionId())));
        }

        return new TrafficSignData(trafficSigns, maxNwbVersionTracker.getMaxNwbReferenceDate(),
                maxEventTimestampTracker.getMaxEventTimestamp());
    }

    private Stream<TrafficSignJsonDtoV3> findTrafficSignByRvvCodes(Set<String> rvvCodes) {
        return trafficSignRepository.findCurrentState(CurrentStateStatus.PLACED, rvvCodes);
    }

    private boolean hasRoadSectionId(TrafficSignJsonDtoV3 t) {
        return t.getLocation() != null &&
                t.getLocation().getRoad() != null &&
                t.getLocation().getRoad().getRoadSectionId() != null;
    }

}
