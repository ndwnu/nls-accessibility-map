package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.geojson.writers;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.commands.model.CmdGenerateGeoJsonType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.services.FileService;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.suppliers.GeoJsonIdSequenceSupplier;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.accessibility.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.geojson.model.AccessibilityGeoJsonFeature;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.geojson.model.AccessibilityGeoJsonFeatureCollection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.geojson.model.AccessibilityGeoJsonProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.geojson.model.LineStringGeometry;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.geojson.model.PointGeometry;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.MapGenerationProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.RoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSign;
import nu.ndw.nls.geometry.distance.FractionAndDistanceCalculator;
import nu.ndw.nls.geometry.geojson.mappers.GeoJsonLineStringCoordinateMapper;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GeoJsonRoadSectionWriter implements OutputWriter {

    private final FileService uploadService;
    private final GenerateConfiguration generateConfiguration;
    private final GeoJsonLineStringCoordinateMapper geoJsonLineStringCoordinateMapper;
    private final FractionAndDistanceCalculator fractionAndDistanceCalculator;

    @Override
    public void writeToFile(Accessibility accessibility,
            MapGenerationProperties mapGenerationProperties) {

        CmdGenerateGeoJsonType type = CmdGenerateGeoJsonType.valueOf(
                mapGenerationProperties.getTrafficSigns().stream()
                        .map(Enum::name)
                        .findFirst()
                        .orElseThrow());

        Path tempFile = uploadService.createTmpGeoJsonFile(type);

        GeoJsonIdSequenceSupplier geoJsonIdSequenceSupplier = new GeoJsonIdSequenceSupplier();

        AccessibilityGeoJsonFeatureCollection geoJson = AccessibilityGeoJsonFeatureCollection
                .builder()
                .features(accessibility.mergedAccessibility().stream()
                        .map(roadSection -> createFeatures(roadSection, geoJsonIdSequenceSupplier))
                        .flatMap(List::stream)
                        .toList())
                .build();

        try {
            generateConfiguration.getObjectMapper().writeValue(tempFile.toFile(), geoJson);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to serialize geojson to file: " + tempFile, e);
        }

        uploadService.uploadFile(type, tempFile, LocalDateTime.now().toLocalDate());
    }

    private List<AccessibilityGeoJsonFeature> createFeatures(
            RoadSection roadSection,
            GeoJsonIdSequenceSupplier geoJsonIdSequenceSupplier) {

        return roadSection.getSegments().stream()
                .filter(Objects::nonNull)
                .map(directionalSegment -> {
                    List<AccessibilityGeoJsonFeature> features = new ArrayList<>();

                    features.add(buildLineString(roadSection, geoJsonIdSequenceSupplier, directionalSegment));
                    features.addAll(directionalSegment.getTrafficSigns().stream()
                            .map(trafficSign ->
                                    buildPoint(geoJsonIdSequenceSupplier, trafficSign, directionalSegment))
                            .toList());

                    return features;
                })
                .flatMap(Collection::stream)
                .toList();
    }

    private AccessibilityGeoJsonFeature buildPoint(
            GeoJsonIdSequenceSupplier geoJsonIdSequenceSupplier,
            TrafficSign trafficSign,
            DirectionalSegment directionalSegment) {

        LineString trafficSignLineString = fractionAndDistanceCalculator.getSubLineString(
                directionalSegment.getLineString(),
                trafficSign.fraction());

        return AccessibilityGeoJsonFeature.builder()
                .id(geoJsonIdSequenceSupplier.next())
                .geometry(PointGeometry
                        .builder()
                        .coordinates(List.of(
                                trafficSignLineString.getEndPoint().getX(),
                                trafficSignLineString.getEndPoint().getY()))
                        .build())
                .properties(AccessibilityGeoJsonProperties
                        .builder()
                        .trafficSignType(trafficSign.trafficSignType())
                        .windowTimes(buildWindowTime(trafficSign))
                        .iconUrl(trafficSign.iconUri())
                        .build())
                .build();
    }

    private AccessibilityGeoJsonFeature buildLineString(
            RoadSection roadSection,
            GeoJsonIdSequenceSupplier geoJsonIdSequenceSupplier,
            DirectionalSegment directionalSegment) {

        return AccessibilityGeoJsonFeature.builder()
                .id(geoJsonIdSequenceSupplier.next())
                .geometry(LineStringGeometry
                        .builder()
                        .coordinates(geoJsonLineStringCoordinateMapper.map(directionalSegment.getLineString()))
                        .build())
                .properties(AccessibilityGeoJsonProperties
                        .builder()
                        .id(roadSection.getRoadSectionId())
                        .direction(directionalSegment.getDirection())
                        .trafficSignType(
                                directionalSegment.hasTrafficSigns()
                                        ? directionalSegment.getTrafficSigns().getFirst().trafficSignType()
                                        : null)
                        .windowTimes(
                                directionalSegment.hasTrafficSigns()
                                        ? buildWindowTime(directionalSegment.getTrafficSigns().getFirst())
                                        : null)
                        .accessible(directionalSegment.isAccessible())
                        .build())
                .build();
    }

    private String buildWindowTime(TrafficSign trafficSign) {
        return trafficSign.findFirstWindowTimeTextSign()
                .map(TextSign::getText)
                .orElse(null);
    }

    @Override
    public OutputFormat getOutputFormat() {
        return OutputFormat.GEO_JSON_INACCESSIBLE;
    }
}
