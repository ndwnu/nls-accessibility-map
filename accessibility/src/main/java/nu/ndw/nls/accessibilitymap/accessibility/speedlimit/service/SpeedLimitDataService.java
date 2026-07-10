package nu.ndw.nls.accessibilitymap.accessibility.speedlimit.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.cache.Cache;
import nu.ndw.nls.accessibilitymap.accessibility.cache.active.ActiveVersionRepository;
import nu.ndw.nls.accessibilitymap.accessibility.cache.locking.DistributedLockService;
import nu.ndw.nls.accessibilitymap.accessibility.json.JsonWriter;
import nu.ndw.nls.accessibilitymap.accessibility.speedlimit.configuration.SpeedLimitCacheConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.speedlimit.dto.SpeedLimits;
import nu.ndw.nls.springboot.core.time.ClockService;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Service
@Slf4j
public class SpeedLimitDataService extends Cache<SpeedLimits> {

    private static final String DATA_FILE = "speedLimits.json";

    private final JsonMapper jsonMapper;

    private final JsonWriter jsonWriter;

    public SpeedLimitDataService(
            SpeedLimitCacheConfiguration speedLimitCacheConfiguration,
            ClockService clockService,
            DistributedLockService distributedLockService,
            JsonMapper jsonMapper,
            JsonWriter jsonWriter,
            ActiveVersionRepository activeVersionRepository,
            RetryTemplate directoryNotEmptyRetryTemplate
    ) {

        super(speedLimitCacheConfiguration, clockService, distributedLockService, activeVersionRepository, directoryNotEmptyRetryTemplate);

        this.jsonMapper = jsonMapper;
        this.jsonWriter = jsonWriter;
    }

    public SpeedLimits findAll() {
        return this.get();
    }

    @Override
    protected SpeedLimits readData(Path activeVersion) {
        File trafficSignFile = activeVersion
                .resolve(DATA_FILE)
                .toFile();
        return jsonMapper.readValue(
                trafficSignFile,
                SpeedLimits.class);
    }

    @Override
    protected void writeData(Path target, SpeedLimits data) throws IOException {
        jsonWriter.writeJsonToFile(target, DATA_FILE, data);
    }
}
