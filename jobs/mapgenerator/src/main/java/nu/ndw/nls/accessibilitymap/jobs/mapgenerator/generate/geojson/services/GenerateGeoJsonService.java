package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.SortedMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.AccessibilityConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.model.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import nu.ndw.nls.accessibilitymap.accessibility.services.AccessibilityMapService;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.GenerateProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers.AccessibilityGeoJsonMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.AccessibilityGeoJsonFeatureCollection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.GenerateGeoJsonType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.properties.GeoJsonProperties;
import nu.ndw.nls.accessibilitymap.shared.network.services.NetworkMetaDataService;
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

    public void generate(GenerateGeoJsonType type) {
        log.info("Generating geojson {} (still needs to be implemented)", type);

        GeoJsonProperties configuration = generateConfiguration.getConfiguration(type);

        VehicleProperties vehicleProperties = VehicleProperties.builder().carAccessForbidden(true).build();

        SortedMap<Integer, RoadSection> idToRoadSectionSortedMap =
                accessibilityMapService.determineAccessibilityByRoadSection(vehicleProperties,
                        generateConfiguration.getStartLocation(), generateProperties.getSearchDistanceInMeters());

        int nwbVersion = accessibilityConfiguration.accessibilityGraphhopperMetaData().nwbVersion();

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

        uploadService.uploadFile(type, tempFile);
    }

}