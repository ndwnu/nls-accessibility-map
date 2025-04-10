package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.time.ClockService;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.configuration.TrafficSignCacheConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.dto.TrafficSigns;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrafficSignCacheReadWriter {

    private static final BigDecimal BINARY_KILO = BigDecimal.valueOf(1024);

    private static final int SIZE_ROUNDING = 2;

    private final TrafficSignCacheConfiguration trafficSignCacheConfiguration;

    private final ObjectMapper objectMapper;

    private final ClockService clockService;

    public Optional<TrafficSigns> read() {

        try {
            OffsetDateTime start = OffsetDateTime.now();
            TrafficSigns trafficSigns = objectMapper.readValue(trafficSignCacheConfiguration.getActiveVersion(), TrafficSigns.class);
            log.info("Read traffic signs data from `{}` with size {}MB in {} ms",
                    trafficSignCacheConfiguration.getActiveVersion().toPath().toAbsolutePath(),
                    BigDecimal.valueOf(Files.size(trafficSignCacheConfiguration.getActiveVersion().toPath()))
                            .divide(BINARY_KILO.multiply(BINARY_KILO), SIZE_ROUNDING, RoundingMode.HALF_UP),
                    Duration.between(start, OffsetDateTime.now()).toMillis());

            return Optional.ofNullable(trafficSigns);
        } catch (IOException exception) {
            log.error("Failed to read traffic signs from file", exception);
            if (trafficSignCacheConfiguration.isFailOnNoDataOnStartup()) {
                throw new IllegalStateException("Failed to read traffic signs from file", exception);
            }
            return Optional.empty();
        }
    }

    public void write(TrafficSigns trafficSigns) {
        try {
            Files.createDirectories(trafficSignCacheConfiguration.getFolder());

            Path targetRelativePath = Path.of(
                    "trafficSigns-%s.json".formatted(clockService.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
            File target = trafficSignCacheConfiguration.getFolder()
                    .resolve(targetRelativePath)
                    .toFile();

            log.info("Writing traffic signs to file: {}", target.getAbsolutePath());
            FileUtils.writeStringToFile(target, objectMapper.writeValueAsString(trafficSigns), StandardCharsets.UTF_8);

            switchSymLink(targetRelativePath.toFile());
        } catch (IOException exception) {
            log.error("Failed to write traffic signs to file", exception);
        }
    }

    private void switchSymLink(File target) throws IOException {

        Path symlink = trafficSignCacheConfiguration.getActiveVersion().toPath();
        Path oldTarget = null;

        if (Files.isSymbolicLink(symlink)) {
            if (Files.exists(symlink)) {
                oldTarget = symlink.toRealPath();
            }
            Files.delete(symlink);
        }

        Files.createSymbolicLink(symlink, target.toPath());
        log.info("Updated symlink: %s".formatted(trafficSignCacheConfiguration.getActiveVersion().getAbsolutePath()));

        if (Objects.nonNull(oldTarget)) {
            Files.deleteIfExists(oldTarget);
            log.info("Removed old symlink target: %s".formatted(oldTarget.toAbsolutePath()));
        }
    }
}
