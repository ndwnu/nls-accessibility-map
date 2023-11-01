package nu.ndw.nls.accessibilitymap.jobs.nwb.mappers;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.LineString;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.springframework.stereotype.Component;

@Component
public class RijksdriehoekToWgs84Mapper {

    private static final MathTransform RIJKSDRIEHOEK_TO_WGS84;

    static {
        try {
            boolean longitudeFirst = true;
            CoordinateReferenceSystem wgs84Crs = CRS.decode("EPSG:4326", longitudeFirst);
            CoordinateReferenceSystem rijksdriehoekCrs = CRS.decode("EPSG:28992", longitudeFirst);
            RIJKSDRIEHOEK_TO_WGS84 = CRS.findMathTransform(rijksdriehoekCrs, wgs84Crs);
        } catch (FactoryException e) {
            throw new IllegalStateException("Failed to initialize coordinate references systems", e);
        }
    }

    public LineString map(LineString lineString) {
        try {
            return (LineString) JTS.transform(lineString, RIJKSDRIEHOEK_TO_WGS84);
        } catch (TransformException e) {
            throw new IllegalStateException("Failed to transform geometry from rijksdriehoek to wgs84", e);
        }
    }
}
