package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Map;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.properties.GeoJsonProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.configuration.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.configuration.GenerateProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.trafficsign.TrafficSignType;
import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith( MockitoExtension.class )
class GenerateConfigurationTest {

    private static final double LONGITUDE = 1D;
    private static final double LATITUDE = 2D;
    @Mock
    private GenerateProperties generateProperties;
    @Mock
    private GeometryFactoryWgs84 geometryFactoryWgs84;
    @InjectMocks
    private GenerateConfiguration generateConfiguration;

    @Mock
    private Map<TrafficSignType, GeoJsonProperties> typeToGeoJsonProperties;

    @Mock
    private GeoJsonProperties geoJsonProperties;

    @Mock
    private Point point;

    @Test
    void getConfiguration_ok() {
        when(generateProperties.getGeoJsonProperties()).thenReturn(typeToGeoJsonProperties);
        when(typeToGeoJsonProperties.get(TrafficSignType.C6)).thenReturn(geoJsonProperties);
        assertEquals(geoJsonProperties, generateConfiguration.getConfiguration(TrafficSignType.C6));
    }

    @Test
    void getStartLocation_ok() {
        when(generateProperties.getStartLocationLongitude()).thenReturn(LONGITUDE);
        when(generateProperties.getStartLocationLatitude()).thenReturn(LATITUDE);
        when(geometryFactoryWgs84.createPoint(new Coordinate(LONGITUDE, LATITUDE))).thenReturn(point);
        assertEquals(point, generateConfiguration.getStartLocation());
    }
}