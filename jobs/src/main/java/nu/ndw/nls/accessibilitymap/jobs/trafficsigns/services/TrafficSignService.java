package nu.ndw.nls.accessibilitymap.jobs.trafficsigns.services;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos.CurrentStateStatus;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos.TrafficSignJsonDtoV3;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.mappers.TrafficSignToLinkTagMapper;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.repositories.TrafficSignRepository;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.utils.MaxEventTimestampTracker;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.utils.MaxNwbVersionTracker;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrafficSignService {

    private final TrafficSignRepository trafficSignRepository;
    private final TrafficSignToLinkTagMapper trafficSignToLinkTagMapper;


    public TrafficSignData getTrafficSigns() {
        MaxEventTimestampTracker maxEventTimestampTracker = new MaxEventTimestampTracker();
        MaxNwbVersionTracker maxNwbVersionTracker = new MaxNwbVersionTracker();

        Map<Long, List<TrafficSignJsonDtoV3>> trafficSigns;

        try ( Stream<TrafficSignJsonDtoV3> stream = findTrafficSignByRvvCodes()) {
            trafficSigns = stream.map(maxEventTimestampTracker::updateMaxEventTimeStampAndContinue)
                    .filter(this::hasRoadSectionId)
                    .map(maxNwbVersionTracker::updateMaxNwbVersionAndContinue)
                    .collect(Collectors.groupingBy(t -> Long.parseLong(t.getLocation().getRoad().getRoadSectionId())));
        }

        return new TrafficSignData(trafficSigns, maxNwbVersionTracker.getMaxNwbReferenceDate(),
                maxEventTimestampTracker.getMaxEventTimestamp());
    }

    private Stream<TrafficSignJsonDtoV3> findTrafficSignByRvvCodes() {
       return trafficSignRepository.findCurrentState(CurrentStateStatus.PLACED,trafficSignToLinkTagMapper.getRvvCodesUsed());
    }

    private boolean hasRoadSectionId(TrafficSignJsonDtoV3 t) {
        return t.getLocation().getRoad() != null && t.getLocation().getRoad().getRoadSectionId() != null;
    }

    public record TrafficSignData(Map<Long, List<TrafficSignJsonDtoV3>> trafficSignsByRoadSectionId,
                                  LocalDate maxNwbReferenceDate, Instant maxEventTimestamp) {

    }
}
