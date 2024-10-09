package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration;

import jakarta.validation.constraints.Null;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.test.unit.ValidationTest;
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
                .rootExportDirectory(Path.of("tmp.tmp"))
                .relativeExportDirectoryPattern("dirPattern")
                .startLocationLatitude(50)
                .startLocationLongitude(3)
                .searchRadiusInMeters(1)
                .addAllRoadSectionFragments(true)
                .addRoadSegmentFragmentsThatAreBlockedInAllAvailableDirections(true)
                .addRoadSegmentFragmentsThatAreAccessibleInAllAvailableDirections(true)
                .writeRoadSegmentFragmentsThatArePartiallyAccessibleAsAccessible(true)
                .addTrafficSignsAsLineStrings(true)
                .addTrafficSignsAsPoints(true)
                .build();
    }

    @Test
    void validate_ok() {

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
            49, must be greater than or equal to 50,
            50, null,
            54, null,
            55, must be less than or equal to 54
            """)
    void validate_startLocationLatitude_edgeCases(double startLocationLatitude, String expectedError) {

        generateConfiguration = generateConfiguration.withStartLocationLatitude(startLocationLatitude);

        if (Objects.nonNull(expectedError)) {
            validate(generateConfiguration, List.of("startLocationLatitude"), List.of(expectedError));
        } else {
            validate(generateConfiguration, List.of(), List.of());
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

        generateConfiguration = generateConfiguration.withStartLocationLongitude(startLocationLongitude);

        if (Objects.nonNull(expectedError)) {
            validate(generateConfiguration, List.of("startLocationLongitude"), List.of(expectedError));
        } else {
            validate(generateConfiguration, List.of(), List.of());
        }
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
    void validate_addRoadSegmentFragmentsThatAreBlockedInAllAvailableDirections_null() {

        generateConfiguration = generateConfiguration.withAddRoadSegmentFragmentsThatAreBlockedInAllAvailableDirections(null);

        validate(generateConfiguration, List.of("addRoadSegmentFragmentsThatAreBlockedInAllAvailableDirections"), List.of("must not be null"));
    }

    @Test
    void validate_addRoadSegmentFragmentsThatAreAccessibleInAllAvailableDirections_null() {

        generateConfiguration = generateConfiguration.withAddRoadSegmentFragmentsThatAreAccessibleInAllAvailableDirections(null);

        validate(generateConfiguration, List.of("addRoadSegmentFragmentsThatAreAccessibleInAllAvailableDirections"), List.of("must not be null"));
    }

    @Test
    void validate_writeRoadSegmentFragmentsThatArePartiallyAccessibleAsAccessible_null() {

        generateConfiguration = generateConfiguration.withWriteRoadSegmentFragmentsThatArePartiallyAccessibleAsAccessible(null);

        validate(generateConfiguration, List.of("writeRoadSegmentFragmentsThatArePartiallyAccessibleAsAccessible"), List.of("must not be null"));
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

    @Override
    protected Class<?> getClassToTest() {

        return generateConfiguration.getClass();
    }
}