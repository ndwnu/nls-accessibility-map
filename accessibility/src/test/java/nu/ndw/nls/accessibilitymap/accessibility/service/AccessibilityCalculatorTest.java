package nu.ndw.nls.accessibilitymap.accessibility.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.shapes.BBox;
import io.micrometer.core.annotation.Timed;
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
import nu.ndw.nls.routingmapmatcher.isochrone.algorithm.IsoLabel;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
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
    private IsoLabel isoLabel;

    @Mock
    private Map<Integer, List<Restriction>> restrictionsByEdgeKey;

    @Mock
    private BBox requestArea;

    @Mock
    private Weighting weighting;

    @Mock
    private Set<Integer> blockedEdges;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

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
                .thenReturn(List.of(isoLabel));

        when(roadSectionMapper.map(accessibilityNetwork, List.of(isoLabel), restrictionsByEdgeKey)).thenReturn(List.of(
                roadSection));

        Collection<RoadSection> baseAccessibility = accessibilityCalculator.calculateWithoutRestrictions(
                accessibilityRequest,
                accessibilityNetwork);

        assertThat(baseAccessibility).containsExactly(roadSection);

        loggerExtension.containsLog(
                Level.DEBUG,
                "Calculating accessibility without restrictions for AccessibilityRequest[timestamp=null, requestArea=requestArea, searchArea=null, municipalityId=1, addMissingRoadsSectionsFromNwb=false, maxSearchDistanceInMeters=2.0, startLocationLatitude=null, startLocationLongitude=null, endLocationLatitude=null, endLocationLongitude=null, vehicleLengthInCm=null, vehicleHeightInCm=null, vehicleWidthInCm=null, vehicleWeightInKg=null, vehicleAxleLoadInKg=null, fuelTypes=null, emissionClasses=null, transportTypes=null, trafficSignTypes=null, trafficSignTextSignTypes=null, excludeTrafficSignTextSignTypes=null, excludeTrafficSignZoneCodeTypes=null, excludeRestrictionsWithEmissionZoneIds=null, excludeRestrictionsWithEmissionZoneTypes=null, dynamicRestrictions=null]");
        loggerExtension.containsLog(Level.DEBUG, "Found 1 isochrone labels");
        loggerExtension.containsLog(
                Level.DEBUG,
                "Calculated accessibility without restrictions, found 1 road sections for AccessibilityRequest[timestamp=null, requestArea=requestArea, searchArea=null, municipalityId=1, addMissingRoadsSectionsFromNwb=false, maxSearchDistanceInMeters=2.0, startLocationLatitude=null, startLocationLongitude=null, endLocationLatitude=null, endLocationLongitude=null, vehicleLengthInCm=null, vehicleHeightInCm=null, vehicleWidthInCm=null, vehicleWeightInKg=null, vehicleAxleLoadInKg=null, fuelTypes=null, emissionClasses=null, transportTypes=null, trafficSignTypes=null, trafficSignTextSignTypes=null, excludeTrafficSignTextSignTypes=null, excludeTrafficSignZoneCodeTypes=null, excludeRestrictionsWithEmissionZoneIds=null, excludeRestrictionsWithEmissionZoneTypes=null, dynamicRestrictions=null]");
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
                .thenReturn(List.of(isoLabel));

        when(roadSectionMapper.map(accessibilityNetwork, List.of(isoLabel), restrictionsByEdgeKey)).thenReturn(List.of(
                roadSection));

        Collection<RoadSection> baseAccessibility = accessibilityCalculator.calculateWithRestrictions(
                accessibilityRequest,
                accessibilityNetwork);

        assertThat(baseAccessibility).containsExactly(roadSection);
        loggerExtension.containsLog(
                Level.DEBUG,
                "Calculating accessibility with restrictions for AccessibilityRequest[timestamp=null, requestArea=requestArea, searchArea=null, municipalityId=1, addMissingRoadsSectionsFromNwb=false, maxSearchDistanceInMeters=2.0, startLocationLatitude=null, startLocationLongitude=null, endLocationLatitude=null, endLocationLongitude=null, vehicleLengthInCm=null, vehicleHeightInCm=null, vehicleWidthInCm=null, vehicleWeightInKg=null, vehicleAxleLoadInKg=null, fuelTypes=null, emissionClasses=null, transportTypes=null, trafficSignTypes=null, trafficSignTextSignTypes=null, excludeTrafficSignTextSignTypes=null, excludeTrafficSignZoneCodeTypes=null, excludeRestrictionsWithEmissionZoneIds=null, excludeRestrictionsWithEmissionZoneTypes=null, dynamicRestrictions=null]");
        loggerExtension.containsLog(Level.DEBUG, "Found 1 isochrone labels");
        loggerExtension.containsLog(
                Level.DEBUG,
                "Calculated accessibility with restrictions, found 1 road sections for AccessibilityRequest[timestamp=null, requestArea=requestArea, searchArea=null, municipalityId=1, addMissingRoadsSectionsFromNwb=false, maxSearchDistanceInMeters=2.0, startLocationLatitude=null, startLocationLongitude=null, endLocationLatitude=null, endLocationLongitude=null, vehicleLengthInCm=null, vehicleHeightInCm=null, vehicleWidthInCm=null, vehicleWeightInKg=null, vehicleAxleLoadInKg=null, fuelTypes=null, emissionClasses=null, transportTypes=null, trafficSignTypes=null, trafficSignTextSignTypes=null, excludeTrafficSignTextSignTypes=null, excludeTrafficSignZoneCodeTypes=null, excludeRestrictionsWithEmissionZoneIds=null, excludeRestrictionsWithEmissionZoneTypes=null, dynamicRestrictions=null]");
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

    @Test
    void calculateWithRestrictions_containsTimeAnnotation() {

        AnnotationUtil.methodContainsAnnotation(
                accessibilityCalculator.getClass(),
                Timed.class,
                "calculateWithRestrictions",
                annotation -> {
                    assertThat(annotation).isNotNull();
                    assertThat(annotation.value()).isEqualTo("accessibilitymap.accessibility.calculateWithRestrictions");
                }
        );
    }

    @Test
    void calculateWithoutRestrictions_containsTimeAnnotation() {

        AnnotationUtil.methodContainsAnnotation(
                accessibilityCalculator.getClass(),
                Timed.class,
                "calculateWithoutRestrictions",
                annotation -> {
                    assertThat(annotation).isNotNull();
                    assertThat(annotation.value()).isEqualTo("accessibilitymap.accessibility.calculateWithoutRestrictions");
                }
        );
    }
}
