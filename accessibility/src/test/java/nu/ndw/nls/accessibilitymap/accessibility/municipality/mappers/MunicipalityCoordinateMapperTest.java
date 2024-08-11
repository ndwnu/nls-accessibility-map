package nu.ndw.nls.accessibilitymap.accessibility.municipality.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;
import lombok.SneakyThrows;
import nu.ndw.nls.accessibilitymap.accessibility.model.MunicipalityBoundingBox;
import nu.ndw.nls.accessibilitymap.accessibility.municipality.MunicipalityProperty;
import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MunicipalityCoordinateMapperTest {

    private static final double START_COORDINATE_LONGITUDE = 1D;
    private static final double START_COORDINATE_LATITUDE = 2D;
    private static final String NAME = "name";
    private static final String MUNICIPALITY_ID_STRING = "123";

    private static final URL URL;

    static {
        try {
            URL = new URL("http://example.com");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Mock
    private GeometryFactoryWgs84 geometryFactoryWgs84;

    @InjectMocks
    private MunicipalityCoordinateMapper municipalityCoordinateMapper;

    @Mock
    private MunicipalityBoundingBox municipalityBoundingBox;

    @Mock
    private Point point;

    @Test
    @SneakyThrows
    void map_ok() {
        when(geometryFactoryWgs84.createPoint(new Coordinate(START_COORDINATE_LONGITUDE, START_COORDINATE_LATITUDE)))
                .thenReturn(point);

        assertEquals(point, municipalityCoordinateMapper.map(new MunicipalityProperty(NAME, START_COORDINATE_LONGITUDE,
                START_COORDINATE_LATITUDE, 3D, MUNICIPALITY_ID_STRING, URL, municipalityBoundingBox)));


    }
}