package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.utils.polygon;

import com.graphhopper.isochrone.algorithm.ContourBuilder;
import com.graphhopper.isochrone.algorithm.ReadableQuadEdge;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSectionFragment;
import org.locationtech.jts.densify.Densifier;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class MultiPolygonFactory {

    private static final int Z_ACCESSIBLE = 2;

    private static final int Z_THRESHOLD = 1;

    private static final int Z_INACCESSIBLE = 0;

    private static final int TOLERANCE_DEGREES = 0;

    private static final int COORDINATE_PRECISION = 7;

    private final Triangulator triangulator;

    private final DirectionalSegmentAccessibleComparator directionalSegmentAccessibleComparator;

    public MultiPolygon createMultiPolygon(
            List<RoadSectionFragment> roadSectionFragments,
            double maxDistanceBetweenCoordinates) {

        log.debug("Calculating coordinates");
        List<Coordinate> coordinates = createCoordinatesFromLineStrings(
                roadSectionFragments,
                maxDistanceBetweenCoordinates);

        log.debug("Started triangulation");
        Collection<ReadableQuadEdge> result = triangulator.triangulate(coordinates, TOLERANCE_DEGREES);

        log.debug("Started contour building");
        ContourBuilder contourBuilder = new ContourBuilder(null);
        return contourBuilder.computeIsoline(Z_THRESHOLD, result);
    }

    private List<Coordinate> createCoordinatesFromLineStrings(
            List<RoadSectionFragment> roadSectionFragments,
            double maxDistanceBetweenCoordinates) {

        return roadSectionFragments.stream()
                .map(roadSectionFragment -> roadSectionFragment.getSegments().stream().findFirst())
                .filter(Optional::isPresent)
                .map(Optional::get)
                // Accessible segments must come first because of triangulation that would deduplicate coordinates
                .sorted(directionalSegmentAccessibleComparator)
                .flatMap(directionalSegment -> Arrays.stream(
                                densify(
                                        directionalSegment.getLineString(),
                                        maxDistanceBetweenCoordinates)
                                        .getCoordinates())
                        .map(coordinate -> new Coordinate(
                                round(coordinate.x),
                                round(coordinate.y),
                                directionalSegment.getRoadSectionFragment().isAccessibleFromAllSegments() ? Z_ACCESSIBLE
                                        : Z_INACCESSIBLE)))
                .toList();
    }

    private double round(double number) {
        return BigDecimal.valueOf(number).setScale(COORDINATE_PRECISION, RoundingMode.HALF_UP).doubleValue();
    }

    private Geometry densify(Geometry geometry, double maxDistanceBetweenPoints) {

        Densifier densifier = new Densifier(geometry);
        densifier.setDistanceTolerance(maxDistanceBetweenPoints);
        return densifier.getResultGeometry();
    }


}
