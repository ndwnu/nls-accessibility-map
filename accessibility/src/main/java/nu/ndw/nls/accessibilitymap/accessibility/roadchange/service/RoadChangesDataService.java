package nu.ndw.nls.accessibilitymap.accessibility.roadchange.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.cache.Cache;
import nu.ndw.nls.accessibilitymap.accessibility.roadchange.configuration.RoadChangesCacheConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.roadchange.dto.RoadChanges;
import nu.ndw.nls.db.nwb.jooq.services.NwbVersionCrudService;
import nu.ndw.nls.springboot.core.time.ClockService;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RoadChangesDataService extends Cache<RoadChanges> {

    private final NwbVersionCrudService nwbVersionCrudService;

    private final ObjectMapper objectMapper;

    private static final String NWB_CHANGED_ROAD_SECTIONS_JSON = "nwb_changed_road_sections.json";

    public RoadChangesDataService(RoadChangesCacheConfiguration cacheConfiguration,
            ClockService clockService,
            NwbVersionCrudService nwbVersionCrudService, ObjectMapper objectMapper
    ) {
        super(cacheConfiguration, clockService);
        this.nwbVersionCrudService = nwbVersionCrudService;
        this.objectMapper = objectMapper;
    }

    public void createEmptyCache() {
        write(new RoadChanges(nwbVersionCrudService.findLatestVersionId(), List.of()));
    }

    @Override
    public void write(RoadChanges roadChanges) {
        try {

            if (dataExists()) {
                Path activeVersion = getCacheConfiguration().getActiveVersion().toPath().toAbsolutePath().toRealPath();
                RoadChanges previousChanges = readData(activeVersion);
                RoadChanges newRoadChanges =
                        previousChanges.isSameVersion(roadChanges) ? (previousChanges.merge(roadChanges)) : roadChanges;
                super.write(newRoadChanges);
            } else {
                super.write(roadChanges);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected RoadChanges readData(Path activeVersion) throws IOException {
        OffsetDateTime start = getClockService().now();
        RoadChanges roadChanges = objectMapper.readValue(
                getCacheConfiguration().getActiveVersion().toPath().resolve(NWB_CHANGED_ROAD_SECTIONS_JSON).toFile(),
                RoadChanges.class);
        log.info("Nwb changed road sections loaded from disk in {}ms", Duration.between(start, getClockService().now()).toMillis());
        return roadChanges;
    }

    @Override
    protected void writeData(Path target, RoadChanges data) throws IOException {
        FileUtils.writeStringToFile(
                target.resolve(Path.of(NWB_CHANGED_ROAD_SECTIONS_JSON)).toFile(),
                objectMapper.writeValueAsString(data),
                StandardCharsets.UTF_8);
    }
}
