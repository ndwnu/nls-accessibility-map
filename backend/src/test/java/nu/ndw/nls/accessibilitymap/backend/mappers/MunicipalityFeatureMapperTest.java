package nu.ndw.nls.accessibilitymap.backend.mappers;

import static nu.ndw.nls.accessibilitymap.backend.generated.model.v1.MunicipalityFeatureCollectionJson.TypeEnum.FEATURE_COLLECTION;
import static nu.ndw.nls.accessibilitymap.backend.generated.model.v1.MunicipalityFeatureJson.TypeEnum.FEATURE;
import static nu.ndw.nls.geojson.geometry.model.GeometryJson.TypeEnum.POINT;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.model.MunicipalityBoundingBox;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.MunicipalityFeatureCollectionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.MunicipalityFeatureJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.MunicipalityPropertiesJson;
import nu.ndw.nls.accessibilitymap.backend.municipality.model.Municipality;
import nu.ndw.nls.geojson.geometry.model.PointJson;
import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
import nu.ndw.nls.geometry.rounding.mappers.RoundDoubleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

class MunicipalityFeatureMapperTest {

    private static final String MUNICIPALITY_ID = "GM0307";

    private static final Municipality MUNICIPALITY;

    private static final LocalDate DATE_LAST_CHECK = LocalDate.of(2024, 7, 11);

    static {
        try {
            MUNICIPALITY = new Municipality(new GeometryFactoryWgs84().createPoint(
                    new Coordinate(5.0, 52.0)),
                    50000,
                    MUNICIPALITY_ID,
                    307,
                    "Test",
                    URI.create("http://iets-met-vergunningen.nl").toURL(),
                    new MunicipalityBoundingBox(1.0, 1.1, 2.1, 2.2),
                    LocalDate.parse("2024-07-11"));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private MunicipalityFeatureMapper municipalityFeatureMapper;

    @BeforeEach
    void setup() {
        municipalityFeatureMapper = new MunicipalityFeatureMapper(new RoundDoubleMapper());
    }

    @Test
    void mapToMunicipalitiesToGeoJson() {
        MunicipalityFeatureCollectionJson expectedResult = createExpectedResult(DATE_LAST_CHECK);

        MunicipalityFeatureCollectionJson geoJSON = municipalityFeatureMapper.mapToMunicipalitiesToGeoJson(
                List.of(MUNICIPALITY));

        assertEquals(expectedResult, geoJSON);
    }

    @Test
    void mapToMunicipalitiesToGeoJson_emptyDateLastCheck() {
        Municipality municipalityWithEmptyDate = createMunicipalityWithDateLastCheckNull();
        MunicipalityFeatureCollectionJson expectedResult = createExpectedResult(null);

        MunicipalityFeatureCollectionJson geoJSON = municipalityFeatureMapper.mapToMunicipalitiesToGeoJson(
                List.of(municipalityWithEmptyDate));

        assertEquals(expectedResult, geoJSON);
    }

    private Municipality createMunicipalityWithDateLastCheckNull() {
        try {
            return new Municipality(new GeometryFactoryWgs84().createPoint(
                    new Coordinate(5.0, 52.0)),
                    50000,
                    MUNICIPALITY_ID,
                    307,
                    "Test",
                    URI.create("http://iets-met-vergunningen.nl").toURL(),
                    new MunicipalityBoundingBox(1.0, 1.1, 2.1, 2.2),
                    null);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private MunicipalityFeatureCollectionJson createExpectedResult(LocalDate dateLastCheck) {
        String municipalityId = MUNICIPALITY.getMunicipalityId();
        Point startPoint = MUNICIPALITY.getStartPoint();
        MunicipalityBoundingBox bounds = MUNICIPALITY.getBounds();
        var pointJson = new PointJson(List.of(startPoint.getX(), startPoint.getY()), POINT);
        List<Double> boundsStart = List.of(bounds.longitudeFrom(), bounds.latitudeFrom());
        List<Double> boundsEnd = List.of(bounds.longitudeTo(), bounds.latitudeTo());
        var propertiesJson = new MunicipalityPropertiesJson(
                MUNICIPALITY.getName(),
                (int) MUNICIPALITY.getSearchDistanceInMetres(),
                List.of(boundsStart, boundsEnd),
                MUNICIPALITY.getRequestExemptionUrl().toString(),
                dateLastCheck);
        var featureJson = new MunicipalityFeatureJson(
                FEATURE,
                municipalityId,
                pointJson,
                propertiesJson);
        return new MunicipalityFeatureCollectionJson(FEATURE_COLLECTION, List.of(featureJson));
    }
}
