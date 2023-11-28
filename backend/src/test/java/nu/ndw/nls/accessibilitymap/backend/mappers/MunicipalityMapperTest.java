package nu.ndw.nls.accessibilitymap.backend.mappers;

import static nu.ndw.nls.accessibilitymap.backend.services.TestHelper.MUNICIPALITY;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.FeatureCollectionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.FeatureCollectionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.FeatureJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.GeometryJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.MunicipalityPropertiesJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.PointJson;
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
        var expectedResult = new FeatureCollectionJson(TypeEnum.FEATURECOLLECTION,
                List.of(new FeatureJson(FeatureJson.TypeEnum.FEATURE, MUNICIPALITY.getMunicipalityId(),
                        new PointJson(GeometryJson.TypeEnum.POINT).coordinates(
                                List.of(MUNICIPALITY.getStartPoint().getX(),
                                        MUNICIPALITY.getStartPoint().getY()))).properties(
                        new MunicipalityPropertiesJson(MUNICIPALITY.getName(),
                                (int) MUNICIPALITY.getSearchDistanceInMetres()))));
        var geoJSON = municipalityMapper.mapToMunicipalitiesToGeoJSON(List.of(MUNICIPALITY));
        assertEquals(expectedResult, geoJSON);
    }
}
