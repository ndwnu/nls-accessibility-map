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
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.GenerateProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.commands.model.CmdGenerateGeoJsonType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers.LocalDateVersionMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers.VehicleTypeVehiclePropertiesMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.accessibility.AccessibilityService;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.accessibility.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.accessibility.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.MapGenerationProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.nwb.services.NdwDataService;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.writers.OutputWriter;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MapGeneratorService {

    private final LocalDateVersionMapper localDateVersionMapper;

    private final GenerateProperties generateProperties;

    private final GenerateConfiguration generateConfiguration;

    private final VehicleTypeVehiclePropertiesMapper vehicleTypeVehiclePropertiesMapper;

    private final AccessibilityConfiguration accessibilityConfiguration;

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

        Accessibility accessibility = getAccessibility(mapGenerationProperties);

        log.info("Map generation done. It took: %s ms".formatted(
                ChronoUnit.MILLIS.between(startTime, OffsetDateTime.now())));

        long roadSectionsWithTrafficSigns = accessibility.mergedAccessibility().stream()
                .flatMap(roadSection -> roadSection.getSegments().stream())
                .filter(segment -> !segment.getTrafficSigns().isEmpty())
                .count();
        log.info("Found {} with road sections with traffic signs.", roadSectionsWithTrafficSigns);

        outputWriters.forEach(
                outputWriter -> outputWriter.writeToFile(accessibility, mapGenerationProperties));
    }

    private Accessibility getAccessibility(@Valid MapGenerationProperties mapGenerationProperties) {

        CmdGenerateGeoJsonType cmdGenerateGeoJsonType = CmdGenerateGeoJsonType.valueOf(
                mapGenerationProperties.getTrafficSigns().stream()
                        .map(Enum::name)
                        .findFirst()
                        .orElseThrow()
        );

        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .vehicleProperties(vehicleTypeVehiclePropertiesMapper.map(cmdGenerateGeoJsonType))
                .startPoint(generateConfiguration.getStartLocation())
                .searchDistanceInMetres(generateProperties.getSearchDistanceInMeters())
                .build();

        Accessibility accessibility = accessibilityService.calculateAccessibility(accessibilityRequest);

        // TODO: can be moved to accessibilityService?
        ndwDataService.addNwbDataToAccessibility(accessibility, mapGenerationProperties.getNwbVersion());

        return accessibility;
    }
}
