package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.writers;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.dto.GeoGenerationProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.model.RoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.model.Feature;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.model.FeatureCollection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.util.LongSequenceSupplier;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class GeoJsonRoadSectionWriter implements OutputWriter {

    private final ObjectMapper geoJsonObjectMapper;


    private final FileService fileService;

    private final FeatureBuilder featureBuilder;


    public GeoJsonRoadSectionWriter(
            FileService fileService,
            FeatureBuilder featureBuilder,
            GenerateConfiguration generateConfiguration) {

        this.featureBuilder = featureBuilder;
        this.fileService = fileService;
        geoJsonObjectMapper = createGeoJsonObjectMapper(generateConfiguration);
    }

    @NotNull
    private ObjectMapper createGeoJsonObjectMapper(GenerateConfiguration generateConfiguration) {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Include.NON_NULL);

        if (generateConfiguration.prettyPrintJson()) {
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        }

        return objectMapper;
    }

    @Override
    public void writeToFile(
            Accessibility accessibility,
            GeoGenerationProperties geoGenerationProperties) {

        String exportFileName = buildExportFileName(geoGenerationProperties);
        String exportFileExtension = ".geojson";

        Path tempFile = fileService.createTmpFile(exportFileName, exportFileExtension);

        LongSequenceSupplier idSequenceSupplier = new LongSequenceSupplier();

        FeatureCollection geoJson = FeatureCollection
                .builder()
                .features(accessibility.combinedAccessibility().stream()
                        .map(roadSection -> createFeatures(
                                roadSection,
                                idSequenceSupplier,
                                geoGenerationProperties.generateConfiguration()))
                        .flatMap(List::stream)
                        .toList())
                .build();

        try {
            geoJsonObjectMapper.writeValue(tempFile.toFile(), geoJson);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to serialize geojson to file: " + tempFile, e);
        }

        Path exportFile = geoGenerationProperties.generateConfiguration()
                .getGenerationDirectionPath(geoGenerationProperties)
                .resolve(exportFileName.concat(exportFileExtension));
        fileService.moveFileAndOverride(tempFile, exportFile);
    }

    private List<Feature> createFeatures(
            RoadSection roadSection,
            LongSequenceSupplier idSequenceSupplier,
            GenerateConfiguration generateConfiguration) {

        return roadSection.getRoadSectionFragments().stream()
                .flatMap(roadSectionFragment -> roadSectionFragment.getSegments().stream())
                .filter(Objects::nonNull)
                .map(directionalSegment -> featureBuilder.buildFeaturesForDirectionalSegment(
                        directionalSegment,
                        idSequenceSupplier,
                        generateConfiguration))
                .flatMap(Collection::stream)
                .toList();

    }

    private String buildExportFileName(GeoGenerationProperties geoGenerationProperties) {
        StringBuilder exportFileName = new StringBuilder();

        exportFileName.append(geoGenerationProperties.trafficSignType().name().toLowerCase());
        if (geoGenerationProperties.includeOnlyTimeWindowedSigns()) {
            exportFileName.append("WindowTimeSegments");
        }

        return exportFileName.toString();
    }
}
