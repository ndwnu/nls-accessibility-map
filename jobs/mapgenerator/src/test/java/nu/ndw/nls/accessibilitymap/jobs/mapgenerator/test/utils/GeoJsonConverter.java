package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.test.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSign;
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

                    List<TrafficSign> relevantTrafficSigns = allRelevantRoadSectionFragments.stream()
                            .flatMap(roadSectionFragment -> roadSectionFragment.getSegments().stream())
                            .filter(directionalSegment -> geometry.intersects(directionalSegment.getLineString()))
                            .filter(DirectionalSegment::hasTrafficSigns)
                            .flatMap(directionalSegment -> directionalSegment.getTrafficSigns().stream())
                            .toList();

                    return featureBuilder.createPolygon(geometry, idSequenceSupplier, relevantTrafficSigns, Set.of());
                })
                .toList();
    }
}
