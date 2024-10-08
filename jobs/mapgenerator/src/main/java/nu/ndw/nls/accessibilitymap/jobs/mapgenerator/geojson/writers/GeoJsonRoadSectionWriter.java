package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.writers;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.dto.GeoGenerationProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.model.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.model.RoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.model.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.model.Feature;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.model.FeatureCollection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.model.LineStringGeometry;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.model.PointGeometry;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.model.RoadSectionProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.model.TrafficSignProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.util.LongSequenceSupplier;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSign;
import nu.ndw.nls.geometry.distance.FractionAndDistanceCalculator;
import nu.ndw.nls.geometry.geojson.mappers.GeoJsonLineStringCoordinateMapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class GeoJsonRoadSectionWriter implements OutputWriter {

    private final ObjectMapper geoJsonObjectMapper;

    private final GeoJsonLineStringCoordinateMapper geoJsonLineStringCoordinateMapper;

    private final FileService fileService;

    private final FractionAndDistanceCalculator fractionAndDistanceCalculator;

    private static final double TRAFFIC_SIGN_LINE_STRING_DISTANCE_IN_METERS = 1;

    public GeoJsonRoadSectionWriter(
            GeoJsonLineStringCoordinateMapper geoJsonLineStringCoordinateMapper,
            FileService fileService,
            FractionAndDistanceCalculator fractionAndDistanceCalculator,
            GenerateConfiguration generateConfiguration) {

        this.geoJsonLineStringCoordinateMapper = geoJsonLineStringCoordinateMapper;
        this.fileService = fileService;
        this.fractionAndDistanceCalculator = fractionAndDistanceCalculator;
        geoJsonObjectMapper = createGeoJsonObjectMapper(generateConfiguration);
    }

    @NotNull
    private ObjectMapper createGeoJsonObjectMapper(GenerateConfiguration generateConfiguration) {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Include.NON_NULL);

        if (generateConfiguration.isPrettyPrintJson()) {
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
                .features(accessibility.mergedAccessibility().stream()
                        .map(roadSection -> createFeatures(roadSection, idSequenceSupplier))
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
        fileService.moveFile(tempFile, exportFile);
    }

    private List<Feature> createFeatures(
            RoadSection roadSection,
            LongSequenceSupplier idSequenceSupplier) {

        return roadSection.getRoadSectionFragments().stream()
                .flatMap(roadSectionFragment -> roadSectionFragment.getSegments().stream())
                .filter(Objects::nonNull)
                .map(directionalSegment -> {
                    List<Feature> features = new ArrayList<>();

                    features.add(buildRoadSection(idSequenceSupplier, directionalSegment));
                    if (directionalSegment.hasTrafficSign()) {
                        features.add(buildTrafficSign(
                                idSequenceSupplier,
                                directionalSegment.getTrafficSign(),
                                directionalSegment));
                        features.add(addTrafficSignAsPoint(
                                idSequenceSupplier,
                                directionalSegment.getTrafficSign(),
                                directionalSegment));
                    }

                    return features;
                })
                .flatMap(Collection::stream)
                .toList();
    }

    private Feature addTrafficSignAsPoint(
            LongSequenceSupplier geoJsonIdSequenceSupplier,
            TrafficSign trafficSign,
            DirectionalSegment directionalSegment) {

        return Feature.builder()
                .id(geoJsonIdSequenceSupplier.next())
                .geometry(PointGeometry
                        .builder()
                        .coordinates(List.of(
                                directionalSegment.getLineString().getStartPoint().getX(),
                                directionalSegment.getLineString().getStartPoint().getY()))
                        .build())
                .properties(buildTrafficSignProperties(trafficSign, directionalSegment))
                .build();
    }

    private Feature buildTrafficSign(
            LongSequenceSupplier geoJsonIdSequenceSupplier,
            TrafficSign trafficSign,
            DirectionalSegment directionalSegment) {

        return Feature.builder()
                .id(geoJsonIdSequenceSupplier.next())
                .geometry(LineStringGeometry
                        .builder()
                        .coordinates(geoJsonLineStringCoordinateMapper.map(
                                fractionAndDistanceCalculator.getSubLineStringByLengthInMeters(
                                        directionalSegment.getLineString(),
                                        TRAFFIC_SIGN_LINE_STRING_DISTANCE_IN_METERS)))
                        .build())
                .properties(buildTrafficSignProperties(trafficSign, directionalSegment))
                .build();
    }

    private TrafficSignProperties buildTrafficSignProperties(
            TrafficSign trafficSign,
            DirectionalSegment directionalSegment) {
        return TrafficSignProperties
                .builder()
                .nwbRoadSectionId(trafficSign.roadSectionId())
                .direction(trafficSign.direction())
                .accessible(directionalSegment.isAccessible())
                .trafficSignType(trafficSign.trafficSignType())
                .windowTimes(trafficSign.findFirstTimeWindowedSign()
                        .map(TextSign::getText)
                        .orElse(null))
                .iconUrl(trafficSign.iconUri())
                .isTrafficSign(true)
                .build();
    }

    private Feature buildRoadSection(
            LongSequenceSupplier geoJsonIdSequenceSupplier,
            DirectionalSegment directionalSegment) {

        return Feature.builder()
                .id(geoJsonIdSequenceSupplier.next())
                .geometry(LineStringGeometry
                        .builder()
                        .coordinates(geoJsonLineStringCoordinateMapper.map(directionalSegment.getLineString()))
                        .build())
                .properties(RoadSectionProperties
                        .builder()
                        .nwbRoadSectionId(directionalSegment.getRoadSectionFragment().getRoadSection().getId())
                        .direction(directionalSegment.getDirection())
                        .accessible(directionalSegment.isAccessible())
                        .build())
                .build();
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
