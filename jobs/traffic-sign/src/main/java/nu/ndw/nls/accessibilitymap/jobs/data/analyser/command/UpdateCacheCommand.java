package nu.ndw.nls.accessibilitymap.jobs.data.analyser.command;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.network.GraphhopperMetaData;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.dto.TrafficSigns;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.TrafficSignCacheReadWriter;
import nu.ndw.nls.accessibilitymap.accessibility.utils.IntegerSequenceSupplier;
import nu.ndw.nls.accessibilitymap.jobs.data.analyser.cache.TrafficSignBuilder;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.services.TrafficSignService;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto.Id;
import nu.ndw.nls.db.nwb.jooq.services.NwbRoadSectionCrudService;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Slf4j
@Component
@Command(name = "update-cache")
@RequiredArgsConstructor
public class UpdateCacheCommand implements Callable<Integer> {

    private final TrafficSignCacheReadWriter trafficSignCacheReadWriter;

    private final TrafficSignService trafficSignService;

    private final TrafficSignBuilder trafficSignBuilder;

    private final NwbRoadSectionCrudService roadSectionService;

    private final GraphhopperMetaData graphhopperMetaData;

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
                            .map(trafficSignGeoJsonDto ->
                                    trafficSignBuilder.mapFromTrafficSignGeoJsonDto(
                                            getNwbRoadSectionGeometry(trafficSignGeoJsonDto),
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

    private LineString getNwbRoadSectionGeometry(TrafficSignGeoJsonDto trafficSignGeoJsonDto) {
        if (Objects.isNull(trafficSignGeoJsonDto.getProperties().getRoadSectionId())) {
            return null;
        }
        return roadSectionService.findById(
                        new Id(graphhopperMetaData.nwbVersion(), trafficSignGeoJsonDto.getProperties().getRoadSectionId()))
                .map(NwbRoadSectionDto::getGeometry)
                .orElse(null);
    }
}
