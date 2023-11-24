package nu.ndw.nls.accessibilitymap.backend.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.PointJson;
import nu.ndw.nls.accessibilitymap.backend.services.TestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MunicipalityMapperTest {

    private MunicipalityMapper municipalityMapper;

    @BeforeEach
    void setup() {
        municipalityMapper = new MunicipalityMapper();
    }

    @Test
    void mapToMunicipalitiesToGeoJSON_ok() {
        var geoJSON = municipalityMapper.mapToMunicipalitiesToGeoJSON(List.of(TestHelper.MUNICIPALITY));
        assertThat(geoJSON.getType().getValue()).isEqualTo("FeatureCollection");
        assertThat(geoJSON.getFeatures()).hasSize(1);
        assertThat(geoJSON.getFeatures().get(0).getId()).isEqualTo(TestHelper.MUNICIPALITY_ID);
        var geometry = geoJSON.getFeatures().get(0).getGeometry();
        assertThat(geometry.getType().getValue()).isEqualTo("Point");
        assertThat(geometry).isInstanceOf(PointJson.class);
        var coordinates = ((PointJson) geometry).getCoordinates();
        assertThat(coordinates.get(0)).isEqualTo(TestHelper.MUNICIPALITY.getStartPoint().getCoordinates()[0].x);
        assertThat(coordinates.get(1)).isEqualTo(TestHelper.MUNICIPALITY.getStartPoint().getCoordinates()[0].y);
        var properties = geoJSON.getFeatures().get(0).getProperties();
        assertThat(properties).isNotNull();
    }
}
