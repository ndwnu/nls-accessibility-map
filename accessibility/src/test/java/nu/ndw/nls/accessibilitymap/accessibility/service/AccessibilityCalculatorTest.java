package nu.ndw.nls.accessibilitymap.accessibility.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.PMap;
import com.graphhopper.util.shapes.BBox;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.GraphHopperNetwork;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.IsochroneArguments;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.factory.IsochroneServiceFactory;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service.IsochroneService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.weighting.RestrictionWeightingAdapter;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityNetwork;
import nu.ndw.nls.accessibilitymap.accessibility.service.mapper.RoadSectionMapper;
import nu.ndw.nls.routingmapmatcher.model.IsochroneMatch;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
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
    private IsochroneServiceFactory isochroneServiceFactory;

    @Mock
    private RoadSectionMapper roadSectionMapper;

    @Mock
    private AccessibilityNetwork accessibilityNetwork;

    @Mock
    private NetworkData networkData;

    @Mock
    private GraphHopperNetwork graphHopperNetwork;

    @Mock
    private RoadSection roadSection;

    @Mock
    private Snap from;

    @Mock
    private NetworkGraphHopper network;

    @Mock
    private Weighting weightingNoRestrictions;

    @Mock
    private IsochroneService isochroneService;

    @Mock
    private IsochroneMatch isochroneMatch;

    @Mock
    private QueryGraph queryGraph;

    @Mock
    private Map<Integer, List<Restriction>> restrictionsByEdgeKey;
    @Mock
    private BBox requestArea;

    private AccessibilityRequest accessibilityRequest;

    @BeforeEach
    void setUp() {

        accessibilityRequest = AccessibilityRequest.builder()
                .municipalityId(1)
                .maxSearchDistanceInMeters(2D)
                .requestArea(requestArea)
                .build();

        accessibilityCalculator = new AccessibilityCalculator(isochroneServiceFactory, roadSectionMapper);
    }

    @Test
    void calculateWithoutRestrictions() {

        when(accessibilityNetwork.getRestrictionsByEdgeKey()).thenReturn(restrictionsByEdgeKey);
        when(accessibilityNetwork.getQueryGraph()).thenReturn(queryGraph);
        when(network.createWeighting(eq(NetworkConstants.CAR_PROFILE), argThat(new PMapArgumentMatcher(new PMap())))).thenReturn(
                weightingNoRestrictions);
        when(accessibilityNetwork.getFrom()).thenReturn(from);
        when(accessibilityNetwork.getNetworkData()).thenReturn(networkData);
        when(networkData.getGraphHopperNetwork()).thenReturn(graphHopperNetwork);
        when(networkData.getGraphHopperNetwork()).thenReturn(graphHopperNetwork);
        when(graphHopperNetwork.network()).thenReturn(network);

        when(isochroneServiceFactory.createService(accessibilityNetwork)).thenReturn(isochroneService);
        when(isochroneService.getIsochroneMatchesByMunicipalityId(
                argThat(new IsochroneArgumentMatcher(IsochroneArguments
                        .builder()
                        .weighting(new RestrictionWeightingAdapter(weightingNoRestrictions, Set.of()))
                        .municipalityId(accessibilityRequest.municipalityId())
                        .boundingBox(accessibilityRequest.requestArea())
                        .searchDistanceInMetres(2.0)
                        .build())),
                eq(queryGraph),
                eq(from)))
                .thenReturn(List.of(isochroneMatch));

        when(roadSectionMapper.mapToRoadSections(List.of(isochroneMatch), restrictionsByEdgeKey)).thenReturn(List.of(roadSection));

        Collection<RoadSection> baseAccessibility = accessibilityCalculator.calculateWithoutRestrictions(
                accessibilityRequest,
                accessibilityNetwork);

        assertThat(baseAccessibility).containsExactly(roadSection);
    }

    @Test
    void calculateWithRestrictions() {

        when(accessibilityNetwork.getRestrictionsByEdgeKey()).thenReturn(restrictionsByEdgeKey);
        when(accessibilityNetwork.getQueryGraph()).thenReturn(queryGraph);
        when(network.createWeighting(eq(NetworkConstants.CAR_PROFILE), argThat(new PMapArgumentMatcher(new PMap())))).thenReturn(
                weightingNoRestrictions);
        when(accessibilityNetwork.getFrom()).thenReturn(from);
        when(accessibilityNetwork.getNetworkData()).thenReturn(networkData);
        when(networkData.getGraphHopperNetwork()).thenReturn(graphHopperNetwork);
        when(networkData.getGraphHopperNetwork()).thenReturn(graphHopperNetwork);
        when(graphHopperNetwork.network()).thenReturn(network);

        when(isochroneServiceFactory.createService(accessibilityNetwork)).thenReturn(isochroneService);
        when(isochroneService.getIsochroneMatchesByMunicipalityId(
                argThat(new IsochroneArgumentMatcher(IsochroneArguments
                        .builder()
                        .weighting(new RestrictionWeightingAdapter(weightingNoRestrictions, Set.of()))
                        .municipalityId(accessibilityRequest.municipalityId())
                        .boundingBox(accessibilityRequest.requestArea())
                        .searchDistanceInMetres(2.0)
                        .build())),
                eq(queryGraph),
                eq(from)))
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
            // Mockito initializes with null value
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

    private record PMapArgumentMatcher(PMap expected) implements ArgumentMatcher<PMap> {

        @Override
        public boolean matches(PMap actual) {
            // Mockito initializes with null value
            if (actual == null) {
                return false;
            }

            return expected.toMap().equals(actual.toMap());
        }
    }
}
