package nu.ndw.nls.accessibilitymap.accessibility.nwb.dto;

import java.util.List;
import nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode;
import nu.ndw.nls.springboot.test.util.validation.ValidationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.LineString;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityNwbRoadSectionTest extends ValidationTest {

    @Mock
    private LineString lineString;

    @Test
    void validate() {
        AccessibilityNwbRoadSection accessibilityNwbRoadSection = new AccessibilityNwbRoadSection(
                1,
                2,
                3,
                4,
                lineString,
                true,
                true,
                CarriagewayTypeCode.RB,
                "1");
        validate(accessibilityNwbRoadSection, List.of(), List.of());
    }

    @Test
    void validate_geometry_null() {
        AccessibilityNwbRoadSection accessibilityNwbRoadSection = new AccessibilityNwbRoadSection(
                1,
                2,
                3,
                4,
                null,
                true,
                true,
                CarriagewayTypeCode.RB,
                "1");
        validate(accessibilityNwbRoadSection, List.of("geometry"), List.of("must not be null"));
    }

    @Test
    void validate_functionalRoadClass_null() {
        AccessibilityNwbRoadSection accessibilityNwbRoadSection = new AccessibilityNwbRoadSection(
                1,
                2,
                3,
                4,
                lineString,
                true,
                true,
                CarriagewayTypeCode.RB,
                null
        );
        validate(accessibilityNwbRoadSection, List.of("functionalRoadClass"), List.of("must not be null"));
    }

    @Test
    void validate_functionalRoadClass_mustBeLengthOfOne() {
        AccessibilityNwbRoadSection accessibilityNwbRoadSection = new AccessibilityNwbRoadSection(
                1,
                2,
                3,
                4,
                lineString,
                true,
                true,
                CarriagewayTypeCode.RB,
                ""
        );
        validate(accessibilityNwbRoadSection, List.of("functionalRoadClass"), List.of("length must be between 1 and 1"));

        accessibilityNwbRoadSection = new AccessibilityNwbRoadSection(
                1,
                2,
                3,
                4,
                lineString,
                true,
                true,
                CarriagewayTypeCode.RB,
                "df"
        );
        validate(accessibilityNwbRoadSection, List.of("functionalRoadClass"), List.of("length must be between 1 and 1"));
    }

    @Test
    void validate_carriagewayTypeCode_mustNotBeNull() {
        AccessibilityNwbRoadSection accessibilityNwbRoadSection = new AccessibilityNwbRoadSection(
                1,
                2,
                3,
                4,
                lineString,
                true,
                true,
                null,
                "1"
        );
        validate(accessibilityNwbRoadSection, List.of("carriagewayTypeCode"), List.of("must not be null"));

        accessibilityNwbRoadSection = new AccessibilityNwbRoadSection(
                1,
                2,
                3,
                4,
                lineString,
                true,
                true,
                CarriagewayTypeCode.RB,
                "df"
        );
        validate(accessibilityNwbRoadSection, List.of("functionalRoadClass"), List.of("length must be between 1 and 1"));
    }

    @Override
    protected Class<?> getClassToTest() {
        return AccessibilityNwbRoadSection.class;
    }
}
