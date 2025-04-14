package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mappers.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import nu.ndw.nls.geojson.geometry.mappers.JtsLineStringJsonMapper;
import nu.ndw.nls.geojson.geometry.model.LineStringJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GeoJsonLineStringMergeMapperTest {

    @Mock
    private JtsLineStringJsonMapper jtsLineStringJsonMapper;

    private GeoJsonLineStringMergeMapper geoJsonLineStringMergeMapper;

    @Mock
    private LineStringJson expectedLineStringJson;

    @BeforeEach
    void setUp() {
        geoJsonLineStringMergeMapper = new GeoJsonLineStringMergeMapper(jtsLineStringJsonMapper);
    }

    @Test
    void mapToLineStringJson_withValidLineStrings() {
        GeometryFactory geometryFactory = new GeometryFactory();
        LineString lineString1 = geometryFactory.createLineString(new Coordinate[]{new Coordinate(0, 0), new Coordinate(1, 1)});
        LineString lineString2 = geometryFactory.createLineString(new Coordinate[]{new Coordinate(1, 1), new Coordinate(2, 2)});
        LineString mergedLineString = geometryFactory.createLineString(
                new Coordinate[]{new Coordinate(0, 0), new Coordinate(1, 1), new Coordinate(2, 2)});
        when(jtsLineStringJsonMapper.map(mergedLineString)).thenReturn(expectedLineStringJson);

        LineStringJson result = geoJsonLineStringMergeMapper.mapToLineStringJson(List.of(lineString1, lineString2));

        assertThat(result).isEqualTo(expectedLineStringJson);
    }

    @Test
    void mapToLineStringJson_withEmptyListThrowsException() {

        assertThatThrownBy(() -> geoJsonLineStringMergeMapper.mapToLineStringJson(List.of()))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot merge empty line strings");

    }

    @Test
    void mapToLineStringJson_withSingleLineString() {
        GeometryFactory geometryFactory = new GeometryFactory();
        LineString lineString = geometryFactory.createLineString(new Coordinate[]{new Coordinate(0, 0), new Coordinate(1, 1)});
        when(jtsLineStringJsonMapper.map(lineString)).thenReturn(expectedLineStringJson);

        LineStringJson result = geoJsonLineStringMergeMapper.mapToLineStringJson(List.of(lineString));

        assertThat(result).isEqualTo(expectedLineStringJson);
    }
}
