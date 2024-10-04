package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.geojson.writers;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.commands.model.CmdGenerateGeoJsonType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.services.FileService;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.suppliers.GeoJsonIdSequenceSupplier;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.accessibility.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.command.dto.GeoGenerationProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.configuration.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.geojson.model.Feature;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.geojson.model.FeatureCollection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.geojson.model.LineStringGeometry;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.geojson.model.PointGeometry;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.geojson.model.Properties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.DirectionalSegment;
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
    public void writeToFile(
            Accessibility accessibility,
            GeoGenerationProperties mapGenerationProperties) {

        CmdGenerateGeoJsonType cmdGenerateGeoJsonType = CmdGenerateGeoJsonType.valueOf(
                mapGenerationProperties.getTrafficSignType().name()
        );

        Path tempFile = uploadService.createTmpGeoJsonFile(cmdGenerateGeoJsonType);

        GeoJsonIdSequenceSupplier geoJsonIdSequenceSupplier = new GeoJsonIdSequenceSupplier();

        FeatureCollection geoJson = FeatureCollection
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

        uploadService.uploadFile(mapGenerationProperties.getTrafficSignType(), tempFile,
                LocalDateTime.now().toLocalDate());
    }

    private List<Feature> createFeatures(
            RoadSection roadSection,
            GeoJsonIdSequenceSupplier geoJsonIdSequenceSupplier) {

        return roadSection.getRoadSectionFragments().stream()
                .flatMap(roadSectionFragment -> roadSectionFragment.getSegments().stream())
                .filter(Objects::nonNull)
                .map(directionalSegment -> {
                    List<Feature> features = new ArrayList<>();

                    features.add(buildLineString(geoJsonIdSequenceSupplier, directionalSegment));
                    if (directionalSegment.hasTrafficSign()) {
                        features.add(buildPoint(geoJsonIdSequenceSupplier, directionalSegment.getTrafficSign(),
                                directionalSegment));
                    }

                    return features;
                })
                .flatMap(Collection::stream)
                .toList();
    }

    private Feature buildPoint(
            GeoJsonIdSequenceSupplier geoJsonIdSequenceSupplier,
            TrafficSign trafficSign,
            DirectionalSegment directionalSegment) {

        LineString trafficSignLineString = fractionAndDistanceCalculator.getSubLineString(
                directionalSegment.getLineString(),
                trafficSign.fraction());

        return Feature.builder()
                .id(geoJsonIdSequenceSupplier.next())
                .geometry(PointGeometry
                        .builder()
                        .coordinates(List.of(
                                trafficSignLineString.getEndPoint().getX(),
                                trafficSignLineString.getEndPoint().getY()))
                        .build())
                .properties(Properties
                        .builder()
                        .trafficSignType(trafficSign.trafficSignType())
                        .windowTimes(buildWindowTime(trafficSign))
                        .iconUrl(trafficSign.iconUri())
                        .build())
                .build();
    }

    private Feature buildLineString(
            GeoJsonIdSequenceSupplier geoJsonIdSequenceSupplier,
            DirectionalSegment directionalSegment) {

        return Feature.builder()
                .id(geoJsonIdSequenceSupplier.next())
                .geometry(LineStringGeometry
                        .builder()
                        .coordinates(geoJsonLineStringCoordinateMapper.map(directionalSegment.getLineString()))
                        .build())
                .properties(Properties
                        .builder()
                        .id(directionalSegment.getRoadSectionFragment().getRoadSection().getId())
                        .direction(directionalSegment.getDirection())
                        .accessible(directionalSegment.isAccessible())
                        .build())
                .build();
    }

    private String buildWindowTime(TrafficSign trafficSign) {
        return trafficSign.findFirstTimeWindowedSign()
                .map(TextSign::getText)
                .orElse(null);
    }

    @Override
    public OutputFormat getOutputFormat() {
        return OutputFormat.GEO_JSON_INACCESSIBLE;
    }
}
