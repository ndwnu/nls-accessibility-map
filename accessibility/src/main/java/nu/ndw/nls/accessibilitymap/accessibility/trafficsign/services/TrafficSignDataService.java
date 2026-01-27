package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.dto.TrafficSigns;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class TrafficSignDataService {

    private final TrafficSignCacheReadWriter trafficSignCacheReadWriter;

    private final TrafficSigns trafficSigns = new TrafficSigns();

    private final ReentrantLock dataLock = new ReentrantLock();

    public final List<Runnable> updateListeners = new ArrayList<>();

    @PostConstruct
    public void init() {

        updateTrafficSignData();
    }

    public Set<TrafficSign> findAll() {

        return this.getTrafficSigns();
    }

    public Set<TrafficSign> getTrafficSigns() {
        dataLock.lock();
        try {
            return new HashSet<>(trafficSigns);
        } finally {
            dataLock.unlock();
        }
    }

    public void registerUpdateListener(Runnable runnable) {
        synchronized (updateListeners) {
            updateListeners.add(runnable);
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
                synchronized (updateListeners) {
                    updateListeners.forEach(Runnable::run);
                }
                log.info(
                        "Switched internal traffic signs data structure and was locked for {} ms",
                        Duration.between(start, OffsetDateTime.now()).toMillis());
            }
        });
    }
}
