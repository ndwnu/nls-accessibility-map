package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.test.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.IntStream;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.dto.Feature;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.dto.FeatureCollection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.writers.FeatureBuilder;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.writers.GeoJsonObjectMapperFactory;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.utils.LongSequenceSupplier;
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
                        .features(createFeatures(multiPolygon, new LongSequenceSupplier(), roadSectionFragments))
                        .build());
    }

    private static List<Feature> createFeatures(
            MultiPolygon multiPolygon,
            LongSequenceSupplier idSequenceSupplier,
            List<RoadSectionFragment> allRelevantRoadSectionFragments) {

        FeatureBuilder featureBuilder = new FeatureBuilder(null, null);

        return IntStream.range(0, multiPolygon.getNumGeometries())
                .mapToObj(i -> {
                    Geometry geometry = multiPolygon.getGeometryN(i);

                    List<TrafficSign> relevantTrafficSigns = allRelevantRoadSectionFragments.stream()
                            .flatMap(roadSectionFragment -> roadSectionFragment.getSegments().stream())
                            .filter(directionalSegment -> geometry.intersects(directionalSegment.getLineString()))
                            .filter(DirectionalSegment::hasTrafficSign)
                            .map(DirectionalSegment::getTrafficSign)
                            .toList();

                    return featureBuilder.createPolygon(geometry, idSequenceSupplier, relevantTrafficSigns);
                })
                .toList();
    }
}
