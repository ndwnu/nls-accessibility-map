package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.utils.polygon;

import com.graphhopper.isochrone.algorithm.ReadableQuadEdge;
import com.graphhopper.isochrone.algorithm.ReadableTriangulation;
import java.util.ArrayList;
import java.util.Collection;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.triangulate.ConformingDelaunayTriangulator;
import org.locationtech.jts.triangulate.ConstraintVertex;
import org.locationtech.jts.triangulate.quadedge.QuadEdgeSubdivision;
import org.locationtech.jts.triangulate.quadedge.Vertex;
import org.springframework.stereotype.Component;

/**
 * Copied class from com.graphhopper.isochrone.algorithm.JTSTriangulator but without the shortest path algorithm
 */
@Component
public class Triangulator {

    public Collection<ReadableQuadEdge> triangulate(final Collection<Coordinate> sites, final double tolerance) {

        final var constraintVertices = sites.stream().map(ConstraintVertex::new).toList();
        final var conformingDelaunayTriangulator = new ConformingDelaunayTriangulator(constraintVertices, tolerance);
        conformingDelaunayTriangulator.setConstraints(new ArrayList<>(), new ArrayList<>());
        conformingDelaunayTriangulator.formInitialDelaunay();
        conformingDelaunayTriangulator.enforceConstraints();

        final var convexHull = conformingDelaunayTriangulator.getConvexHull();

        if (!(convexHull instanceof Polygon)) {
            throw new IllegalArgumentException("Too few points found. "
                    + "Please try a different 'point' or a larger 'time_limit'.");
        }

        QuadEdgeSubdivision tin = conformingDelaunayTriangulator.getSubdivision();
        for (final Vertex vertex : (Collection<Vertex>) tin.getVertices(true)) {
            if (tin.isFrameVertex(vertex)) {
                vertex.setZ(Double.MAX_VALUE);
            }
        }
        ReadableTriangulation triangulation = ReadableTriangulation.wrap(tin);
        return triangulation.getEdges();
    }
}
