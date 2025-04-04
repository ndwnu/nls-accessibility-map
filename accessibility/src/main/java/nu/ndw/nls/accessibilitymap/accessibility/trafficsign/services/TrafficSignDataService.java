package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services;

import com.google.common.base.Predicate;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.request.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.dto.TrafficSigns;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.predicates.NotZoneEndsFilterPredicate;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.predicates.RestrictionIsAbsoluteFilterPredicate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class TrafficSignDataService {

    private final TrafficSigns trafficSigns = new TrafficSigns();

    private final ReentrantLock dataLock = new ReentrantLock();

    private final TrafficSignCacheReadWriter trafficSignCacheReadWriter;

    private final NotZoneEndsFilterPredicate notZoneEndsFilterPredicate;
    private final RestrictionIsAbsoluteFilterPredicate restrictionIsAbsoluteFilterPredicate;

    @PostConstruct
    public void init() {

        updateTrafficSignData();
    }

    public List<TrafficSign> findAllBy(AccessibilityRequest accessibilityRequest) {

        return this.getTrafficSigns().stream()
                .filter(notZoneEndsFilterPredicate::test)
                .filter(restrictionIsAbsoluteFilterPredicate::test)
                .filter(trafficSign -> trafficSign.isRelevant(accessibilityRequest))
                .toList();
    }

    public List<TrafficSign> getTrafficSigns() {
        dataLock.lock();
        try {
            return new ArrayList<>(trafficSigns);
        } finally {
            dataLock.unlock();
        }
    }

    protected void updateTrafficSignData() {
        trafficSignCacheReadWriter.read().ifPresent(newTrafficSignsData -> {
            OffsetDateTime start = OffsetDateTime.now();
            dataLock.lock();
            try {
                trafficSigns.clear();
                trafficSigns.addAll(newTrafficSignsData);
            } finally {
                dataLock.unlock();
                log.info("Switched internal traffic signs data structure and was locked for {} ms",
                        Duration.between(start, OffsetDateTime.now()).toMillis());
            }
        });
    }
}
