package nu.ndw.nls.accessibilitymap.accessibility.nwb.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.geometry.constants.SRID;
import nu.ndw.nls.geometry.crs.CrsTransformer;
import nu.ndw.nls.geometry.distance.FractionAndDistanceCalculator;
import nu.ndw.nls.geometry.distance.model.CoordinateAndBearing;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NwbRoadSectionSnapService {

    private final FractionAndDistanceCalculator fractionAndDistanceCalculator;

    private final CrsTransformer crsTransformer;

    public CoordinateAndBearing snapToLine(LineString geometry, double fraction) {

        LineString lineStringWgs84 = (LineString) crsTransformer.transformFromRdNewToWgs84(geometry);
        lineStringWgs84.setSRID(SRID.WGS84.value);

        return fractionAndDistanceCalculator.getCoordinateAndBearing(
                lineStringWgs84, fraction);
    }
}
