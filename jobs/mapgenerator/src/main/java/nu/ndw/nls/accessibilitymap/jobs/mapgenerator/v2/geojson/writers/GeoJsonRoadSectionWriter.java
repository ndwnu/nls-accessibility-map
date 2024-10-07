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
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.geojson.model.RoadSectionProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.geojson.model.TrafficSignProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.RoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSign;
import nu.ndw.nls.geometry.distance.FractionAndDistanceCalculator;
import nu.ndw.nls.geometry.geojson.mappers.GeoJsonLineStringCoordinateMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GeoJsonRoadSectionWriter implements OutputWriter {

    private final FileService uploadService;

    private final GenerateConfiguration generateConfiguration;

    private final GeoJsonLineStringCoordinateMapper geoJsonLineStringCoordinateMapper;

    private final FractionAndDistanceCalculator fractionAndDistanceCalculator;

    private static final double TRAFFIC_SIGN_LINE_STRING_DISTANCE_IN_METERS = 1;

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

                    features.add(buildRoadSection(geoJsonIdSequenceSupplier, directionalSegment));
                    if (directionalSegment.hasTrafficSign()) {
                        features.add(buildTrafficSign(
                                geoJsonIdSequenceSupplier,
                                directionalSegment.getTrafficSign(),
                                directionalSegment));
                        features.add(addTrafficSignAsPoint(
                                geoJsonIdSequenceSupplier,
                                directionalSegment.getTrafficSign(),
                                directionalSegment));
                    }

                    return features;
                })
                .flatMap(Collection::stream)
                .toList();
    }

    private Feature addTrafficSignAsPoint(
            GeoJsonIdSequenceSupplier geoJsonIdSequenceSupplier,
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
                .properties(buildTrafficSignProperties(trafficSign))
                .build();
    }

    private Feature buildTrafficSign(
            GeoJsonIdSequenceSupplier geoJsonIdSequenceSupplier,
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
                .properties(buildTrafficSignProperties(trafficSign))
                .build();
    }

    private TrafficSignProperties buildTrafficSignProperties(TrafficSign trafficSign) {
        return TrafficSignProperties
                .builder()
                .nwbRoadSectionId(trafficSign.roadSectionId())
                .direction(trafficSign.direction())
                .trafficSignType(trafficSign.trafficSignType())
                .windowTimes(trafficSign.findFirstTimeWindowedSign()
                        .map(TextSign::getText)
                        .orElse(null))
                .iconUrl(trafficSign.iconUri())
                .iconUrl(trafficSign.iconUri())
                .build();
    }

    private Feature buildRoadSection(
            GeoJsonIdSequenceSupplier geoJsonIdSequenceSupplier,
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
}
