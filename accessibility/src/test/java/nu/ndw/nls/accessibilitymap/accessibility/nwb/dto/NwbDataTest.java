package nu.ndw.nls.accessibilitymap.accessibility.nwb.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import nu.ndw.nls.springboot.test.util.validation.ValidationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NwbDataTest extends ValidationTest {

    @Mock
    private AccessibilityNwbRoadSection accessibilityNwbRoadSection;

    @Test
    void validate() {

        when(accessibilityNwbRoadSection.roadSectionId()).thenReturn(1L);
        NwbData nwbData = new NwbData(1, List.of(accessibilityNwbRoadSection));
        validate(nwbData, List.of(), List.of());
    }

    @Test
    void validate_nwbVersion_null() {

        when(accessibilityNwbRoadSection.roadSectionId()).thenReturn(1L);
        NwbData nwbData = new NwbData(null, List.of(accessibilityNwbRoadSection));

        validate(nwbData, List.of("nwbVersionId"), List.of("must not be null"));
    }

    @Test
    void consturctor_accessibilityNwbRoadSections_null() {
        assertThat(catchThrowable(() -> new NwbData(1, null)))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void findAllAccessibilityNwbRoadSections() {

        when(accessibilityNwbRoadSection.roadSectionId()).thenReturn(1L);

        AccessibilityNwbRoadSection accessibilityNwbRoadSection2 = mock(AccessibilityNwbRoadSection.class);
        when(accessibilityNwbRoadSection2.roadSectionId()).thenReturn(1L);

        NwbData nwbData = new NwbData(1, List.of(accessibilityNwbRoadSection, accessibilityNwbRoadSection2));

        assertThat(nwbData.findAllAccessibilityNwbRoadSections()).containsExactlyInAnyOrder(
                accessibilityNwbRoadSection,
                accessibilityNwbRoadSection2);
    }

    @Test
    void findAccessibilityNwbRoadSectionById() {

        when(accessibilityNwbRoadSection.roadSectionId()).thenReturn(1L);

        AccessibilityNwbRoadSection accessibilityNwbRoadSection2 = mock(AccessibilityNwbRoadSection.class);
        when(accessibilityNwbRoadSection2.roadSectionId()).thenReturn(1L);

        NwbData nwbData = new NwbData(1, List.of(accessibilityNwbRoadSection, accessibilityNwbRoadSection2));

        assertThat(nwbData.findAccessibilityNwbRoadSectionById(1)).contains(accessibilityNwbRoadSection);
    }

    @Test
    void findAllAccessibilityNwbRoadSectionByMunicipalityId() {

        when(accessibilityNwbRoadSection.roadSectionId()).thenReturn(1L);
        when(accessibilityNwbRoadSection.municipalityId()).thenReturn(2);

        AccessibilityNwbRoadSection accessibilityNwbRoadSection2 = mock(AccessibilityNwbRoadSection.class);
        when(accessibilityNwbRoadSection2.roadSectionId()).thenReturn(1L);
        when(accessibilityNwbRoadSection2.municipalityId()).thenReturn(2);

        NwbData nwbData = new NwbData(1, List.of(accessibilityNwbRoadSection, accessibilityNwbRoadSection2));

        assertThat(nwbData.findAllAccessibilityNwbRoadSectionByMunicipalityId(2)).containsExactlyInAnyOrder(
                accessibilityNwbRoadSection,
                accessibilityNwbRoadSection2);
    }

    @Test
    void toStringTest() {

        NwbData nwbData = new NwbData(1, List.of(accessibilityNwbRoadSection));

        assertThat(nwbData).hasToString("NwbData(nwbVersionId=1)");
    }

    @Override
    protected Class<?> getClassToTest() {
        return NwbData.class;
    }
}
