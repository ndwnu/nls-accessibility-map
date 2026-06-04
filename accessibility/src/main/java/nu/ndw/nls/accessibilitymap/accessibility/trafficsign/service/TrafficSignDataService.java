package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.cache.Cache;
import nu.ndw.nls.accessibilitymap.accessibility.cache.active.ActiveVersionRepository;
import nu.ndw.nls.accessibilitymap.accessibility.cache.locking.DistributedLockService;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.json.JsonWriter;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.configuration.TrafficSignCacheConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.dto.TrafficSigns;
import nu.ndw.nls.springboot.core.time.ClockService;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Service
@Slf4j
public class TrafficSignDataService extends Cache<TrafficSigns> {

    private static final String TRAFFIC_SIGNS_JSON = "trafficSigns.json";

    private final JsonMapper jsonMapper;

    private final JsonWriter jsonWriter;

    public TrafficSignDataService(
            TrafficSignCacheConfiguration trafficSignCacheConfiguration,
            ClockService clockService,
            DistributedLockService distributedLockService,
            JsonMapper jsonMapper,
            JsonWriter jsonWriter,
            ActiveVersionRepository activeVersionRepository,
            RetryTemplate directoryNotEmptyRetryTemplate
    ) {

        super(trafficSignCacheConfiguration, clockService, distributedLockService, activeVersionRepository, directoryNotEmptyRetryTemplate);

        this.jsonMapper = jsonMapper;
        this.jsonWriter = jsonWriter;
    }

    public Set<TrafficSign> findAll() {
        return this.get();
    }

    @Override
    protected TrafficSigns readData(Path activeVersion) throws IOException {
        File trafficSignFile = activeVersion
                .resolve(TRAFFIC_SIGNS_JSON)
                .toFile();
        return jsonMapper.readValue(
                trafficSignFile,
                TrafficSigns.class);
    }

    @Override
    protected void writeData(Path target, TrafficSigns data) throws IOException {
        jsonWriter.writeJsonToFile(target, TRAFFIC_SIGNS_JSON, data);
    }

    @Override
    protected void publishCacheLoadedEvent() {
        // not implemented
    }
}
