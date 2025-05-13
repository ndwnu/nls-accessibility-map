package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service.NetworkCacheDataService;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.dto.TrafficSigns;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.rule.exclude.TrafficSignExclusion;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.rule.restrictive.TrafficSignRestriction;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class TrafficSignDataService {

    private final TrafficSigns trafficSigns = new TrafficSigns();

    private final ReentrantLock dataLock = new ReentrantLock();

    private final TrafficSignCacheReadWriter trafficSignCacheReadWriter;

    private final List<TrafficSignRestriction> trafficSignRestrictiveDeterminations;

    private final List<TrafficSignExclusion> trafficSignExclusionDeterminations;

    private final NetworkCacheDataService networkCacheDataService;

    private final GraphHopperService graphHopperService;

    @PostConstruct
    public void init() {

        updateTrafficSignData(graphHopperService.getNetworkGraphHopper());
    }

    public List<TrafficSign> findAllBy(AccessibilityRequest accessibilityRequest) {

        return this.getTrafficSigns().stream()
                .filter(trafficSign -> isRelevant(trafficSign, accessibilityRequest))
                .toList();
    }

    @SuppressWarnings("java:S1067")
    private boolean isRelevant(TrafficSign trafficSign, AccessibilityRequest accessibilityRequest) {

        return isNotExcluded(trafficSign, accessibilityRequest)
               && isRestrictive(trafficSign, accessibilityRequest);
    }

    private boolean isRestrictive(TrafficSign trafficSign, AccessibilityRequest accessibilityRequest) {

        return trafficSignRestrictiveDeterminations.stream()
                .anyMatch(trafficSignRestriction -> trafficSignRestriction.test(trafficSign, accessibilityRequest));
    }

    private boolean isNotExcluded(TrafficSign trafficSign, AccessibilityRequest accessibilityRequest) {

        return trafficSignExclusionDeterminations.stream()
                .noneMatch(trafficSignExclusion -> trafficSignExclusion.test(trafficSign, accessibilityRequest));
    }

    public List<TrafficSign> getTrafficSigns() {
        dataLock.lock();
        try {
            return new ArrayList<>(trafficSigns);
        } finally {
            dataLock.unlock();
        }
    }

    protected void updateTrafficSignData(NetworkGraphHopper networkGraphHopper) {
        trafficSignCacheReadWriter.read().ifPresent(newTrafficSignsData -> {
            OffsetDateTime start = OffsetDateTime.now();
            dataLock.lock();
            try {
                trafficSigns.clear();
                trafficSigns.addAll(newTrafficSignsData);
                networkCacheDataService.create(newTrafficSignsData, networkGraphHopper);
            } finally {
                dataLock.unlock();
                log.info("Switched internal traffic signs data structure and was locked for {} ms",
                        Duration.between(start, OffsetDateTime.now()).toMillis());
            }
        });
    }
}
