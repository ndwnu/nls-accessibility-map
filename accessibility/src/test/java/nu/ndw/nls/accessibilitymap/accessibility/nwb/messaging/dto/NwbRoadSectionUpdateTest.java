package nu.ndw.nls.accessibilitymap.accessibility.nwb.messaging.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import lombok.SneakyThrows;
import nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode;
import nu.ndw.nls.springboot.test.util.validation.ValidationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NwbRoadSectionUpdateTest extends ValidationTest {

    private static final String JSON_STRING = """
            {"roadSectionId":600547901,"nwbVersion":"2026-03-01","drivingDirection":"BACK","carriagewayTypeCode":"FP"}
            """;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @SneakyThrows
    @Test
    void deserialize_ok() {
        var expectedRoadChange = NwbRoadSectionUpdate.builder()
                .roadSectionId(600547901)
                .nwbVersion(LocalDate.of(2026, 3, 1))
                .drivingDirection(DrivingDirection.BACK)
                .carriagewayTypeCode(CarriagewayTypeCode.FP)
                .build();
        var roadChange = objectMapper.readValue(JSON_STRING, NwbRoadSectionUpdate.class);
        assertThat(roadChange).isEqualTo(expectedRoadChange);
    }

    @Test
    void validate() {
        var roadUpdate = NwbRoadSectionUpdate.builder()
                .roadSectionId(600547901)
                .nwbVersion(LocalDate.of(2026, 3, 1))
                .drivingDirection(DrivingDirection.BACK)
                .carriagewayTypeCode(CarriagewayTypeCode.FP)
                .build();

        validate(roadUpdate, List.of(), List.of());
    }

    @Test
    void validate_non_null_fields() {
        var roadUpdate = NwbRoadSectionUpdate.builder()
                .roadSectionId(600547901)
                .nwbVersion(LocalDate.of(2026, 3, 1))
                .build();

        validate(roadUpdate, List.of(), List.of());
    }

    @Test
    void validate_roadSectionId_null() {
        var roadUpdate = NwbRoadSectionUpdate.builder()
                .roadSectionId(600547901)
                .drivingDirection(DrivingDirection.BACK)
                .carriagewayTypeCode(CarriagewayTypeCode.FP)
                .build();
        validate(roadUpdate, List.of("nwbVersion"), List.of("must not be null"));
    }

    @Test
    void validate_nwbVersion_null() {
        var roadUpdate = NwbRoadSectionUpdate.builder()
                .nwbVersion(LocalDate.of(2026, 3, 1))
                .drivingDirection(DrivingDirection.BACK)
                .carriagewayTypeCode(CarriagewayTypeCode.FP)
                .build();
        validate(roadUpdate, List.of("roadSectionId"), List.of("must not be null"));
    }

    @Override
    protected Class<?> getClassToTest() {
        return NwbRoadSectionUpdate.class;
    }
}
