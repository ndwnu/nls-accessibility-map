package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.constraints.Null;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import nu.ndw.nls.springboot.test.util.validation.ValidationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GenerateConfigurationTest extends ValidationTest {

    private GenerateConfiguration generateConfiguration;

    @BeforeEach
    void setUp() {

        generateConfiguration = GenerateConfiguration.builder()
                .zone(ZoneId.of("Europe/Amsterdam"))
                .rootExportDirectory(Path.of("tmp/tmp/"))
                .relativeExportDirectoryPattern("'v1/windowTimes/'yyyyMMdd'/geojson/'")
                .startLocationLatitude(30D)
                .startLocationLongitude(10D)
                .searchRadiusInMeters(1)
                .addAllRoadSectionFragments(true)
                .addRoadSegmentFragmentsThatAreBlockedInAllAvailableDirections(true)
                .addRoadSegmentFragmentsThatAreAccessibleInAllAvailableDirections(true)
                .writeRoadSegmentFragmentsThatArePartiallyAccessibleAsAccessible(true)
                .addTrafficSignsAsLineStrings(true)
                .addTrafficSignsAsPoints(true)
                .trafficSignLineStringDistanceInMeters(5)
                .build();
    }

    @Test
    void validate() {

        validate(generateConfiguration, List.of(), List.of());
    }

    @Test
    void validate_zone_null() {

        generateConfiguration = generateConfiguration.withZone(null);

        validate(generateConfiguration, List.of("zone"), List.of("must not be null"));
    }

    @Test
    void validate_rootExportDirectory_null() {

        generateConfiguration = generateConfiguration.withRootExportDirectory(null);

        validate(generateConfiguration, List.of("rootExportDirectory"), List.of("must not be null"));
    }

    @ParameterizedTest
    @EmptySource
    @Null
    void validate_relativeExportDirectoryPattern_null(String relativeExportDirectoryPattern) {

        generateConfiguration = generateConfiguration.withRelativeExportDirectoryPattern(
                relativeExportDirectoryPattern);

        validate(generateConfiguration, List.of("relativeExportDirectoryPattern"), List.of("must not be blank"));
    }

    @ParameterizedTest
    @CsvSource(nullValues = "null", textBlock = """
            0, must be greater than or equal to 1,
            1, null
            """)
    void validate_searchRadiusInMeters_edgeCases(double searchRadiusInMeters, String expectedError) {

        generateConfiguration = generateConfiguration.withSearchRadiusInMeters(searchRadiusInMeters);

        if (Objects.nonNull(expectedError)) {
            validate(generateConfiguration, List.of("searchRadiusInMeters"), List.of(expectedError));
        } else {
            validate(generateConfiguration, List.of(), List.of());
        }
    }

    @Test
    void validate_addAllRoadSectionFragments_null() {

        generateConfiguration = generateConfiguration.withAddAllRoadSectionFragments(null);

        validate(generateConfiguration, List.of("addAllRoadSectionFragments"), List.of("must not be null"));
    }

    @Test
    void validate_startLocationLatitude_null() {

        generateConfiguration = generateConfiguration.withStartLocationLatitude(null);

        validate(generateConfiguration, List.of("startLocationLatitude"), List.of("must not be null"));
    }

    @Test
    void validate_startLocationLongitude_null() {

        generateConfiguration = generateConfiguration.withStartLocationLongitude(null);

        validate(generateConfiguration, List.of("startLocationLongitude"), List.of("must not be null"));
    }

    @Test
    void validate_addRoadSegmentFragmentsThatAreBlockedInAllAvailableDirections_null() {

        generateConfiguration = generateConfiguration
                .withAddRoadSegmentFragmentsThatAreBlockedInAllAvailableDirections(null);

        validate(
                generateConfiguration,
                List.of("addRoadSegmentFragmentsThatAreBlockedInAllAvailableDirections"),
                List.of("must not be null"));
    }

    @Test
    void validate_addRoadSegmentFragmentsThatAreAccessibleInAllAvailableDirections_null() {

        generateConfiguration = generateConfiguration
                .withAddRoadSegmentFragmentsThatAreAccessibleInAllAvailableDirections(null);

        validate(
                generateConfiguration,
                List.of("addRoadSegmentFragmentsThatAreAccessibleInAllAvailableDirections"),
                List.of("must not be null"));
    }

    @Test
    void validate_writeRoadSegmentFragmentsThatArePartiallyAccessibleAsAccessible_null() {

        generateConfiguration = generateConfiguration
                .withWriteRoadSegmentFragmentsThatArePartiallyAccessibleAsAccessible(null);

        validate(
                generateConfiguration,
                List.of("writeRoadSegmentFragmentsThatArePartiallyAccessibleAsAccessible"),
                List.of("must not be null"));
    }

    @Test
    void validate_addTrafficSignsAsLineStrings_null() {

        generateConfiguration = generateConfiguration.withAddTrafficSignsAsLineStrings(null);

        validate(generateConfiguration, List.of("addTrafficSignsAsLineStrings"), List.of("must not be null"));
    }

    @Test
    void validate_addTrafficSignsAsPoints_null() {

        generateConfiguration = generateConfiguration.withAddTrafficSignsAsPoints(null);

        validate(generateConfiguration, List.of("addTrafficSignsAsPoints"), List.of("must not be null"));
    }

    @ParameterizedTest
    @CsvSource(nullValues = "null", textBlock = """
            0, must be greater than or equal to 1,
            1, null,
            """)
    void validate_trafficSignLineStringDistanceInMeters_edgeCases(
            int trafficSignLineStringDistanceInMeters,
            String expectedError) {

        generateConfiguration = generateConfiguration
                .withTrafficSignLineStringDistanceInMeters(trafficSignLineStringDistanceInMeters);

        if (Objects.nonNull(expectedError)) {
            validate(
                    generateConfiguration,
                    List.of("trafficSignLineStringDistanceInMeters"),
                    List.of(expectedError));
        } else {
            validate(generateConfiguration, List.of(), List.of());
        }
    }

    @Test
    void validate_trafficSignLineStringDistanceInMeters_null() {

        generateConfiguration = generateConfiguration.withTrafficSignLineStringDistanceInMeters(null);

        validate(
                generateConfiguration,
                List.of("trafficSignLineStringDistanceInMeters"),
                List.of("must not be null"));
    }

    @Test
    void getGenerationDirectoryPath() {

        Path directoryPath = generateConfiguration.getGenerationDirectoryPath(OffsetDateTime.parse("2022-03-11T09:00:00.000-01:00"));

        assertThat(directoryPath).hasToString("tmp/tmp/v1/windowTimes/20220311/geojson");
    }

    @Override
    protected Class<?> getClassToTest() {

        return generateConfiguration.getClass();
    }
}
