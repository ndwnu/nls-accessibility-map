package nu.ndw.nls.accessibilitymap.accessibility.nwb.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.Mockito.mock;

import java.util.List;
import nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode;
import nu.ndw.nls.springboot.test.util.validation.ValidationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NwbDataTest extends ValidationTest {

    @Test
    void validate() {
        AccessibilityNwbRoadSection accessibilityNwbRoadSection = AccessibilityNwbRoadSection.builder()
                .functionalRoadClass("A")
                .carriagewayTypeCode(CarriagewayTypeCode.HR)
                .build();

        NwbData nwbData = new NwbData(1, List.of(accessibilityNwbRoadSection));
        validate(nwbData, List.of(), List.of());
    }

    @Test
    void validate_nwbVersion_null() {
        AccessibilityNwbRoadSection accessibilityNwbRoadSection = AccessibilityNwbRoadSection.builder()
                .functionalRoadClass("A")
                .carriagewayTypeCode(CarriagewayTypeCode.HR)
                .build();

        NwbData nwbData = new NwbData(null, List.of(accessibilityNwbRoadSection));

        validate(nwbData, List.of("nwbVersionId"), List.of("must not be null"));
    }

    @Test
    @SuppressWarnings("DataFlowIssue")
    void constructor_accessibilityNwbRoadSections_null() {
        assertThat(catchThrowable(() -> new NwbData(1, null)))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void findAllAccessibilityNwbRoadSections() {

        AccessibilityNwbRoadSection accessibilityNwbRoadSection = mock(AccessibilityNwbRoadSection.class);
        NwbData nwbData = new NwbData(1, List.of(accessibilityNwbRoadSection));

        assertThat(nwbData.findAllAccessibilityNwbRoadSections()).containsExactly(accessibilityNwbRoadSection);
    }

    @Test
    void toStringTest() {

        NwbData nwbData = new NwbData(1, List.of());

        assertThat(nwbData).hasToString("NwbData(nwbVersionId=1)");
    }

    @Override
    protected Class<?> getClassToTest() {
        return NwbData.class;
    }
}
