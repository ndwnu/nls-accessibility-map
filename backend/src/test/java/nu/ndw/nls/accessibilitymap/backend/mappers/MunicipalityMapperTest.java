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
import org.jetbrains.annotations.NotNull;
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

        var expectedResult = createExpectedResult();

        var geoJSON = municipalityMapper.mapToMunicipalitiesToGeoJSON(List.of(MUNICIPALITY));

        assertEquals(expectedResult, geoJSON);
    }

    @NotNull
    private static FeatureCollectionJson createExpectedResult() {
        var municipalityId = MUNICIPALITY.getMunicipalityId();
        var startPoint = MUNICIPALITY.getStartPoint();
        var bounds = MUNICIPALITY.getBounds();
        var pointJson = new PointJson(GeometryJson.TypeEnum.POINT);
        pointJson.coordinates(List.of(startPoint.getX(), startPoint.getY()));
        var boundsStart = List.of(bounds.longitudeFrom(), bounds.latitudeFrom());
        var boundsEnd = List.of(bounds.longitudeTo(), bounds.latitudeTo());
        var propertiesJson = new MunicipalityPropertiesJson(
                MUNICIPALITY.getName(),
                (int) MUNICIPALITY.getSearchDistanceInMetres(),
                List.of(boundsStart, boundsEnd),
                MUNICIPALITY.getRequestExemptionUrl());
        var featureJson = new FeatureJson(FeatureJson.TypeEnum.FEATURE, municipalityId, pointJson);
        featureJson.properties(propertiesJson);
        return new FeatureCollectionJson(TypeEnum.FEATURECOLLECTION, List.of(featureJson));
    }
}
