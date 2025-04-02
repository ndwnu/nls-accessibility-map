package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.utils.polygon;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.utils.polygon.DirectionalSegmentAccessibleComparator;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.utils.polygon.MultiPolygonFactory;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.utils.polygon.Triangulator;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.test.utils.GeoJsonConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;

class MultiPolygonFactoryTest {

    private MultiPolygonFactory multiPolygonFactory;

    @BeforeEach
    void setUp() {

        multiPolygonFactory = new MultiPolygonFactory(new Triangulator(), new DirectionalSegmentAccessibleComparator());
    }

    @Test
    void createMultiPolygon() throws Exception {

        RoadSectionFragment roadSectionFragmentWithInaccessibleSegments = RoadSectionFragment.builder().build();
        roadSectionFragmentWithInaccessibleSegments.setForwardSegment(
                buildDirectionSegment(roadSectionFragmentWithInaccessibleSegments, false, 1, 1, 1, 1));

        RoadSectionFragment roadSectionFragmentWithAccessibleSegmentsOuter = RoadSectionFragment.builder().build();
        roadSectionFragmentWithAccessibleSegmentsOuter.setForwardSegment(
                buildDirectionSegment(roadSectionFragmentWithAccessibleSegmentsOuter, true, 0, 0, 3, 3));

        RoadSectionFragment roadSectionFragmentWithAccessibleSegmentsInner = RoadSectionFragment.builder().build();
        roadSectionFragmentWithAccessibleSegmentsInner.setForwardSegment(
                buildDirectionSegment(roadSectionFragmentWithAccessibleSegmentsInner, true, 1.25, 1.25, 0.5, 0.5));

        List<RoadSectionFragment> roadSectionFragments = List.of(
                roadSectionFragmentWithInaccessibleSegments,
                roadSectionFragmentWithAccessibleSegmentsInner,
                roadSectionFragmentWithAccessibleSegmentsOuter);

        MultiPolygon multiPolygon = multiPolygonFactory.createMultiPolygon(roadSectionFragments, 0.5);
        assertThat(multiPolygon.toString()).hasToString(
                "MULTIPOLYGON (("
                        + "(1.75 0.5, 2 0.5, 2.25 0.5, 2.5 0.75, 2.5 1, 2.5 1.25, 2.5 1.5, 2.5 1.75, 2.5 2, 2.5 2.25, "
                        + "2.25 2.5, 2 2.5, 1.75 2.5, 1.5 2.5, 1.25 2.5, 1 2.5, 0.75 2.5, 0.5 2.25, 0.5 2, 0.5 1.75, "
                        + "0.5 1.5, 0.5 1.25, 0.5 1, 0.5 0.75, 0.75 0.5, 1 0.5, 1.25 0.5, 1.5 0.5, 1.75 0.5), "
                        + "(1.125 1.375, 1.125 1.625, 1.125 1.875, 1.375 1.875, 1.625 1.875, 1.875 1.875, 1.875 1.625, "
                        + "1.875 1.375, 1.875 1.125, 1.625 1.125, 1.375 1.125, 1.125 1.125, 1.125 1.375)"
                        + "))");

        // This check is just here so we can easily load it into QGIS for visualisation. This check should always pass
        // if the multiPolygon assert is correct as it is just a different representation of the same data.
        String geoJson = GeoJsonConverter.createPolygon(multiPolygon, roadSectionFragments);
        assertThatJson(geoJson)
                .isEqualTo("""
                        {
                          "features" : [ {
                            "id" : 1,
                            "geometry" : {
                              "coordinates" : [ [
                              [ 1.75, 0.5 ], [ 2.0, 0.5 ], [ 2.25, 0.5 ], [ 2.5, 0.75 ], [ 2.5, 1.0 ], [ 2.5, 1.25 ],
                              [ 2.5, 1.5 ], [ 2.5, 1.75 ], [ 2.5, 2.0 ], [ 2.5, 2.25 ], [ 2.25, 2.5 ], [ 2.0, 2.5 ],
                              [ 1.75, 2.5 ], [ 1.5, 2.5 ], [ 1.25, 2.5 ], [ 1.0, 2.5 ], [ 0.75, 2.5 ], [ 0.5, 2.25 ],
                              [ 0.5, 2.0 ], [ 0.5, 1.75 ], [ 0.5, 1.5 ], [ 0.5, 1.25 ], [ 0.5, 1.0 ], [ 0.5, 0.75 ],
                              [ 0.75, 0.5 ], [ 1.0, 0.5 ], [ 1.25, 0.5 ], [ 1.5, 0.5 ], [ 1.75, 0.5 ], [ 1.125, 1.375 ],
                              [ 1.125, 1.625 ], [ 1.125, 1.875 ], [ 1.375, 1.875 ], [ 1.625, 1.875 ], [ 1.875, 1.875 ],
                              [ 1.875, 1.625 ], [ 1.875, 1.375 ], [ 1.875, 1.125 ], [ 1.625, 1.125 ], [ 1.375, 1.125 ],
                              [ 1.125, 1.125 ], [ 1.125, 1.375 ]
                              ] ],
                              "type" : "Polygon"
                            },
                            "properties" : {
                              "roadSectionIds" : [ ],
                              "windowTimes" : [ ]
                            },
                            "type" : "Feature"
                          } ],
                          "type" : "FeatureCollection"
                        }
                        """);
    }

    private DirectionalSegment buildDirectionSegment(
            RoadSectionFragment roadSectionFragment,
            boolean accessible,
            double boxStartX,
            double boxStartY,
            double boxWidth,
            double boxHeight) {

        return DirectionalSegment.builder()
                .accessible(accessible)
                .roadSectionFragment(roadSectionFragment)
                .lineString(new GeometryFactory().createLineString(
                        buildRectangle(boxStartX, boxStartY, boxWidth, boxHeight)))
                .build();
    }

    private Coordinate[] buildRectangle(double startX, double startY, double width, double height) {

        return new Coordinate[]{
                new Coordinate(startX, startY, 0),
                new Coordinate(startX, startY + height, 0),
                new Coordinate(startX + width, startY + height, 0),
                new Coordinate(startX + width, startY, 0),
                new Coordinate(startX, startY, 0),
        };
    }
}
