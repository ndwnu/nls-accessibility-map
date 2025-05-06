package nu.ndw.nls.accessibilitymap.backend.municipality.mappers;

import static nu.ndw.nls.accessibilitymap.backend.generated.model.v1.MunicipalityFeatureCollectionJson.TypeEnum.FEATURE_COLLECTION;
import static nu.ndw.nls.accessibilitymap.backend.generated.model.v1.MunicipalityFeatureJson.TypeEnum.FEATURE;
import static nu.ndw.nls.geojson.geometry.model.GeometryJson.TypeEnum.POINT;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.MunicipalityFeatureCollectionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.MunicipalityFeatureJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.MunicipalityPropertiesJson;
import nu.ndw.nls.accessibilitymap.backend.municipality.controllers.mappers.MunicipalityFeatureMapper;
import nu.ndw.nls.accessibilitymap.backend.municipality.repository.dto.Municipality;
import nu.ndw.nls.accessibilitymap.backend.municipality.repository.dto.MunicipalityBoundingBox;
import nu.ndw.nls.geojson.geometry.model.PointJson;
import nu.ndw.nls.geometry.rounding.mappers.RoundDoubleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MunicipalityFeatureMapperTest {

    private static final String MUNICIPALITY_ID = "GM0307";

    private static final LocalDate DATE_LAST_CHECK = LocalDate.of(2024, 7, 11);

    private MunicipalityFeatureMapper municipalityFeatureMapper;


    private Municipality municipality;

    @BeforeEach
    void setup() {
        municipalityFeatureMapper = new MunicipalityFeatureMapper(new RoundDoubleMapper());

        try {
            municipality = new Municipality(52.0d, 5d,
                    50000,
                    MUNICIPALITY_ID,
                    "GM0307",
                    URI.create("http://iets-met-vergunningen.nl").toURL(),
                    new MunicipalityBoundingBox(1.0, 1.1, 2.1, 2.2),
                    LocalDate.parse("2024-07-11"));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void mapToMunicipalitiesToGeoJson() {

        MunicipalityFeatureCollectionJson expectedResult = createExpectedResult(DATE_LAST_CHECK);

        MunicipalityFeatureCollectionJson geoJSON = municipalityFeatureMapper.mapToMunicipalitiesToGeoJson(
                List.of(municipality));

        assertEquals(expectedResult, geoJSON);
    }

    @Test
    void mapToMunicipalitiesToGeoJson_emptyDateLastCheck() {
        Municipality municipalityWithEmptyDate = municipality.withDateLastCheck(null);
        MunicipalityFeatureCollectionJson expectedResult = createExpectedResult(null);

        MunicipalityFeatureCollectionJson geoJSON = municipalityFeatureMapper.mapToMunicipalitiesToGeoJson(
                List.of(municipalityWithEmptyDate));

        assertEquals(expectedResult, geoJSON);
    }

    private MunicipalityFeatureCollectionJson createExpectedResult(LocalDate dateLastCheck) {
        String municipalityId = municipality.municipalityId();
        var pointJson = new PointJson(List.of(municipality.startCoordinateLongitude(), municipality.startCoordinateLatitude()), POINT);

        MunicipalityBoundingBox bounds = municipality.bounds();
        List<Double> boundsStart = List.of(bounds.longitudeFrom(), bounds.latitudeFrom());
        List<Double> boundsEnd = List.of(bounds.longitudeTo(), bounds.latitudeTo());

        var propertiesJson = new MunicipalityPropertiesJson(
                municipality.name(),
                (int) municipality.searchDistanceInMetres(),
                List.of(boundsStart, boundsEnd),
                municipality.requestExemptionUrl().toString(),
                dateLastCheck);

        var featureJson = new MunicipalityFeatureJson(
                FEATURE,
                municipalityId,
                pointJson,
                propertiesJson);
        return new MunicipalityFeatureCollectionJson(FEATURE_COLLECTION, List.of(featureJson));
    }
}
