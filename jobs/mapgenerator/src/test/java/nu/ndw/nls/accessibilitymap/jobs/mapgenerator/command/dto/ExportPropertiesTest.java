package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.ExportType;
import nu.ndw.nls.springboot.test.util.validation.ValidationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExportPropertiesTest extends ValidationTest {

    private ExportProperties exportProperties;

    @Mock
    private AccessibilityRequest accessibilityRequest;

    @Mock
    private GenerateConfiguration generateConfiguration;

    @BeforeEach
    void setUp() {

        exportProperties = ExportProperties.builder()
                .name(TrafficSignType.C7.name())
                .exportTypes(Set.of(ExportType.ASYMMETRIC_TRAFFIC_SIGNS_GEO_JSON))
                .nwbVersion(2)
                .publishEvents(true)
                .startTime(OffsetDateTime.now())
                .accessibilityRequest(accessibilityRequest)
                .polygonMaxDistanceBetweenPoints(0.000000000000001d)
                .generateConfiguration(generateConfiguration)
                .build();
    }

    @Test
    void validate() {

        validate(exportProperties, List.of(), List.of());
    }

    @Test
    void validate_exportTypes_null() {

        exportProperties = exportProperties.withExportTypes(null);
        validate(exportProperties, List.of("exportTypes", "exportTypes"), List.of("must not be null", "must not be empty"));
    }

    @Test
    void validate_exportTypes_empty() {

        exportProperties = exportProperties.withExportTypes(Set.of());

        validate(exportProperties, List.of("exportTypes"), List.of("must not be empty"));
    }

    @Test
    void validate_nwbVersion_null() {

        exportProperties = exportProperties.withNwbVersion(null);

        validate(exportProperties, List.of("nwbVersion"), List.of("must not be null"));
    }

    @Test
    void validate_publishEvents_null() {

        exportProperties = exportProperties.withPublishEvents(null);

        validate(exportProperties, List.of("publishEvents"), List.of("must not be null"));
    }

    @Test
    void validate_startTime_null() {

        exportProperties = exportProperties.withStartTime(null);

        validate(exportProperties, List.of("startTime"), List.of("must not be null"));
    }

    @ParameterizedTest
    @CsvSource(nullValues = "null", textBlock = """
            0, must be greater than 0,
            0.0000000000001, null,
            """)
    void validate_polygonMaxDistanceBetweenPoints_mustBePositive(double polygonMaxDistanceBetweenPoints,
            String expectedError) {

        exportProperties = exportProperties.withPolygonMaxDistanceBetweenPoints(
                polygonMaxDistanceBetweenPoints);

        if (Objects.nonNull(expectedError)) {
            validate(exportProperties, List.of("polygonMaxDistanceBetweenPoints"), List.of(expectedError));
        } else {
            validate(exportProperties, List.of(), List.of());
        }
    }

    @Test
    void validate_generateConfiguration_null() {

        exportProperties = exportProperties.withGenerateConfiguration(null);

        validate(exportProperties, List.of("generateConfiguration"), List.of("must not be null"));
    }

    @Test
    void validate_accessibilityRequest_null() {

        exportProperties = exportProperties.withAccessibilityRequest(null);

        validate(exportProperties, List.of("accessibilityRequest"), List.of("must not be null"));
    }

    @Override
    protected Class<?> getClassToTest() {
        return exportProperties.getClass();
    }
}
