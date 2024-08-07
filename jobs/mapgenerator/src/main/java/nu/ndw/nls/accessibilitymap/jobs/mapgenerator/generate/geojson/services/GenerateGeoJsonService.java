package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.SortedMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.AccessibilityConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.model.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import nu.ndw.nls.accessibilitymap.accessibility.services.AccessibilityMapService;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.GenerateProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers.AccessibilityGeoJsonGeneratedEventMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers.AccessibilityGeoJsonMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers.LocalDateVersionMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.AccessibilityGeoJsonFeatureCollection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.GenerateGeoJsonType;
import nu.ndw.nls.events.NlsEvent;
import nu.ndw.nls.springboot.messaging.services.MessageService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenerateGeoJsonService {

    private final GenerateProperties generateProperties;

    private final GenerateConfiguration generateConfiguration;

    private final AccessibilityMapService accessibilityMapService;

    private final AccessibilityGeoJsonMapper accessibilityGeoJsonMapper;

    private final FileService uploadService;

    private final AccessibilityConfiguration accessibilityConfiguration;

    private final MessageService messageService;

    private final AccessibilityGeoJsonGeneratedEventMapper accessibilityGeoJsonGeneratedEventMapper;

    private final LocalDateVersionMapper localDateVersionMapper;

    public void generate(GenerateGeoJsonType type) {
        LocalDateTime versionLocalDateTime = LocalDateTime.now();
        int version = localDateVersionMapper.map(versionLocalDateTime.toLocalDate());
        int nwbVersion = accessibilityConfiguration.accessibilityGraphhopperMetaData().nwbVersion();

        log.info("Generating geojson: {} version: {} based on NWB version: {}", type, version, nwbVersion);

        VehicleProperties vehicleProperties = VehicleProperties.builder().hgvAccessForbidden(true).build();

        SortedMap<Integer, RoadSection> idToRoadSectionSortedMap =
                accessibilityMapService.determineAccessibilityByRoadSection(vehicleProperties,
                        generateConfiguration.getStartLocation(), generateProperties.getSearchDistanceInMeters());

        AccessibilityGeoJsonFeatureCollection geoJson = accessibilityGeoJsonMapper.map(idToRoadSectionSortedMap,
                nwbVersion);

        Path tempFile;
        try {
            tempFile = Files.createTempFile("accessibility", ".geojson");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create tmp file for geojson response", e);
        }

        try {
            generateConfiguration.getObjectMapper().writeValue(tempFile.toFile(), geoJson);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to serialize geojson to file: " + tempFile, e);
        }

        uploadService.uploadFile(type, tempFile, versionLocalDateTime.toLocalDate());

        NlsEvent nlsEvent = accessibilityGeoJsonGeneratedEventMapper.map(type, nwbVersion, nwbVersion,
                versionLocalDateTime.toInstant(ZoneOffset.UTC));

        log.debug("Sending {} created event for type {}, version {}, NWB version {} and traffic sign timestamp {}",
                nlsEvent.getType().getLabel(), nlsEvent.getSubject().getType(), version, nwbVersion,
                versionLocalDateTime);

        messageService.publish(nlsEvent);
    }

}