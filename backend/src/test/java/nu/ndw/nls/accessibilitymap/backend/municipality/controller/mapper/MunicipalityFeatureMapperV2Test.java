package nu.ndw.nls.accessibilitymap.backend.municipality.controller.mapper;

import static nu.ndw.nls.accessibilitymap.backend.generated.model.v2.MunicipalityFeature.TypeEnum.FEATURE;
import static nu.ndw.nls.accessibilitymap.backend.generated.model.v2.MunicipalityFeatureCollection.TypeEnum.FEATURE_COLLECTION;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v2.Geometry.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v2.MunicipalityFeature;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v2.MunicipalityFeatureCollection;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v2.MunicipalityProperties;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v2.Point;
import nu.ndw.nls.accessibilitymap.backend.municipality.repository.dto.Municipality;
import nu.ndw.nls.accessibilitymap.backend.municipality.repository.dto.MunicipalityBoundingBox;
import nu.ndw.nls.geometry.rounding.mappers.RoundDoubleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MunicipalityFeatureMapperV2Test {

    private static final String MUNICIPALITY_ID = "GM0307";

    private static final LocalDate DATE_LAST_CHECK = LocalDate.of(2024, 7, 11);

    private MunicipalityFeatureMapperV2 municipalityFeatureMapper;

    private Municipality municipality;

    @BeforeEach
    void setup() {
        municipalityFeatureMapper = new MunicipalityFeatureMapperV2(new RoundDoubleMapper());

        municipality = new Municipality(
                52.0d, 5d,
                50000,
                MUNICIPALITY_ID,
                "GM0307",
                new MunicipalityBoundingBox(1.0, 1.1, 2.1, 2.2),
                LocalDate.parse("2024-07-11"));
    }

    @Test
    void mapToMunicipalitiesToGeoJson() {

        var expectedResult = createExpectedResult(DATE_LAST_CHECK);

        var geoJSON = municipalityFeatureMapper.mapToMunicipalitiesToGeoJson(
                List.of(municipality));

        assertEquals(expectedResult, geoJSON);
    }

    @Test
    void mapToMunicipalitiesToGeoJson_emptyDateLastCheck() {
        Municipality municipalityWithEmptyDate = municipality.withDateLastCheck(null);
        MunicipalityFeatureCollection expectedResult = createExpectedResult(null);

        MunicipalityFeatureCollection geoJSON = municipalityFeatureMapper.mapToMunicipalitiesToGeoJson(
                List.of(municipalityWithEmptyDate));

        assertEquals(expectedResult, geoJSON);
    }

    private MunicipalityFeatureCollection createExpectedResult(LocalDate dateLastCheck) {
        String municipalityId = municipality.municipalityId();
        Point point = Point.builder()
                .type(TypeEnum.POINT)
                .coordinates(List.of(municipality.startCoordinateLongitude(), municipality.startCoordinateLatitude()))
                .build();

        MunicipalityBoundingBox bounds = municipality.bounds();
        List<Double> boundsStart = List.of(bounds.longitudeFrom(), bounds.latitudeFrom());
        List<Double> boundsEnd = List.of(bounds.longitudeTo(), bounds.latitudeTo());

        MunicipalityProperties propertiesJson = new MunicipalityProperties(
                municipality.name(),
                municipality.searchDistanceInMetres(),
                List.of(boundsStart, boundsEnd),
                dateLastCheck);

        MunicipalityFeature featureJson = new MunicipalityFeature(
                FEATURE,
                municipalityId,
                point,
                propertiesJson);
        return new MunicipalityFeatureCollection(FEATURE_COLLECTION, List.of(featureJson));
    }
}
