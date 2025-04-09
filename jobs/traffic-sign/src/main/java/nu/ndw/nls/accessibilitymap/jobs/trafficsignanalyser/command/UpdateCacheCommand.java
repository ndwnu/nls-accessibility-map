package nu.ndw.nls.accessibilitymap.jobs.trafficsignanalyser.command;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.dto.TrafficSigns;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.TrafficSignCacheReadWriter;
import nu.ndw.nls.accessibilitymap.accessibility.utils.IntegerSequenceSupplier;
import nu.ndw.nls.accessibilitymap.jobs.trafficsignanalyser.cache.TrafficSignMapper;
import nu.ndw.nls.accessibilitymap.trafficsignclient.services.TrafficSignService;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Slf4j
@Component
@Command(name = "update-cache")
@RequiredArgsConstructor
public class UpdateCacheCommand implements Callable<Integer> {

    private final TrafficSignCacheReadWriter trafficSignCacheReadWriter;

    private final TrafficSignService trafficSignService;

    private final TrafficSignMapper trafficSignMapper;

    @Override
    public Integer call() {

        try {
            log.info("Updating traffic signs");

            IntegerSequenceSupplier idSupplier = new IntegerSequenceSupplier();

            TrafficSigns trafficSigns = new TrafficSigns(
                    trafficSignService.getTrafficSigns(Arrays.stream(TrafficSignType.values())
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

            trafficSignCacheReadWriter.write(trafficSigns);
            return 0;
        } catch (Exception exception) {
            log.error("Failed updating traffic signs", exception);
            return 1;
        }
    }

}
