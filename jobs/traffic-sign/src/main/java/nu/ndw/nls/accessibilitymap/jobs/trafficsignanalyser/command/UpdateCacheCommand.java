package nu.ndw.nls.accessibilitymap.jobs.trafficsignanalyser.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.core.time.ClockService;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.configuration.TrafficSignCacheConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.dto.TrafficSigns;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.mappers.TrafficSignMapper;
import nu.ndw.nls.accessibilitymap.accessibility.utils.IntegerSequenceSupplier;
import nu.ndw.nls.accessibilitymap.trafficsignclient.services.TrafficSignService;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Slf4j
@Component
@Command(name = "update-cache")
@RequiredArgsConstructor
public class UpdateCacheCommand implements Callable<Integer> {

    private final TrafficSignCacheConfiguration trafficSignCacheConfiguration;

    private final TrafficSignService trafficSignService;

    private final TrafficSignMapper trafficSignMapper;

    private final ObjectMapper objectMapper;

    private final ClockService clockService;

    @Override
    public Integer call() {

        try {
            log.info("Updating traffic signs");

            IntegerSequenceSupplier idSupplier = new IntegerSequenceSupplier();

            TrafficSigns trafficSigns = new TrafficSigns();
            trafficSigns.addAll(trafficSignService.getTrafficSigns(Arrays.stream(TrafficSignType.values())
                            .map(TrafficSignType::getRvvCode)
                            .collect(Collectors.toSet()))
                    .trafficSignsByRoadSectionId().values().stream()
                    .flatMap(Collection::stream)
                    .map(trafficSignGeoJsonDto -> trafficSignMapper.mapFromTrafficSignGeoJsonDto(
                            trafficSignGeoJsonDto,
                            idSupplier))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList());

            Files.createDirectories(trafficSignCacheConfiguration.getFolder());

            Path targetRelativePath = Path.of(
                    "trafficSigns-%s.json".formatted(clockService.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
            File target = trafficSignCacheConfiguration.getFolder()
                    .resolve(targetRelativePath)
                    .toFile();

            log.info("Writing traffic signs to file: {}", target.getAbsolutePath());
            FileUtils.writeStringToFile(target, objectMapper.writeValueAsString(trafficSigns), StandardCharsets.UTF_8);

            switchSymLink(targetRelativePath.toFile());
            return 0;
        } catch (Exception exception) {
            log.error("Failed updating traffic signs", exception);
            return 1;
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
