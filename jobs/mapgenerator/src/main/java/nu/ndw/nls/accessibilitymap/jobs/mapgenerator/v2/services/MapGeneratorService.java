package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.services;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.AccessibilityConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.model.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import nu.ndw.nls.accessibilitymap.accessibility.services.AccessibilityMapService;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.GenerateProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.commands.model.CmdGenerateGeoJsonType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers.LocalDateVersionMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers.VehicleTypeVehiclePropertiesMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.MapGenerationProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.nwb.services.NdwDataService;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.services.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.trafficsign.services.TrafficSignDataService;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.writers.OutputWriter;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MapGeneratorService {

    private final LocalDateVersionMapper localDateVersionMapper;

    private final GenerateProperties generateProperties;

    private final GenerateConfiguration generateConfiguration;

    private final AccessibilityMapService accessibilityMapService;

    private final VehicleTypeVehiclePropertiesMapper vehicleTypeVehiclePropertiesMapper;

    private final AccessibilityConfiguration accessibilityConfiguration;

    private final TrafficSignDataService trafficSignFactory;

    private final NdwDataService ndwDataService;

    private final List<OutputWriter> outputWriters;

    private final AccessibilityService accessibilityService;

    public void generate(@Valid MapGenerationProperties mapGenerationProperties) {

        OffsetDateTime startTime = OffsetDateTime.now();

        if (mapGenerationProperties.getTrafficSigns().size() != 1) {
            throw new IllegalArgumentException("Exactly one traffic sign is supported right now.");
        }

        // TODO: Move this section the command
        mapGenerationProperties.setExportVersion(localDateVersionMapper.map(LocalDateTime.now().toLocalDate()));
        mapGenerationProperties.setNwbVersion(
                accessibilityConfiguration.accessibilityGraphhopperMetaData().nwbVersion());

        log.info("Generating geojson: {} version: {} based on NWB version: {}",
                mapGenerationProperties.getTrafficSigns().stream()
                        .map(Enum::name)
                        .collect(Collectors.joining("-")),
                mapGenerationProperties.getExportVersion(),
                mapGenerationProperties.getNwbVersion());

        CmdGenerateGeoJsonType cmdGenerateGeoJsonType = CmdGenerateGeoJsonType.valueOf(
                mapGenerationProperties.getTrafficSigns().stream()
                        .map(Enum::name)
                        .findFirst()
                        .orElseThrow()
        );

        Accessibility accessibility = getAccessibility(cmdGenerateGeoJsonType);

        // TODO: Fix this
//        trafficSignFactory.addTrafficSignDataToRoadSections(accessibility.mergedAccessibility(), mapGenerationProperties);
        ndwDataService.addNdwDataToRoadSections(accessibility.mergedAccessibility(), mapGenerationProperties.getNwbVersion());

        log.info("Map generation done. It took: %s ms".formatted(
                ChronoUnit.MILLIS.between(startTime, OffsetDateTime.now())));
//
//        List<RoadSection> roadSectionsWithTrafficSigns = inaccessibleRoadSections.stream()
//                .filter(roadSection ->
//                        !roadSection.getForward().getTrafficSigns().isEmpty()
//                                || (!roadSection.isOneWay() && !roadSection.getBackward().getTrafficSigns().isEmpty()))
//                .toList();
//        log.info("Found {} with road sections with traffic signs. {}", roadSectionsWithTrafficSigns.size(),
//                roadSectionsWithTrafficSigns);

        outputWriters.forEach(
                outputWriter -> outputWriter.writeToFile(accessibility, mapGenerationProperties));
    }

    private Accessibility getAccessibility(CmdGenerateGeoJsonType cmdGenerateGeoJsonType) {

        VehicleProperties vehicleProperties = vehicleTypeVehiclePropertiesMapper.map(cmdGenerateGeoJsonType);
       return accessibilityService.calculateAccessibility(
                AccessibilityRequest.builder()
                        .vehicleProperties(vehicleProperties)
                        .startPoint(generateConfiguration.getStartLocation())
                        .searchDistanceInMetres(generateProperties.getSearchDistanceInMeters())
                        .build(),
                List.of()); // TODO add traffic sign snaps here.

    }

    private boolean isInaccessible(nu.ndw.nls.accessibilitymap.accessibility.model.RoadSection roadSection) {
        return (roadSection.getBackwardAccessible() != null && !roadSection.getBackwardAccessible())
                || (roadSection.getForwardAccessible() != null && !roadSection.getForwardAccessible());
    }


}
