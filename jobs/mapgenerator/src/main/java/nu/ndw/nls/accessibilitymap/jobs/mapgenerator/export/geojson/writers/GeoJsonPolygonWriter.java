package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.writers;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.accessibility.utils.LongSequenceSupplier;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.dto.ExportProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.ExportType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.dto.Feature;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.dto.FeatureCollection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.utils.polygon.MultiPolygonFactory;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GeoJsonPolygonWriter extends AbstractGeoJsonWriter {

    private final MultiPolygonFactory multiPolygonFactory;

    private final FeatureBuilder featureBuilder;

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
    public boolean isEnabled(Set<ExportType> exportTypes) {
        return exportTypes.contains(ExportType.POLYGON_GEO_JSON);
    }

    @Override
    protected FeatureCollection prepareGeoJsonFeatureCollection(
            Accessibility accessibility,
            ExportProperties exportProperties,
            LongSequenceSupplier idSequenceSupplier) {

        List<RoadSectionFragment> roadSectionFragments = accessibility.combinedAccessibility().stream()
                .flatMap(roadSection -> roadSection.getRoadSectionFragments().stream())
                .toList();

        MultiPolygon multiPolygon = multiPolygonFactory.createMultiPolygon(
                roadSectionFragments,
                exportProperties.polygonMaxDistanceBetweenPoints());

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
                            .map(directionalSegment ->
                                    directionalSegment.getRoadSectionFragment().getRoadSection().getId())
                            .collect(Collectors.toSet());

                    List<TrafficSign> relevantTrafficSigns = relevantDirectionalSegment.stream()
                            .filter(DirectionalSegment::hasTrafficSigns)
                            .flatMap(directionalSegment -> directionalSegment.getTrafficSigns().stream())
                            .toList();

                    return featureBuilder.createPolygon(
                            geometry,
                            idSequenceSupplier,
                            relevantTrafficSigns,
                            relevantRoadSectionIds);
                })
                .toList();
    }

    @Override
    protected String buildExportFileName(ExportProperties exportProperties) {

        return super.buildExportFileName(exportProperties).concat("-polygon");
    }

}
