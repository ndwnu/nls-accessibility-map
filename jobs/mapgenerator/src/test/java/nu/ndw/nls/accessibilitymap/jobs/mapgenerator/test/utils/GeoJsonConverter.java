package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.test.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.dto.Feature;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.dto.FeatureCollection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.writers.FeatureBuilder;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.writers.GeoJsonObjectMapperFactory;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;

public class GeoJsonConverter {

    public static String createPolygon(
            MultiPolygon multiPolygon,
            List<RoadSectionFragment> roadSectionFragments) throws JsonProcessingException {

        ObjectMapper geoJsonObjectMapper = new GeoJsonObjectMapperFactory().create(
                GenerateConfiguration.builder()
                        .prettyPrintJson(true)
                        .build());

        return geoJsonObjectMapper.writeValueAsString(
                FeatureCollection.builder()
                        .features(createFeatures(multiPolygon, roadSectionFragments))
                        .build());
    }

    private static List<Feature> createFeatures(
            MultiPolygon multiPolygon,
            List<RoadSectionFragment> allRelevantRoadSectionFragments) {

        AtomicLong idSequenceSupplier = new AtomicLong(1);
        FeatureBuilder featureBuilder = new FeatureBuilder(null, null);

        return IntStream.range(0, multiPolygon.getNumGeometries())
                .mapToObj(i -> {
                    Geometry geometry = multiPolygon.getGeometryN(i);

                    Set<Restriction> relevantRestrictions = allRelevantRoadSectionFragments.stream()
                            .flatMap(roadSectionFragment -> roadSectionFragment.getSegments().stream())
                            .filter(directionalSegment -> geometry.intersects(directionalSegment.getLineString()))
                            .filter(DirectionalSegment::hasRestrictions)
                            .flatMap(directionalSegment -> directionalSegment.getRestrictions().stream())
                            .collect(Collectors.toSet());

                    return featureBuilder.createPolygon(geometry, idSequenceSupplier, relevantRestrictions, Set.of());
                })
                .toList();
    }
}
