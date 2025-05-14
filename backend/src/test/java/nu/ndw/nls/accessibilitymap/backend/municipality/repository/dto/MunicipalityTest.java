package nu.ndw.nls.accessibilitymap.backend.municipality.repository.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import nu.ndw.nls.springboot.test.util.validation.ValidationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MunicipalityTest extends ValidationTest {

    private Municipality municipality;

    @BeforeEach
    void setUp() {
        municipality = Municipality.builder()
                .name("name")
                .startCoordinateLatitude(2D)
                .startCoordinateLongitude(3D)
                .searchDistanceInMetres(4)
                .municipalityId("ABC123")
                .bounds(new MunicipalityBoundingBox(11D, 12D, 13D, 14D))
                .dateLastCheck(LocalDate.MAX)
                .build();
    }

    @Test
    void validate() {

        validate(municipality, List.of(), List.of());
    }

    @Test
    void validate_name_null() {

        municipality = municipality.withName(null);

        validate(municipality,
                List.of("name"),
                List.of("must not be null"));
    }

    @Test
    void validate_startCoordinateLatitude_null() {

        municipality = municipality.withStartCoordinateLatitude(null);

        validate(municipality,
                List.of("startCoordinateLatitude"),
                List.of("must not be null"));
    }

    @Test
    void validate_startCoordinateLongitude_null() {

        municipality = municipality.withStartCoordinateLongitude(null);

        validate(municipality,
                List.of("startCoordinateLongitude"),
                List.of("must not be null"));
    }

    @Test
    void validate_searchDistanceInMetres_null() {

        municipality = municipality.withSearchDistanceInMetres(null);

        validate(municipality,
                List.of("searchDistanceInMetres"),
                List.of("must not be null"));
    }

    @Test
    void validate_municipalityId_null() {

        municipality = municipality.withMunicipalityId(null);

        validate(municipality,
                List.of("municipalityId"),
                List.of("must not be null"));
    }

    @Test
    void validate_bounds_null() {

        municipality = municipality.withBounds(null);

        validate(municipality,
                List.of("bounds"),
                List.of("must not be null"));
    }

    @ParameterizedTest
    @CsvSource(nullValues = "null", textBlock = """
            GM0012, 12
            gm1, 1
            ASasdfasdfDF29, 29
            1, null
            """)
    void municipalityIdAsInteger(String municipalityId, Integer expectedResult) {

        Municipality municipality = Municipality.builder()
                .municipalityId(municipalityId)
                .build();

        if (Objects.isNull(expectedResult)) {
            assertThat(catchThrowable(municipality::municipalityIdAsInteger))
                    .withFailMessage("Incorrect municipalityId %s".formatted(municipalityId))
                    .isInstanceOf(IllegalStateException.class);
        } else {
            assertThat(municipality.municipalityIdAsInteger()).isEqualTo(expectedResult);
        }
    }

    @Override
    protected Class<?> getClassToTest() {

        return Municipality.class;
    }

    @Test
    void annotation_validated() {

        AnnotationUtil.fieldContainsAnnotation(
                this.getClassToTest(),
                Valid.class,
                "bounds",
                (annotation) -> Assertions.assertThat(annotation).isNotNull());
    }
}