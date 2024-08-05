package nu.ndw.nls.accessibilitymap.backend.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.GeometryJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.MunicipalityFeatureCollectionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.MunicipalityFeatureCollectionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.MunicipalityFeatureJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.MunicipalityPropertiesJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.PointJson;
import nu.ndw.nls.accessibilitymap.accessibility.model.Municipality;
import nu.ndw.nls.accessibilitymap.accessibility.model.MunicipalityBoundingBox;
import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

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
    void mapToMunicipalitiesToGeoJson_ok() {
        MunicipalityFeatureCollectionJson expectedResult = createExpectedResult();

        MunicipalityFeatureCollectionJson geoJSON = municipalityFeatureMapper.mapToMunicipalitiesToGeoJson(
                List.of(MUNICIPALITY));

        assertEquals(expectedResult, geoJSON);
    }

    private MunicipalityFeatureCollectionJson createExpectedResult() {
        String municipalityId = MUNICIPALITY.getMunicipalityId();
        Point startPoint = MUNICIPALITY.getStartPoint();
        MunicipalityBoundingBox bounds = MUNICIPALITY.getBounds();
        var pointJson = new PointJson(List.of(startPoint.getX(), startPoint.getY()), GeometryJson.TypeEnum.POINT);
        List<Double> boundsStart = List.of(bounds.longitudeFrom(), bounds.latitudeFrom());
        List<Double> boundsEnd = List.of(bounds.longitudeTo(), bounds.latitudeTo());
        var propertiesJson = new MunicipalityPropertiesJson(
                MUNICIPALITY.getName(),
                (int) MUNICIPALITY.getSearchDistanceInMetres(),
                List.of(boundsStart, boundsEnd),
                MUNICIPALITY.getRequestExemptionUrl().toString());
        var featureJson = new MunicipalityFeatureJson(MunicipalityFeatureJson.TypeEnum.FEATURE, municipalityId,
                pointJson);
        featureJson.properties(propertiesJson);
        return new MunicipalityFeatureCollectionJson(TypeEnum.FEATURE_COLLECTION, List.of(featureJson));
    }
}
