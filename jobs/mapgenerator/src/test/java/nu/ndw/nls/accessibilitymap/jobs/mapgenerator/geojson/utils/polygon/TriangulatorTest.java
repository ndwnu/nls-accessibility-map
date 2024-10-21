package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.utils.polygon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.graphhopper.isochrone.algorithm.ReadableQuadEdge;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TriangulatorTest {

    private Triangulator triangulator;

    @BeforeEach
    void setUp() {

        triangulator = new Triangulator();
    }

    @Test
    void triangulate_ok() {

        List<Coordinate> coordinates = List.of(
                new Coordinate(1, 1, 0),
                new Coordinate(2, 1, 0),
                new Coordinate(2, 2, 0)
        );

        Collection<ReadableQuadEdge> result = triangulator.triangulate(
                coordinates, 0);

        List<Coordinate> coordinates2 = result.stream()
                .map(readableQuadEdge -> readableQuadEdge.dest().getCoordinate())
                .toList();

        assertThat(coordinates2).containsExactly(
                new Coordinate(-13.200000000000001, -13.200000000000001, Double.MAX_VALUE),
                new Coordinate(16.200000000000003, -13.200000000000001, Double.MAX_VALUE),
                new Coordinate(1.5, 16.200000000000003, Double.MAX_VALUE),
                new Coordinate(1, 1, 0),
                new Coordinate(1, 1, 0),
                new Coordinate(-13.200000000000001, -13.200000000000001, Double.MAX_VALUE),
                new Coordinate(2, 2, 0),
                new Coordinate(2, 1, 0),
                new Coordinate(2, 1, 0),
                new Coordinate(2, 2, 0),
                new Coordinate(2, 2, 0),
                new Coordinate(2, 2, 0)
        );
    }

    @Test
    void triangulate_notEnoughPoints() {

        List<Coordinate> coordinates = List.of(
                new Coordinate(1, 1, 0),
                new Coordinate(2, 1, 0)
        );

        assertThat(catchThrowable(() ->triangulator.triangulate(coordinates, 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Too few points found. Please try a different 'point' or a larger 'time_limit'.");
    }
}