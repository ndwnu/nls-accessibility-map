package nu.ndw.nls.accessibilitymap.accessibility.nwb.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import nu.ndw.nls.geometry.constants.SRID;
import nu.ndw.nls.geometry.crs.CrsTransformer;
import nu.ndw.nls.geometry.distance.FractionAndDistanceCalculator;
import nu.ndw.nls.geometry.distance.model.CoordinateAndBearing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.LineString;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NwbRoadSectionSnapServiceTest {

    @Mock
    private FractionAndDistanceCalculator fractionAndDistanceCalculator;

    @Mock
    private CrsTransformer crsTransformer;

    private NwbRoadSectionSnapService nwbRoadSectionSnapService;

    @Mock
    private LineString lineStringWgs84;

    @Mock
    private LineString lineStringRdNew;

    @Mock
    private CoordinateAndBearing coordinateAndBearing;

    @BeforeEach
    void setup() {
        nwbRoadSectionSnapService = new NwbRoadSectionSnapService(fractionAndDistanceCalculator, crsTransformer);
    }

    @Test
    void snapToLineForRdGeometry() {
        when(crsTransformer.transformFromRdNewToWgs84(lineStringRdNew)).thenReturn(lineStringWgs84);
        when(fractionAndDistanceCalculator.getCoordinateAndBearing(lineStringWgs84, 0.5)).thenReturn(coordinateAndBearing);

        double fraction = 0.5;

        CoordinateAndBearing result = nwbRoadSectionSnapService.snapToLineForRdGeometry(lineStringRdNew, fraction);

        assertThat(result).isEqualTo(coordinateAndBearing);
        verify(lineStringWgs84, times(1)).setSRID(SRID.WGS84.value);
    }

    @Test
    void snapToLine() {

        when(fractionAndDistanceCalculator.getCoordinateAndBearing(lineStringWgs84, 0.5)).thenReturn(coordinateAndBearing);

        double fraction = 0.5;

        CoordinateAndBearing result = nwbRoadSectionSnapService.snapToLine(lineStringWgs84, fraction);

        assertThat(result).isEqualTo(coordinateAndBearing);
    }
}
