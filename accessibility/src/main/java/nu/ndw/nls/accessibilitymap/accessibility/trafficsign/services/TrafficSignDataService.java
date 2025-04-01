package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.request.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.configuration.TrafficSignCacheConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.dto.TrafficSigns;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class TrafficSignDataService {

    private static final BigDecimal BINARY_KILO = BigDecimal.valueOf(1024);

    private static final int SIZE_ROUNDING = 2;

    private final TrafficSigns trafficSigns = new TrafficSigns();

    private final ReentrantLock dataLock = new ReentrantLock();

    private final ObjectMapper objectMapper;

    private final TrafficSignCacheConfiguration trafficSignCacheConfiguration;

    public List<TrafficSign> findAllBy(AccessibilityRequest accessibilityRequest) {

        return this.getTrafficSigns().stream()
                .filter(trafficSign -> trafficSign.isRelevant(accessibilityRequest))
                .toList();
    }

    public List<TrafficSign> getTrafficSigns() {
        dataLock.lock();
        try {
            if (trafficSigns.isEmpty()) {
                updateTrafficSignsFromFile();
            }
            return new ArrayList<>(trafficSigns);
        } finally {
            dataLock.unlock();
        }
    }

    protected void updateTrafficSignsFromFile() {
        try {
            log.info("Reading traffic signs from {}", trafficSignCacheConfiguration.getActiveVersion().toPath().toAbsolutePath());

            OffsetDateTime start = OffsetDateTime.now();
            TrafficSigns newTrafficSignsData = objectMapper.readValue(trafficSignCacheConfiguration.getActiveVersion(), TrafficSigns.class);
            log.info("Read traffic signs data from `{}` with size {}MB in {} ms",
                    trafficSignCacheConfiguration.getActiveVersion().toPath().toAbsolutePath(),
                    BigDecimal.valueOf(Files.size(trafficSignCacheConfiguration.getActiveVersion().toPath()))
                            .divide(BINARY_KILO.multiply(BINARY_KILO), SIZE_ROUNDING, RoundingMode.HALF_UP),
                    Duration.between(start, OffsetDateTime.now()).toMillis());

            start = OffsetDateTime.now();
            dataLock.lock();
            try {
                trafficSigns.clear();
                trafficSigns.addAll(newTrafficSignsData);
            } finally {
                dataLock.unlock();
                log.info("Switched internal traffic signs data structure and was locked for {} ms",
                        Duration.between(start, OffsetDateTime.now()).toMillis());
            }
        } catch (IOException exception) {
            log.warn("Failed to read traffic signs from file", exception);
        }
    }
}
