package nu.ndw.nls.accessibilitymap.accessibility.service;

import static net.logstash.logback.argument.StructuredArguments.keyValue;
import static nu.ndw.nls.accessibilitymap.accessibility.core.log.LogConstants.ACCESSIBILITY_REQUEST;
import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;
import io.micrometer.core.annotation.Timed;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Location;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.Accessibility;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.core.log.LogUtil;
import nu.ndw.nls.accessibilitymap.accessibility.core.util.LocationFactory;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.GraphHopperNetwork;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.IsochroneArguments;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.factory.IsochroneServiceFactory;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service.BaseAccessibilityCalculator;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service.IsochroneService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.weighting.RestrictionWeightingAdapter;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.reason.mapper.RoadSectionMapper;
import nu.ndw.nls.accessibilitymap.accessibility.reason.service.AccessibilityReasonService;
import nu.ndw.nls.accessibilitymap.accessibility.restriction.RestrictionService;
import nu.ndw.nls.routingmapmatcher.model.IsochroneMatch;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import nu.ndw.nls.springboot.core.time.ClockService;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityServiceTest {

    @Mock
    private LocationFactory locationFactory;

    @Mock
    private IsochroneServiceFactory isochroneServiceFactory;

    @Mock
    private NetworkGraphHopper networkGraphHopper;

    @Mock
    private EncodingManager encodingManager;

    @Mock
    private IntEncodedValue idIntEncodedValue;

    @Mock
    private RoadSectionMapper roadSectionMapper;

    @Mock
    private RoadSectionCombinator roadSectionCombinator;

    @Mock
    private ClockService clockService;

    @Mock
    private IsochroneService isochroneService;

    @Mock
    private EdgeIteratorState endSegmentClosestEdge;

    @Mock
    private QueryGraph queryGraph;

    @Mock
    private Weighting weightingNoRestrictions;

    @Mock
    private RoadSection roadSectionNoRestriction;

    @Mock
    private RoadSection roadSectionRestriction;

    @Mock
    private RoadSection roadSectionCombined;

    @Mock
    private RoadSection roadSectionDestination;

    @Mock
    private RoadSection missingRoadSection;

    @Mock
    private IsochroneMatch isochroneMatchRestriction;

    @Mock
    private RestrictionService restrictionService;

    @Mock
    private GraphHopperService graphHopperService;

    @Mock
    private BaseAccessibilityCalculator baseAccessibilityCalculator;

    private AccessibilityService accessibilityService;

    @Mock
    private MissingRoadSectionProvider missingRoadSectionProvider;

    @Mock
    private AccessibilityReasonService accessibilityReasonService;

    @Mock
    private List<List<AccessibilityReason>> accessibilityReasons;

    @Mock
    private Location from;

    @Mock
    private Location destination;

    @Mock
    private Set<Integer> blockedEdges;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @Mock
    private Restrictions restrictions;

    @Mock
    private GraphHopperNetwork graphHopperNetwork;

    @Mock
    private Snap fromSnap;

    @Mock
    private Snap destinationSnap;

    private AccessibilityRequest accessibilityRequest;

    @BeforeEach
    void setUp() {

        accessibilityService = new AccessibilityService(
                locationFactory,
                isochroneServiceFactory,
                restrictionService,
                roadSectionMapper,
                clockService,
                baseAccessibilityCalculator,
                roadSectionCombinator,
                missingRoadSectionProvider,
                accessibilityReasonService,
                graphHopperService);

        accessibilityRequest = AccessibilityRequest.builder()
                .startLocationLatitude(1.0)
                .startLocationLongitude(2.0)
                .endLocationLatitude(3.0)
                .endLocationLongitude(4.0)
                .municipalityId(5)
                .searchRadiusInMeters(6.0)
                .addMissingRoadsSectionsFromNwb(true)
                .build();
    }

    @Test
    void calculateAccessibility() {

        when(clockService.now())
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:00:00.000-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:00:00.123-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        when(restrictionService.findAllBy(accessibilityRequest)).thenReturn(restrictions);

        mockFromAndDestination(accessibilityRequest);
        mockGraphHopperNetwork(accessibilityRequest);

        when(baseAccessibilityCalculator.calculate(
                graphHopperNetwork,
                accessibilityRequest.municipalityId(),
                accessibilityRequest.searchRadiusInMeters()))
                .thenReturn(new HashSet<>(Set.of(roadSectionNoRestriction)));

        when(missingRoadSectionProvider.get(20, accessibilityRequest.municipalityId(), Set.of(roadSectionNoRestriction), false))
                .thenReturn(List.of(missingRoadSection));

        when(roadSectionCombinator.combineNoRestrictionsWithAccessibilityRestrictions(
                Set.of(roadSectionNoRestriction, missingRoadSection),
                Set.of(roadSectionRestriction, missingRoadSection)))
                .thenReturn(Set.of(roadSectionCombined, roadSectionDestination));

        calculateRoadsSectionsWithAppliedRestrictions(accessibilityRequest);

        mockFindDestinationRoadSection(true);
        when(roadSectionDestination.getId()).thenReturn(30L);
        when(roadSectionDestination.isRestrictedInAnyDirection()).thenReturn(true);

        mockCalculateReasons(accessibilityRequest);

        try (var logUtilStaticMock = Mockito.mockStatic(LogUtil.class)) {
            logUtilStaticMock.when(() -> LogUtil.keyValueJson(ACCESSIBILITY_REQUEST, accessibilityRequest))
                    .thenReturn(keyValue("key", "value"));

            Accessibility accessibility = accessibilityService.calculateAccessibility(accessibilityRequest);

            assertThat(accessibility.combinedAccessibility()).containsExactlyInAnyOrder(roadSectionCombined, roadSectionDestination);
            assertThat(accessibility.accessibleRoadsSectionsWithoutAppliedRestrictions()).containsExactlyInAnyOrder(
                    roadSectionNoRestriction,
                    missingRoadSection);
            assertThat(accessibility.accessibleRoadSectionsWithAppliedRestrictions()).containsExactlyInAnyOrder(
                    roadSectionRestriction,
                    missingRoadSection);
            assertThat(accessibility.unroutableRoadSections()).containsExactlyInAnyOrder(missingRoadSection);
            assertThat(accessibility.toRoadSection()).contains(roadSectionDestination);
            assertThat(accessibility.reasons()).isEqualTo(accessibilityReasons);

            loggerExtension.containsLog(Level.INFO, "Calculating accessibility for key=value");
            loggerExtension.containsLog(Level.DEBUG, "Accessibility calculation done. It took: 123 ms");
        }
    }

    @Test
    void calculateAccessibility_noAddingMissingRoadSections() {

        accessibilityRequest = accessibilityRequest.withAddMissingRoadsSectionsFromNwb(false);

        when(clockService.now())
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:00:00.000-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:00:00.123-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        when(restrictionService.findAllBy(accessibilityRequest)).thenReturn(restrictions);

        mockFromAndDestination(accessibilityRequest);
        mockGraphHopperNetwork(accessibilityRequest);

        when(baseAccessibilityCalculator.calculate(
                graphHopperNetwork,
                accessibilityRequest.municipalityId(),
                accessibilityRequest.searchRadiusInMeters()))
                .thenReturn(new HashSet<>(Set.of(roadSectionNoRestriction)));

        when(roadSectionCombinator.combineNoRestrictionsWithAccessibilityRestrictions(
                Set.of(roadSectionNoRestriction),
                Set.of(roadSectionRestriction)))
                .thenReturn(Set.of(roadSectionCombined, roadSectionDestination));

        calculateRoadsSectionsWithAppliedRestrictions(accessibilityRequest);

        mockFindDestinationRoadSection(true);
        when(roadSectionDestination.getId()).thenReturn(30L);
        when(roadSectionDestination.isRestrictedInAnyDirection()).thenReturn(true);

        mockCalculateReasons(accessibilityRequest);

        try (var logUtilStaticMock = Mockito.mockStatic(LogUtil.class)) {
            logUtilStaticMock.when(() -> LogUtil.keyValueJson(ACCESSIBILITY_REQUEST, accessibilityRequest))
                    .thenReturn(keyValue("key", "value"));

            Accessibility accessibility = accessibilityService.calculateAccessibility(accessibilityRequest);

            assertThat(accessibility.combinedAccessibility()).containsExactlyInAnyOrder(roadSectionCombined, roadSectionDestination);
            assertThat(accessibility.accessibleRoadsSectionsWithoutAppliedRestrictions()).containsExactlyInAnyOrder(roadSectionNoRestriction);
            assertThat(accessibility.accessibleRoadSectionsWithAppliedRestrictions()).containsExactlyInAnyOrder(roadSectionRestriction);
            assertThat(accessibility.unroutableRoadSections()).isEmpty();
            assertThat(accessibility.toRoadSection()).contains(roadSectionDestination);
            assertThat(accessibility.reasons()).isEqualTo(accessibilityReasons);

            loggerExtension.containsLog(Level.INFO, "Calculating accessibility for key=value");
            loggerExtension.containsLog(Level.DEBUG, "Accessibility calculation done. It took: 123 ms");
        }
    }

    @Test
    void calculateAccessibility_requestHasNoEndLocation() {

        accessibilityRequest = accessibilityRequest
                .withEndLocationLatitude(null)
                .withEndLocationLongitude(null);

        when(clockService.now())
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:00:00.000-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:00:00.123-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        when(restrictionService.findAllBy(accessibilityRequest)).thenReturn(restrictions);

        mockFromAndDestination(accessibilityRequest);
        mockGraphHopperNetwork(accessibilityRequest);

        when(baseAccessibilityCalculator.calculate(
                graphHopperNetwork,
                accessibilityRequest.municipalityId(),
                accessibilityRequest.searchRadiusInMeters()))
                .thenReturn(new HashSet<>(Set.of(roadSectionNoRestriction)));

        when(missingRoadSectionProvider.get(20, accessibilityRequest.municipalityId(), Set.of(roadSectionNoRestriction), false))
                .thenReturn(List.of(missingRoadSection));

        when(roadSectionCombinator.combineNoRestrictionsWithAccessibilityRestrictions(
                Set.of(roadSectionNoRestriction, missingRoadSection),
                Set.of(roadSectionRestriction, missingRoadSection)))
                .thenReturn(Set.of(roadSectionCombined, roadSectionDestination));

        calculateRoadsSectionsWithAppliedRestrictions(accessibilityRequest);

        mockFindDestinationRoadSection(false);

        try (var logUtilStaticMock = Mockito.mockStatic(LogUtil.class)) {
            logUtilStaticMock.when(() -> LogUtil.keyValueJson(ACCESSIBILITY_REQUEST, accessibilityRequest))
                    .thenReturn(keyValue("key", "value"));

            Accessibility accessibility = accessibilityService.calculateAccessibility(accessibilityRequest);

            assertThat(accessibility.combinedAccessibility()).containsExactlyInAnyOrder(roadSectionCombined, roadSectionDestination);
            assertThat(accessibility.accessibleRoadsSectionsWithoutAppliedRestrictions()).containsExactlyInAnyOrder(
                    roadSectionNoRestriction,
                    missingRoadSection);
            assertThat(accessibility.accessibleRoadSectionsWithAppliedRestrictions()).containsExactlyInAnyOrder(
                    roadSectionRestriction,
                    missingRoadSection);
            assertThat(accessibility.unroutableRoadSections()).containsExactlyInAnyOrder(missingRoadSection);
            assertThat(accessibility.toRoadSection()).isEmpty();
            assertThat(accessibility.reasons()).isEmpty();

            loggerExtension.containsLog(Level.INFO, "Calculating accessibility for key=value");
            loggerExtension.containsLog(Level.DEBUG, "Accessibility calculation done. It took: 123 ms");
        }
    }

    @Test
    void calculateAccessibility_destinationNotFound() {

        when(clockService.now())
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:00:00.000-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:00:00.123-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        when(restrictionService.findAllBy(accessibilityRequest)).thenReturn(restrictions);

        mockFromAndDestination(accessibilityRequest);
        mockGraphHopperNetwork(accessibilityRequest);

        when(baseAccessibilityCalculator.calculate(
                graphHopperNetwork,
                accessibilityRequest.municipalityId(),
                accessibilityRequest.searchRadiusInMeters()))
                .thenReturn(new HashSet<>(Set.of(roadSectionNoRestriction)));

        when(missingRoadSectionProvider.get(20, accessibilityRequest.municipalityId(), Set.of(roadSectionNoRestriction), false))
                .thenReturn(List.of(missingRoadSection));

        when(roadSectionCombinator.combineNoRestrictionsWithAccessibilityRestrictions(
                Set.of(roadSectionNoRestriction, missingRoadSection),
                Set.of(roadSectionRestriction, missingRoadSection)))
                .thenReturn(Set.of(roadSectionCombined, roadSectionDestination));

        calculateRoadsSectionsWithAppliedRestrictions(accessibilityRequest);

        mockFindDestinationRoadSection(false);

        try (var logUtilStaticMock = Mockito.mockStatic(LogUtil.class)) {
            logUtilStaticMock.when(() -> LogUtil.keyValueJson(ACCESSIBILITY_REQUEST, accessibilityRequest))
                    .thenReturn(keyValue("key", "value"));

            Accessibility accessibility = accessibilityService.calculateAccessibility(accessibilityRequest);

            assertThat(accessibility.combinedAccessibility()).containsExactlyInAnyOrder(roadSectionCombined, roadSectionDestination);
            assertThat(accessibility.accessibleRoadsSectionsWithoutAppliedRestrictions()).containsExactlyInAnyOrder(
                    roadSectionNoRestriction,
                    missingRoadSection);
            assertThat(accessibility.accessibleRoadSectionsWithAppliedRestrictions()).containsExactlyInAnyOrder(
                    roadSectionRestriction,
                    missingRoadSection);
            assertThat(accessibility.unroutableRoadSections()).containsExactlyInAnyOrder(missingRoadSection);
            assertThat(accessibility.toRoadSection()).isEmpty();
            assertThat(accessibility.reasons()).isEmpty();

            loggerExtension.containsLog(Level.INFO, "Calculating accessibility for key=value");
            loggerExtension.containsLog(Level.DEBUG, "Accessibility calculation done. It took: 123 ms");
            loggerExtension.containsLog(
                    Level.ERROR, "Could not find a snap point for end location (%s, %s).".formatted(
                            accessibilityRequest.endLocationLatitude(),
                            accessibilityRequest.endLocationLongitude()));
        }
    }

    private void mockFromAndDestination(AccessibilityRequest accessibilityRequest) {
        when(locationFactory.mapCoordinate(accessibilityRequest.startLocationLatitude(), accessibilityRequest.startLocationLongitude()))
                .thenReturn(from);
        when(locationFactory.mapCoordinate(accessibilityRequest.endLocationLatitude(), accessibilityRequest.endLocationLongitude()))
                .thenReturn(destination);
    }

    private void mockGraphHopperNetwork(AccessibilityRequest accessibilityRequest) {
        when(graphHopperService.getNetwork(restrictions, from, destination)).thenReturn(graphHopperNetwork);
        when(graphHopperNetwork.getBlockedEdges()).thenReturn(blockedEdges);
        when(graphHopperNetwork.getNetwork()).thenReturn(networkGraphHopper);
        when(graphHopperNetwork.getQueryGraph()).thenReturn(queryGraph);
        when(graphHopperNetwork.getFrom()).thenReturn(fromSnap);
        if (accessibilityRequest.addMissingRoadsSectionsFromNwb()) {
            when(graphHopperNetwork.getNwbVersion()).thenReturn(20);
        }
    }

    private void calculateRoadsSectionsWithAppliedRestrictions(AccessibilityRequest accessibilityRequest) {

        when(networkGraphHopper.createWeighting(
                eq(NetworkConstants.CAR_PROFILE),
                argThat(new PMapArgumentMatcher(new PMap())))
        ).thenReturn(weightingNoRestrictions);
        when(isochroneServiceFactory.createService(graphHopperNetwork)).thenReturn(isochroneService);
        when(isochroneService
                .getIsochroneMatchesByMunicipalityId(
                        argThat(new IsochroneArgumentMatcher(IsochroneArguments.builder()
                                .weighting(new RestrictionWeightingAdapter(weightingNoRestrictions, blockedEdges))
                                .municipalityId(accessibilityRequest.municipalityId())
                                .searchDistanceInMetres(accessibilityRequest.searchRadiusInMeters())
                                .build())),
                        eq(queryGraph),
                        eq(fromSnap)))
                .thenReturn(List.of(isochroneMatchRestriction));
        when(roadSectionMapper.mapToRoadSections(List.of(isochroneMatchRestriction)))
                .thenReturn(new HashSet<>(Set.of(roadSectionRestriction)));
    }

    private void mockCalculateReasons(AccessibilityRequest accessibilityRequest) {
        when(accessibilityReasonService.calculateReasons(accessibilityRequest, graphHopperNetwork)).thenReturn(accessibilityReasons);
    }

    private void mockFindDestinationRoadSection(boolean hasDestination) {
        if (hasDestination) {
            when(graphHopperNetwork.getDestination()).thenReturn(destinationSnap);
            when(destinationSnap.getClosestEdge()).thenReturn(endSegmentClosestEdge);
            when(networkGraphHopper.getEncodingManager()).thenReturn(encodingManager);
            when(encodingManager.getIntEncodedValue(WAY_ID_KEY)).thenReturn(idIntEncodedValue);
            when(endSegmentClosestEdge.get(idIntEncodedValue)).thenReturn(30);
        }
    }

    @Test
    void annotation_calculateAccessibility() {

        AnnotationUtil.methodContainsAnnotation(
                accessibilityService.getClass(),
                Timed.class,
                "calculateAccessibility",
                annotation -> assertThat(annotation.description())
                        .isEqualTo("Time spent calculating accessibility")
        );
    }

    private record PMapArgumentMatcher(PMap expected) implements ArgumentMatcher<PMap> {

        @Override
        public boolean matches(PMap actual) {
            // Mockito initializes with a null value
            if (actual == null) {
                return false;
            }

            return expected.toMap().equals(actual.toMap());
        }
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
