package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.writers;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.dto.GeoGenerationProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.dto.Feature;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.dto.FeatureCollection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.utils.polygon.MultiPolygonFactory;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.utils.LongSequenceSupplier;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GeoJsonPolygonWriter extends AbstractGeoJsonWriter {

    private final MultiPolygonFactory multiPolygonFactory;

    private final FeatureBuilder featureBuilder;

    private static final double MAX_DISTANCE_BETWEEN_POINTS = 0.0005;

    public GeoJsonPolygonWriter(
            FileService fileService,
            GenerateConfiguration generateConfiguration,
            GeoJsonObjectMapperFactory geoJsonObjectMapperFactory,
            MultiPolygonFactory multiPolygonFactory,
            FeatureBuilder featureBuilder) {

        super(generateConfiguration, geoJsonObjectMapperFactory, fileService);

        this.multiPolygonFactory = multiPolygonFactory;
        this.featureBuilder = featureBuilder;
    }

    @Override
    protected FeatureCollection prepareGeoJsonFeatureCollection(
            Accessibility accessibility,
            GeoGenerationProperties geoGenerationProperties,
            LongSequenceSupplier idSequenceSupplier) {

        List<RoadSectionFragment> roadSectionFragments = accessibility.combinedAccessibility().stream()
                .flatMap(roadSection -> roadSection.getRoadSectionFragments().stream())
                .toList();

        MultiPolygon multiPolygon = multiPolygonFactory.createMultiPolygon(roadSectionFragments,
                MAX_DISTANCE_BETWEEN_POINTS);

        return FeatureCollection
                .builder()
                .features(createFeatures(multiPolygon, idSequenceSupplier, roadSectionFragments))
                .build();
    }

    private List<Feature> createFeatures(
            MultiPolygon multiPolygon,
            LongSequenceSupplier idSequenceSupplier,
            List<RoadSectionFragment> roadSectionFragments) {

        log.debug("Building polygon features");
        return IntStream.range(0, multiPolygon.getNumGeometries())
                .mapToObj(i -> {
                    Geometry geometry = multiPolygon.getGeometryN(i);

                    List<DirectionalSegment> relevantDirectionalSegment = roadSectionFragments.stream()
                            .flatMap(roadSectionFragment -> roadSectionFragment.getSegments().stream())
                            .filter(directionalSegment -> geometry.intersects(directionalSegment.getLineString()))
                            .toList();

                    Set<Long> relevantRoadSectionIds = relevantDirectionalSegment.stream()
                            .map(directionalSegment -> directionalSegment.getRoadSectionFragment().getRoadSection().getId())
                            .collect(Collectors.toSet());

                    List<TrafficSign> relevantTrafficSigns = relevantDirectionalSegment.stream()
                            .filter(DirectionalSegment::hasTrafficSign)
                            .map(DirectionalSegment::getTrafficSign)
                            .toList();

                    return featureBuilder.createPolygon(geometry, idSequenceSupplier, relevantTrafficSigns, relevantRoadSectionIds);
                })
                .toList();
    }

    @Override
    protected String buildExportFileName(GeoGenerationProperties geoGenerationProperties) {

        return super.buildExportFileName(geoGenerationProperties).concat("-polygon");
    }

}
