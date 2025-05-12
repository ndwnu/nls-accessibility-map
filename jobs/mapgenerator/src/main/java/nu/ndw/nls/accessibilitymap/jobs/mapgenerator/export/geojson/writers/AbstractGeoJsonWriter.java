package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.writers;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.accessibility.utils.LongSequenceSupplier;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.dto.ExportProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.Exporter;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.dto.FeatureCollection;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignType;

@Slf4j
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractGeoJsonWriter implements Exporter {

    private final ObjectMapper geoJsonObjectMapper;

    private final GenerateConfiguration generateConfiguration;

    private final FileService fileService;

    protected AbstractGeoJsonWriter(
            GenerateConfiguration generateConfiguration,
            GeoJsonObjectMapperFactory geoJsonObjectMapperFactory,
            FileService fileService) {

        this.generateConfiguration = generateConfiguration;
        this.fileService = fileService;
        geoJsonObjectMapper = geoJsonObjectMapperFactory.create(generateConfiguration);
    }

    public void export(
            Accessibility accessibility,
            ExportProperties exportProperties) {

        log.debug("Started generating geojson");

        String exportFileName = buildExportFileName(exportProperties);
        String exportFileExtension = ".geojson";

        Path tempFile = fileService.createTmpFile(exportFileName, exportFileExtension);

        LongSequenceSupplier idSequenceSupplier = new LongSequenceSupplier();

        try {
            log.debug("Started building features");
            FeatureCollection geoJson = prepareGeoJsonFeatureCollection(accessibility, exportProperties,
                    idSequenceSupplier);

            log.debug("Started writing geojson to temp file: %s".formatted(tempFile.toAbsolutePath()));
            getGeoJsonObjectMapper().writeValue(tempFile.toFile(), geoJson);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to serialize geojson to file: %s"
                    .formatted(tempFile.toAbsolutePath()), exception);
        }

        Path exportFile = exportProperties.generateConfiguration()
                .getGenerationDirectoryPath(exportProperties.startTime())
                .resolve(exportFileName.concat(exportFileExtension));

        log.debug("Moving geojson to: %s".formatted(exportFile.toAbsolutePath()));
        fileService.moveFileAndOverride(tempFile, exportFile);
    }

    protected abstract FeatureCollection prepareGeoJsonFeatureCollection(
            Accessibility accessibility,
            ExportProperties exportProperties,
            LongSequenceSupplier idSequenceSupplier);

    protected String buildExportFileName(ExportProperties exportProperties) {
        StringBuilder exportFileName = new StringBuilder();

        exportFileName.append(exportProperties.name().toLowerCase(Locale.US));
        if (Objects.nonNull(exportProperties.accessibilityRequest().trafficSignTextSignTypes())
                && exportProperties.accessibilityRequest().trafficSignTextSignTypes().contains(TextSignType.TIME_PERIOD)) {
            exportFileName.append("WindowTimeSegments");
        }

        return exportFileName.toString();
    }
}
