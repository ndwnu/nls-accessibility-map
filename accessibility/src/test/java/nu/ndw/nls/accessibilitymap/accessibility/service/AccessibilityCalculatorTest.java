package nu.ndw.nls.accessibilitymap.accessibility.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.shapes.BBox;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.IsochroneArguments;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service.IsochroneService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.weighting.RestrictionWeightingAdapter;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityNetwork;
import nu.ndw.nls.accessibilitymap.accessibility.service.mapper.RoadSectionMapper;
import nu.ndw.nls.routingmapmatcher.model.IsochroneMatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityCalculatorTest {

    private AccessibilityCalculator accessibilityCalculator;

    @Mock
    private RoadSectionMapper roadSectionMapper;

    @Mock
    private AccessibilityNetwork accessibilityNetwork;

    @Mock
    private RoadSection roadSection;

    @Mock
    private IsochroneService isochroneService;

    @Mock
    private IsochroneMatch isochroneMatch;

    @Mock
    private Map<Integer, List<Restriction>> restrictionsByEdgeKey;

    @Mock
    private BBox requestArea;

    @Mock
    private Weighting weighting;

    @Mock
    private Set<Integer> blockedEdges;

    private AccessibilityRequest accessibilityRequest;

    @BeforeEach
    void setUp() {

        accessibilityRequest = AccessibilityRequest.builder()
                .municipalityId(1)
                .maxSearchDistanceInMeters(2D)
                .requestArea(requestArea)
                .build();

        accessibilityCalculator = new AccessibilityCalculator(isochroneService, roadSectionMapper);
    }

    @Test
    void calculateWithoutRestrictions() {

        when(accessibilityNetwork.getWeighting()).thenReturn(weighting);
        when(accessibilityNetwork.getRestrictionsByEdgeKey()).thenReturn(restrictionsByEdgeKey);

        when(isochroneService.search(
                eq(accessibilityNetwork),
                argThat(new IsochroneArgumentMatcher(IsochroneArguments
                        .builder()
                        .weighting(new RestrictionWeightingAdapter(weighting, Set.of()))
                        .municipalityId(accessibilityRequest.municipalityId())
                        .boundingBox(accessibilityRequest.requestArea())
                        .searchDistanceInMetres(2.0)
                        .build()))))
                .thenReturn(List.of(isochroneMatch));

        when(roadSectionMapper.mapToRoadSections(List.of(isochroneMatch), restrictionsByEdgeKey)).thenReturn(List.of(roadSection));

        Collection<RoadSection> baseAccessibility = accessibilityCalculator.calculateWithoutRestrictions(
                accessibilityRequest,
                accessibilityNetwork);

        assertThat(baseAccessibility).containsExactly(roadSection);
    }

    @Test
    void calculateWithRestrictions() {

        when(accessibilityNetwork.getWeighting()).thenReturn(weighting);
        when(accessibilityNetwork.getRestrictionsByEdgeKey()).thenReturn(restrictionsByEdgeKey);
        when(accessibilityNetwork.getBlockedEdges()).thenReturn(blockedEdges);

        when(isochroneService.search(
                eq(accessibilityNetwork),
                argThat(new IsochroneArgumentMatcher(IsochroneArguments
                        .builder()
                        .weighting(new RestrictionWeightingAdapter(weighting, blockedEdges))
                        .municipalityId(accessibilityRequest.municipalityId())
                        .boundingBox(accessibilityRequest.requestArea())
                        .searchDistanceInMetres(2.0)
                        .build()))))
                .thenReturn(List.of(isochroneMatch));

        when(roadSectionMapper.mapToRoadSections(List.of(isochroneMatch), restrictionsByEdgeKey)).thenReturn(List.of(roadSection));

        Collection<RoadSection> baseAccessibility = accessibilityCalculator.calculateWithRestrictions(
                accessibilityRequest,
                accessibilityNetwork);

        assertThat(baseAccessibility).containsExactly(roadSection);
    }

    private record IsochroneArgumentMatcher(IsochroneArguments expected) implements
            ArgumentMatcher<IsochroneArguments> {

        @Override
        public boolean matches(IsochroneArguments actual) {
            // Mockito initializes with a null value
            if (actual == null) {
                return false;
            }
            return Objects.equals(expected.municipalityId(), actual.municipalityId()) &&
                   expected.searchDistanceInMetres() == actual.searchDistanceInMetres() &&
                   expected.boundingBox() == actual.boundingBox() &&
                   weightingEquals(expected.weighting(), actual.weighting());
        }

        private boolean weightingEquals(Weighting expectedWeighting, Weighting actualWeighting) {
            if (expected.weighting() instanceof RestrictionWeightingAdapter expectedWeightingAdapter
                && actualWeighting instanceof RestrictionWeightingAdapter actualWeightingAdapter) {
                return Objects.equals(
                        expectedWeightingAdapter.getBlockedEdges(),
                        actualWeightingAdapter.getBlockedEdges());
            } else {
                return Objects.equals(expectedWeighting, actualWeighting);
            }
        }
    }
}
