package nu.ndw.nls.accessibilitymap.job.trafficsign.command;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.network.NetworkDataService;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.dto.TrafficSigns;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.TrafficSignDataService;
import nu.ndw.nls.accessibilitymap.job.trafficsign.cache.TrafficSignBuilder;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignData;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.services.TrafficSignService;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Slf4j
@Component
@Command(name = "update-cache")
@RequiredArgsConstructor
public class UpdateCacheCommand implements Callable<Integer> {

    private final TrafficSignDataService trafficSignDataService;

    private final TrafficSignService trafficSignService;

    private final TrafficSignBuilder trafficSignBuilder;

    private final NetworkDataService networkDataService;

    @Override
    public Integer call() {

        try {
            log.info("Updating traffic signs");

            AtomicInteger idSupplier = new AtomicInteger();

            TrafficSignData externalTrafficSigns = trafficSignService.getTrafficSigns(Arrays.stream(TrafficSignType.values())
                    .map(TrafficSignType::getRvvCode)
                    .collect(Collectors.toSet()));
            TrafficSigns trafficSigns = new TrafficSigns(
                    externalTrafficSigns
                            .trafficSignsByRoadSectionId().values().stream()
                            .flatMap(Collection::stream)
                            .map(trafficSignGeoJsonDto ->
                                    trafficSignBuilder.mapFromTrafficSignGeoJsonDto(
                                            getNwbRoadSectionGeometry(trafficSignGeoJsonDto),
                                            trafficSignGeoJsonDto,
                                            idSupplier))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .toList());

            log.info("Downloaded {} traffic signs", trafficSigns.size());

            trafficSignDataService.write(trafficSigns);
            return 0;
        } catch (Exception exception) {
            log.error("Failed updating traffic signs", exception);
            return 1;
        }
    }

    private LineString getNwbRoadSectionGeometry(TrafficSignGeoJsonDto trafficSignGeoJsonDto) {

        return networkDataService.get().getNwbData()
                .findAccessibilityNwbRoadSectionById(trafficSignGeoJsonDto.getProperties().getRoadSectionId())
                .map(AccessibilityNwbRoadSection::geometry)
                .orElse(null);
    }
}
