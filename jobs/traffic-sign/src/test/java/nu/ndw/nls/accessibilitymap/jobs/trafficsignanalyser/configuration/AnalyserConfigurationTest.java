package nu.ndw.nls.accessibilitymap.jobs.trafficsignanalyser.configuration;

import java.util.List;
import java.util.Objects;
import nu.ndw.nls.springboot.test.util.validation.ValidationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnalyserConfigurationTest extends ValidationTest {

    private AnalyserConfiguration analyserConfiguration;

    @BeforeEach
    void setUp() {

        analyserConfiguration = AnalyserConfiguration.builder()
                .startLocationLatitude(51d)
                .startLocationLongitude(6d)
                .searchRadiusInMeters(1)
                .build();
    }

    @Test
    void validate() {

        validate(analyserConfiguration, List.of(), List.of());
    }

    @ParameterizedTest
    @CsvSource(nullValues = "null", textBlock = """
            0, must be greater than or equal to 1,
            1, null
            """)
    void validate_searchRadiusInMeters_edgeCases(double searchRadiusInMeters, String expectedError) {

        analyserConfiguration = analyserConfiguration.withSearchRadiusInMeters(searchRadiusInMeters);

        if (Objects.nonNull(expectedError)) {
            validate(analyserConfiguration, List.of("searchRadiusInMeters"), List.of(expectedError));
        } else {
            validate(analyserConfiguration, List.of(), List.of());
        }
    }

    @Test
    void validate_startLocationLatitude_null() {

        analyserConfiguration = analyserConfiguration.withStartLocationLatitude(null);

        validate(analyserConfiguration, List.of("startLocationLatitude"), List.of("must not be null"));
    }

    @Test
    void validate_startLocationLongitude_null() {

        analyserConfiguration = analyserConfiguration.withStartLocationLongitude(null);

        validate(analyserConfiguration, List.of("startLocationLongitude"), List.of("must not be null"));
    }

    @Override
    protected Class<?> getClassToTest() {
        return analyserConfiguration.getClass();
    }
}