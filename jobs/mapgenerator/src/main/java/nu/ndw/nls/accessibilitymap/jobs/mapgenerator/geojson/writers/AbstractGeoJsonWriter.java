package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.writers;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.dto.GeoGenerationProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.dto.FeatureCollection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.utils.LongSequenceSupplier;

@Slf4j
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractGeoJsonWriter {

    private final ObjectMapper geoJsonObjectMapper;

    private final GenerateConfiguration generateConfiguration;

    private final FileService fileService;

    public AbstractGeoJsonWriter(
            GenerateConfiguration generateConfiguration,
            GeoJsonObjectMapperFactory geoJsonObjectMapperFactory,
            FileService fileService) {

        this.generateConfiguration = generateConfiguration;
        this.fileService = fileService;
        geoJsonObjectMapper = geoJsonObjectMapperFactory.create(generateConfiguration);
    }

    public void writeToFile(
            Accessibility accessibility,
            GeoGenerationProperties geoGenerationProperties) {

        log.debug("Started generating geojson");

        String exportFileName = buildExportFileName(geoGenerationProperties);
        String exportFileExtension = ".geojson";

        Path tempFile = fileService.createTmpFile(exportFileName, exportFileExtension);

        LongSequenceSupplier idSequenceSupplier = new LongSequenceSupplier();

        try {
            log.debug("Started building features");
            FeatureCollection geoJson = prepareGeoJsonFeatureCollection(accessibility, geoGenerationProperties,
                    idSequenceSupplier);

            log.debug("Started writing geojson to temp file: %s".formatted(tempFile.toAbsolutePath()));
            getGeoJsonObjectMapper().writeValue(tempFile.toFile(), geoJson);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to serialize geojson to file: %s"
                    .formatted(tempFile.toAbsolutePath()), exception);
        }

        Path exportFile = geoGenerationProperties.generateConfiguration()
                .getGenerationDirectoryPath(geoGenerationProperties.startTime())
                .resolve(exportFileName.concat(exportFileExtension));

        log.debug("Moving geojson to: %s".formatted(exportFile.toAbsolutePath()));
        fileService.moveFileAndOverride(tempFile, exportFile);
    }

    protected abstract FeatureCollection prepareGeoJsonFeatureCollection(
            Accessibility accessibility,
            GeoGenerationProperties geoGenerationProperties,
            LongSequenceSupplier idSequenceSupplier);

    protected String buildExportFileName(GeoGenerationProperties geoGenerationProperties) {
        StringBuilder exportFileName = new StringBuilder();

        exportFileName.append(geoGenerationProperties.trafficSignType().name().toLowerCase(Locale.US));
        if (geoGenerationProperties.includeOnlyTimeWindowedSigns()) {
            exportFileName.append("WindowTimeSegments");
        }

        return exportFileName.toString();
    }

}
