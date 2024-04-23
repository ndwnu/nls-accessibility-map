package nu.ndw.nls.accessibilitymap.backend.mappers;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.FeatureCollectionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.FeatureCollectionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.FeatureJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.GeometryJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.MunicipalityPropertiesJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.PointJson;
import nu.ndw.nls.accessibilitymap.backend.model.Municipality;
import nu.ndw.nls.accessibilitymap.backend.model.MunicipalityBoundingBox;
import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

class MunicipalityFeatureMapperTest {

    private static final String MUNICIPALITY_ID = "GM0307";
    private static final Municipality MUNICIPALITY;

    static {
        try {
            MUNICIPALITY = new Municipality(new GeometryFactoryWgs84().createPoint(
                    new Coordinate(5.0, 52.0)),
                    50000,
                    MUNICIPALITY_ID,
                    307,
                    "Test",
                    new URL("http://iets-met-vergunningen.nl"),
                    new MunicipalityBoundingBox(1.0, 1.1, 2.1, 2.2));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private MunicipalityFeatureMapper municipalityFeatureMapper;

    @BeforeEach
    void setup() {
        municipalityFeatureMapper = new MunicipalityFeatureMapper();
    }

    @Test
    void mapToMunicipalitiesToGeoJSON_ok() {

        var expectedResult = createExpectedResult();

        var geoJSON = municipalityFeatureMapper.mapToMunicipalitiesToGeoJSON(List.of(MUNICIPALITY));

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
                MUNICIPALITY.getRequestExemptionUrl().toString());
        var featureJson = new FeatureJson(FeatureJson.TypeEnum.FEATURE, municipalityId, pointJson);
        featureJson.properties(propertiesJson);
        return new FeatureCollectionJson(TypeEnum.FEATURECOLLECTION, List.of(featureJson));
    }
}
