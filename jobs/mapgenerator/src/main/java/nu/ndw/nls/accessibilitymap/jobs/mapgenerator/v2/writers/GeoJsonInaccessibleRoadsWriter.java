package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.writers;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.commands.model.CmdGenerateGeoJsonType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.services.FileService;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.suppliers.GeoJsonIdSequenceSupplier;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.MapGenerationProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.OutputFormat;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.RoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.geojson.AccessibilityGeoJsonFeature;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.geojson.AccessibilityGeoJsonFeatureCollection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.geojson.AccessibilityGeoJsonProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.geojson.LineStringGeojson;
import nu.ndw.nls.geometry.geojson.mappers.GeoJsonLineStringCoordinateMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GeoJsonInaccessibleRoadsWriter implements OutputWriter {

    private final FileService uploadService;
    private final GenerateConfiguration generateConfiguration;
    private final GeoJsonLineStringCoordinateMapper geoJsonLineStringCoordinateMapper;

    @Override
    public void writeToFile(List<RoadSection> roadSections,
            MapGenerationProperties mapGenerationProperties) {

        CmdGenerateGeoJsonType type = CmdGenerateGeoJsonType.valueOf(
                mapGenerationProperties.getTrafficSigns().stream()
                        .map(Enum::name)
                        .findFirst()
                        .orElseThrow());
        Path tempFile = uploadService.createTmpGeoJsonFile(type);
        GeoJsonIdSequenceSupplier geoJsonIdSequenceSupplier = new GeoJsonIdSequenceSupplier();
        List<AccessibilityGeoJsonFeature> features = roadSections
                .stream()
                .map(r -> mapToFeatures(r, mapGenerationProperties, geoJsonIdSequenceSupplier))
                .flatMap(List::stream)
                .toList();
        AccessibilityGeoJsonFeatureCollection geoJson = AccessibilityGeoJsonFeatureCollection
                .builder()
                .features(features)
                .build();

        try {
            generateConfiguration.getObjectMapper().writeValue(tempFile.toFile(), geoJson);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to serialize geojson to file: " + tempFile, e);
        }

        uploadService.uploadFile(type, tempFile, LocalDateTime.now().toLocalDate());

    }

    private List<AccessibilityGeoJsonFeature> mapToFeatures(RoadSection roadSection,
            MapGenerationProperties mapGenerationProperties, GeoJsonIdSequenceSupplier geoJsonIdSequenceSupplier) {
        return Stream.of(roadSection.getBackward(),
                        roadSection.getForward())
                .filter(Objects::nonNull)
                .map(directionalSegment -> AccessibilityGeoJsonFeature
                        .builder()
                        .id(geoJsonIdSequenceSupplier.next())
                        .geometry(LineStringGeojson
                                .builder()
                                .coordinates(geoJsonLineStringCoordinateMapper.map(directionalSegment.getLineString()))
                                .build())
                        .properties(AccessibilityGeoJsonProperties
                                .builder()
                                .id(roadSection.getRoadSectionId())
                                .direction(directionalSegment.getDirection())
                                .trafficSignType(
                                        directionalSegment.hasTrafficSigns() ? directionalSegment.getTrafficSigns()
                                                .getFirst().trafficSignType() : null)
                                .windowTimes(directionalSegment.hasTrafficSigns() ? directionalSegment.getTrafficSigns()
                                        .getFirst().windowTimes() : null)
                                .accessible(directionalSegment.isAccessible())
                                .build())
                        .build()).toList();
    }

    @Override
    public OutputFormat getOutputFormat() {
        return OutputFormat.GEO_JSON_INACCESSIBLE;
    }
}
