package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.utils.polygon;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.RoadSectionFragment;
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
                buildDirectionSegment(roadSectionFragmentWithInaccessibleSegments, false, 1, 1, 1));

        RoadSectionFragment roadSectionFragmentWithAccessibleSegmentsOuter = RoadSectionFragment.builder().build();
        roadSectionFragmentWithAccessibleSegmentsOuter.setForwardSegment(
                buildDirectionSegment(roadSectionFragmentWithAccessibleSegmentsOuter, true, 0, 0, 3));

        RoadSectionFragment roadSectionFragmentWithAccessibleSegmentsInner = RoadSectionFragment.builder().build();
        roadSectionFragmentWithAccessibleSegmentsInner.setForwardSegment(
                buildDirectionSegment(roadSectionFragmentWithAccessibleSegmentsInner, true, 1.25, 1.25, 0.5));

        final var roadSectionFragments = List.of(
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
            double squareStartX,
            double squareStartY,
            double squareSize) {

        return DirectionalSegment.builder()
                .accessible(accessible)
                .roadSectionFragment(roadSectionFragment)
                .lineString(new GeometryFactory().createLineString(
                        buildSquare(squareStartX, squareStartY, squareSize)))
                .build();
    }

    private Coordinate[] buildSquare(double squareStartX, double squareStartY, double squareSize) {

        return new Coordinate[]{
                new Coordinate(squareStartX, squareStartY, 0),
                new Coordinate(squareStartX, squareStartY + squareSize, 0),
                new Coordinate(squareStartX + squareSize, squareStartY + squareSize, 0),
                new Coordinate(squareStartX + squareSize, squareStartY, 0),
                new Coordinate(squareStartX, squareStartY, 0),
        };
    }
}