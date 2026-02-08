package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.cache.Cache;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.configuration.TrafficSignCacheConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.dto.TrafficSigns;
import nu.ndw.nls.springboot.core.time.ClockService;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TrafficSignDataService extends Cache<TrafficSigns> {

    private final ObjectMapper objectMapper;

    public TrafficSignDataService(
            TrafficSignCacheConfiguration trafficSignCacheConfiguration,
            ClockService clockService,
            ObjectMapper objectMapper) {

        super(trafficSignCacheConfiguration, clockService);

        this.objectMapper = objectMapper;
    }

    public Set<TrafficSign> findAll() {

        return this.get();
    }

    @Override
    protected TrafficSigns readData(Path activeVersion) throws IOException {
        return objectMapper.readValue(
                getCacheConfiguration().getActiveVersion().toPath().resolve("trafficSigns.json").toFile(),
                TrafficSigns.class);
    }

    @Override
    protected void writeData(Path target, TrafficSigns data) throws IOException {

        FileUtils.writeStringToFile(
                target.resolve(Path.of("trafficSigns.json")).toFile(),
                objectMapper.writeValueAsString(data),
                StandardCharsets.UTF_8);
    }

    public boolean dataExists() {
        return Files.exists(getCacheConfiguration().getActiveVersion().toPath());
    }
}
