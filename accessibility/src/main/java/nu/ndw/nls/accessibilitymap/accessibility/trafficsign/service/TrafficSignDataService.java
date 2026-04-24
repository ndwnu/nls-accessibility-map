package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.cache.Cache;
import nu.ndw.nls.accessibilitymap.accessibility.cache.locking.DistributedLockService;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.json.JsonWriter;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.configuration.TrafficSignCacheConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.dto.TrafficSigns;
import nu.ndw.nls.springboot.core.time.ClockService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TrafficSignDataService extends Cache<TrafficSigns> {

    private static final String TRAFFIC_SIGNS_JSON = "trafficSigns.json";

    private final ObjectMapper objectMapper;

    private final JsonWriter jsonWriter;

    public TrafficSignDataService(
            TrafficSignCacheConfiguration trafficSignCacheConfiguration,
            ClockService clockService,
            DistributedLockService distributedLockService,
            ObjectMapper objectMapper, JsonWriter jsonWriter
    ) {

        super(trafficSignCacheConfiguration, clockService, distributedLockService);

        this.objectMapper = objectMapper;
        this.jsonWriter = jsonWriter;
    }

    public Set<TrafficSign> findAll() {

        return this.get();
    }

    @Override
    protected TrafficSigns readData(Path activeVersion) throws IOException {
        return objectMapper.readValue(
                getCacheConfiguration().getActiveVersion().toPath().resolve(TRAFFIC_SIGNS_JSON).toFile(),
                TrafficSigns.class);
    }

    @Override
    protected void writeData(Path target, TrafficSigns data) throws IOException {
        jsonWriter.writeJsonToFile(target, TRAFFIC_SIGNS_JSON, data);
    }
}
