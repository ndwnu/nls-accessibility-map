package nu.ndw.nls.accessibilitymap.accessibility.service.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.GraphHopperNetwork;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityContextTest {

    @Mock
    private GraphHopperNetwork graphHopperNetwork;

    @Mock
    private List<TrafficSign> trafficSigns;

    @Mock
    private AccessibilityNwbRoadSection accessibilityNwbRoadSection1;

    @Mock
    private AccessibilityNwbRoadSection accessibilityNwbRoadSection2;

    @Test
    void findAllAccessibilityNwbRoadSectionById() {

        var accessibilityNwbRoadSections = new TreeMap<>(Map.of(2L, accessibilityNwbRoadSection1));

        AccessibilityContext accessibilityContext = new AccessibilityContext(
                graphHopperNetwork,
                accessibilityNwbRoadSections,
                trafficSigns,
                1);

        Optional<AccessibilityNwbRoadSection> roadSection = accessibilityContext.findAllAccessibilityNwbRoadSectionById(2);

        assertThat(roadSection)
                .isPresent()
                .contains(accessibilityNwbRoadSection1);
    }

    @Test
    void findAllAccessibilityNwbRoadSectionById_notFound() {

        AccessibilityContext accessibilityContext = new AccessibilityContext(graphHopperNetwork, new TreeMap<>(), trafficSigns, 1);

        Optional<AccessibilityNwbRoadSection> roadSection = accessibilityContext.findAllAccessibilityNwbRoadSectionById(2);

        assertThat(roadSection).isEmpty();
    }

    @Test
    void findAllAccessibilityNwbRoadSectionByMunicipalityId() {

        var accessibilityNwbRoadSections = new TreeMap<>(Map.of(
                2L, accessibilityNwbRoadSection1,
                3L, accessibilityNwbRoadSection2));

        when(accessibilityNwbRoadSection1.municipalityId()).thenReturn(5);
        when(accessibilityNwbRoadSection2.municipalityId()).thenReturn(6);

        AccessibilityContext accessibilityContext = new AccessibilityContext(
                graphHopperNetwork,
                accessibilityNwbRoadSections,
                trafficSigns,
                1);

        List<AccessibilityNwbRoadSection> roadSections = accessibilityContext.findAllAccessibilityNwbRoadSectionByMunicipalityId(6);

        assertThat(roadSections).containsExactly(accessibilityNwbRoadSection2);
    }

    @Test
    void findAllAccessibilityNwbRoadSection() {

        var accessibilityNwbRoadSections = new TreeMap<>(Map.of(
                2L, accessibilityNwbRoadSection1,
                3L, accessibilityNwbRoadSection2));

        AccessibilityContext accessibilityContext = new AccessibilityContext(
                graphHopperNetwork,
                accessibilityNwbRoadSections,
                trafficSigns,
                1);

        List<AccessibilityNwbRoadSection> roadSections = accessibilityContext.findAllAccessibilityNwbRoadSection();

        assertThat(roadSections).containsExactly(accessibilityNwbRoadSection1, accessibilityNwbRoadSection2);
    }
}
