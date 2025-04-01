package nu.ndw.nls.accessibilitymap.trafficsignclient.services;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.trafficsignclient.TrafficSignClientProperties;
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

    private final TrafficSignClientProperties trafficSignProperties;

    public TrafficSignData getTrafficSigns(Set<String> rvvCodes) {

        return getTrafficSigns(rvvCodes, Collections.emptySet());
    }

    public TrafficSignData getTrafficSigns(Set<String> rvvCodes, Set<Long> roadSectionIds) {
        MaxNwbVersionTracker maxNwbVersionTracker = new MaxNwbVersionTracker();

        Map<Long, List<TrafficSignGeoJsonDto>> trafficSigns;
        Instant fetchTimestamp = Instant.now();
        try (Stream<TrafficSignGeoJsonDto> stream = findTrafficSignByRvvCodesAndRoadSectionIds(
                rvvCodes.stream().sorted().collect(Collectors.toCollection(LinkedHashSet::new)),
                roadSectionIds)) {
            trafficSigns = stream
                    .filter(this::hasRoadSectionId)
                    .map(maxNwbVersionTracker::updateMaxNwbVersionAndContinue)
                    .collect(Collectors.groupingBy(t -> Long.parseLong(
                            String.valueOf(t.getProperties().getRoadSectionId()))));
        }

        return new TrafficSignData(trafficSigns, maxNwbVersionTracker.getMaxNwbReferenceDate(), fetchTimestamp);
    }

    private Stream<TrafficSignGeoJsonDto> findTrafficSignByRvvCodesAndRoadSectionIds(
            Set<String> rvvCodes,
            Set<Long> roadSectionIds) {

        Set<String> townCodes = trafficSignProperties.getApi().getTownCodes();

        return trafficSignRepository.findCurrentState(
                CurrentStateStatus.PLACED,
                        rvvCodes,
                        roadSectionIds.isEmpty() ? null : roadSectionIds,
                        townCodes.isEmpty() ? null : townCodes)
                .getFeatures().stream();

    }

    private boolean hasRoadSectionId(TrafficSignGeoJsonDto t) {
        return t.getProperties().getRoadSectionId() != null;
    }

}
