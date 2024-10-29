package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.test.unit.ValidationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GeoGenerationPropertiesTest extends ValidationTest {

    private GeoGenerationProperties geoGenerationProperties;

    @Mock
    private VehicleProperties vehicleProperties;

    @Mock
    private GenerateConfiguration generateConfiguration;

    @BeforeEach
    void setUp() {

        geoGenerationProperties = GeoGenerationProperties.builder()
                .exportVersion(1)
                .nwbVersion(2)
                .publishEvents(true)
                .startTime(OffsetDateTime.now())
                .startLocationLatitude(50)
                .startLocationLongitude(3)
                .trafficSignType(TrafficSignType.C7)
                .vehicleProperties(vehicleProperties)
                .polygonMaxDistanceBetweenPoints(0.000000000000001d)
                .includeOnlyTimeWindowedSigns(true)
                .generateConfiguration(generateConfiguration)
                .build();
    }

    @Test
    void validate_ok() {

        validate(geoGenerationProperties, List.of(), List.of());
    }

    @Test
    void validate_exportVersion_null() {

        geoGenerationProperties = geoGenerationProperties.withExportVersion(null);

        validate(geoGenerationProperties, List.of("exportVersion"), List.of("must not be null"));
    }

    @Test
    void validate_nwbVersion_null() {

        geoGenerationProperties = geoGenerationProperties.withNwbVersion(null);

        validate(geoGenerationProperties, List.of("nwbVersion"), List.of("must not be null"));
    }

    @Test
    void validate_publishEvents_null() {

        geoGenerationProperties = geoGenerationProperties.withPublishEvents(null);

        validate(geoGenerationProperties, List.of("publishEvents"), List.of("must not be null"));
    }

    @Test
    void validate_startTime_null() {

        geoGenerationProperties = geoGenerationProperties.withStartTime(null);

        validate(geoGenerationProperties, List.of("startTime"), List.of("must not be null"));
    }

    @Test
    void validate_trafficSignType_null() {

        geoGenerationProperties = geoGenerationProperties.withTrafficSignType(null);

        validate(geoGenerationProperties, List.of("trafficSignType"), List.of("must not be null"));
    }

    @ParameterizedTest
    @CsvSource(nullValues = "null", textBlock = """
            0, must be greater than 0,
            0.0000000000001, null,
            """)
    void validate_polygonMaxDistanceBetweenPoints_mustBePositive(double polygonMaxDistanceBetweenPoints,
            String expectedError) {

        geoGenerationProperties = geoGenerationProperties.withPolygonMaxDistanceBetweenPoints(
                polygonMaxDistanceBetweenPoints);

        if (Objects.nonNull(expectedError)) {
            validate(geoGenerationProperties, List.of("polygonMaxDistanceBetweenPoints"), List.of(expectedError));
        } else {
            validate(geoGenerationProperties, List.of(), List.of());
        }
    }

    @Test
    void validate_includeOnlyTimeWindowedSigns_null() {

        geoGenerationProperties = geoGenerationProperties.withIncludeOnlyTimeWindowedSigns(null);

        validate(geoGenerationProperties, List.of("includeOnlyTimeWindowedSigns"), List.of("must not be null"));
    }

    @Test
    void validate_generateConfiguration_null() {

        geoGenerationProperties = geoGenerationProperties.withGenerateConfiguration(null);

        validate(geoGenerationProperties, List.of("generateConfiguration"), List.of("must not be null"));
    }

    @Test
    void validate_vehicleProperties_null() {

        geoGenerationProperties = geoGenerationProperties.withVehicleProperties(null);

        validate(geoGenerationProperties, List.of("vehicleProperties"), List.of("must not be null"));
    }

    @ParameterizedTest
    @CsvSource(nullValues = "null", textBlock = """
            49, must be greater than or equal to 50,
            50, null,
            54, null,
            55, must be less than or equal to 54
            """)
    void validate_startLocationLatitude_edgeCases(double startLocationLatitude, String expectedError) {

        geoGenerationProperties = geoGenerationProperties.withStartLocationLatitude(startLocationLatitude);

        if (Objects.nonNull(expectedError)) {
            validate(geoGenerationProperties, List.of("startLocationLatitude"), List.of(expectedError));
        } else {
            validate(geoGenerationProperties, List.of(), List.of());
        }
    }

    @ParameterizedTest
    @CsvSource(nullValues = "null", textBlock = """
            2, must be greater than or equal to 3,
            3, null,
            8, null,
            9, must be less than or equal to 8
            """)
    void validate_startLocationLongitude_edgeCases(double startLocationLongitude, String expectedError) {

        geoGenerationProperties = geoGenerationProperties.withStartLocationLongitude(startLocationLongitude);

        if (Objects.nonNull(expectedError)) {
            validate(geoGenerationProperties, List.of("startLocationLongitude"), List.of(expectedError));
        } else {
            validate(geoGenerationProperties, List.of(), List.of());
        }
    }

    @Override
    protected Class<?> getClassToTest() {
        return geoGenerationProperties.getClass();
    }
}