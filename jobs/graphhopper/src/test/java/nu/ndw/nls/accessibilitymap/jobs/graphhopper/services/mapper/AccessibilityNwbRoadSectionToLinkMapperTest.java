package nu.ndw.nls.accessibilitymap.jobs.graphhopper.services.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.AccessibilityLink;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.locationtech.jts.geom.LineString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityNwbRoadSectionToLinkMapperTest {

    @InjectMocks
    private AccessibilityNwbRoadSectionToLinkMapper accessibilityNwbRoadSectionToLinkMapper;

    @Mock
    private LineString lineString;

    @ParameterizedTest
    @CsvSource({
            "true, true",
            "true, false",
            "false, true",
            "false, false"
    })
    @SuppressWarnings("unchecked")
    void map(boolean forwardAccessible, boolean backwardAccessible) {
        when(lineString.getLength()).thenReturn(10.0);

        AccessibilityNwbRoadSection roadSection = createRoadSection(forwardAccessible, backwardAccessible);
        AccessibilityLink link = accessibilityNwbRoadSectionToLinkMapper.map(roadSection);

        assertThat(link.getId()).isEqualTo(roadSection.roadSectionId());
        assertThat(link.getFromNodeId()).isEqualTo(roadSection.fromNode());
        assertThat(link.getToNodeId()).isEqualTo(roadSection.toNode());
        assertThat(link.getAccessibility().forward()).isEqualTo(forwardAccessible);
        assertThat(link.getAccessibility().reverse()).isEqualTo(backwardAccessible);
        assertThat(link.getDistanceInMeters()).isEqualTo(10.0);
        assertThat(link.getGeometry()).isEqualTo(lineString);
        assertThat(link.getMunicipalityCode()).isEqualTo(roadSection.municipalityId());
    }

    private AccessibilityNwbRoadSection createRoadSection(boolean forwardAccessible, boolean backwardAccessible) {
        return new AccessibilityNwbRoadSection(
                1,
                2,
                3,
                4,
                lineString,
                forwardAccessible,
                backwardAccessible
        );
    }
}
