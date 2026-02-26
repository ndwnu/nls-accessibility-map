package nu.ndw.nls.accessibilitymap.accessibility.service;

import static net.logstash.logback.argument.StructuredArguments.keyValue;
import static nu.ndw.nls.accessibilitymap.accessibility.core.log.LogConstants.ACCESSIBILITY_REQUEST;
import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.EdgeIteratorState;
import io.micrometer.core.annotation.Timed;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Location;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.Accessibility;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.core.log.LogUtil;
import nu.ndw.nls.accessibilitymap.accessibility.core.util.LocationFactory;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.GraphHopperNetwork;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReasonGroup;
import nu.ndw.nls.accessibilitymap.accessibility.reason.service.AccessibilityReasonService;
import nu.ndw.nls.accessibilitymap.accessibility.restriction.RestrictionService;
import nu.ndw.nls.accessibilitymap.accessibility.service.debug.AccessibilityDebugger;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityNetwork;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import nu.ndw.nls.springboot.core.time.ClockService;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityServiceTest {

    private AccessibilityService accessibilityService;

    @Mock
    private LocationFactory locationFactory;

    @Mock
    private NetworkGraphHopper networkGraphHopper;

    @Mock
    private GraphHopperNetwork graphHopperNetwork;

    @Mock
    private EncodingManager encodingManager;

    @Mock
    private IntEncodedValue idIntEncodedValue;

    @Mock
    private AccessibilityDebugger accessibilityDebugger;

    @Mock
    private RoadSectionCombinator roadSectionCombinator;

    @Mock
    private ClockService clockService;

    @Mock
    private EdgeIteratorState endSegmentClosestEdge;

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
    private RestrictionService restrictionService;

    @Mock
    private AccessibilityNetworkProvider accessibilityNetworkProvider;

    @Mock
    private AccessibilityCalculator accessibilityCalculator;

    @Mock
    private MissingRoadSectionProvider missingRoadSectionProvider;

    @Mock
    private AccessibilityReasonService accessibilityReasonService;

    @Mock
    private Location from;

    @Mock
    private Location destination;

    @Mock
    private Restrictions restrictions;

    @Mock
    private Snap destinationSnap;

    @Mock
    private AccessibilityNetwork accessibilityNetwork;

    @Mock
    private NetworkData networkData;

    @Mock
    private DirectionalSegment directionalSegmentRoadSectionDestination;

    @Mock
    private DirectionalSegment directionalSegmentRoadSectionCombined;

    @Mock
    private List<AccessibilityReasonGroup> accessibilityReasonGroups;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    private AccessibilityRequest accessibilityRequest;

    @BeforeEach
    void setUp() {

        accessibilityService = new AccessibilityService(
                locationFactory,
                restrictionService,
                clockService,
                accessibilityCalculator,
                roadSectionCombinator,
                missingRoadSectionProvider,
                accessibilityReasonService,
                accessibilityNetworkProvider,
                accessibilityDebugger);

        accessibilityRequest = AccessibilityRequest.builder()
                .startLocationLatitude(1.0)
                .startLocationLongitude(2.0)
                .endLocationLatitude(3.0)
                .endLocationLongitude(4.0)
                .municipalityId(5)
                .maxSearchDistanceInMeters(6.0)
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
        when(networkData.getGraphHopperNetwork()).thenReturn(graphHopperNetwork);
        when(graphHopperNetwork.network()).thenReturn(networkGraphHopper);

        when(accessibilityNetworkProvider.get(networkData, restrictions, from, destination)).thenReturn(accessibilityNetwork);
        when(accessibilityNetwork.getNetworkData()).thenReturn(networkData);

        when(accessibilityCalculator.calculateWithoutRestrictions(accessibilityRequest, accessibilityNetwork))
                .thenReturn(new HashSet<>(Set.of(roadSectionNoRestriction)));

        when(accessibilityCalculator.calculateWithRestrictions(accessibilityRequest, accessibilityNetwork))
                .thenReturn(new HashSet<>(Set.of(roadSectionRestriction)));

        when(missingRoadSectionProvider.findAll(
                networkData,
                accessibilityRequest.municipalityId(),
                Set.of(roadSectionNoRestriction),
                false,
                accessibilityRequest.requestArea()))
                .thenReturn(List.of(missingRoadSection));

        when(roadSectionCombinator.combineNoRestrictionsWithAccessibilityRestrictions(
                Set.of(roadSectionNoRestriction, missingRoadSection),
                Set.of(roadSectionRestriction, missingRoadSection)))
                .thenReturn(Set.of(roadSectionCombined, roadSectionDestination));

        mockFindDestinationRoadSection(true);
        when(roadSectionDestination.getId()).thenReturn(30L);

        mockCalculateReasons();

        try (var logUtilStaticMock = Mockito.mockStatic(LogUtil.class)) {
            logUtilStaticMock.when(() -> LogUtil.keyValueJson(ACCESSIBILITY_REQUEST, accessibilityRequest))
                    .thenReturn(keyValue("key", "value"));

            Accessibility accessibility = accessibilityService.calculateAccessibility(networkData, accessibilityRequest);

            assertThat(accessibility.combinedAccessibility()).containsExactlyInAnyOrder(roadSectionCombined, roadSectionDestination);
            assertThat(accessibility.accessibleRoadsSectionsWithoutAppliedRestrictions()).containsExactlyInAnyOrder(
                    roadSectionNoRestriction,
                    missingRoadSection);
            assertThat(accessibility.accessibleRoadSectionsWithAppliedRestrictions()).containsExactlyInAnyOrder(
                    roadSectionRestriction,
                    missingRoadSection);
            assertThat(accessibility.unroutableRoadSections()).containsExactlyInAnyOrder(missingRoadSection);
            assertThat(accessibility.toRoadSection()).contains(roadSectionDestination);
            assertThat(accessibility.reasons()).isEqualTo(accessibilityReasonGroups);

            loggerExtension.containsLog(Level.INFO, "Calculating accessibility for key=value");
            loggerExtension.containsLog(Level.DEBUG, "Adding missing road sections");
            loggerExtension.containsLog(Level.DEBUG, "Added 1 missing road sections");
            loggerExtension.containsLog(Level.DEBUG, "Accessibility calculation done. It took: 123 ms");

            verify(accessibilityDebugger).writeDebug(accessibilityRequest);
            verify(accessibilityDebugger).writeDebug(restrictions);
            verify(accessibilityDebugger).writeDebug(accessibility);
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
        when(networkData.getGraphHopperNetwork()).thenReturn(graphHopperNetwork);
        when(graphHopperNetwork.network()).thenReturn(networkGraphHopper);
        when(accessibilityNetworkProvider.get(networkData, restrictions, from, destination)).thenReturn(accessibilityNetwork);
        when(accessibilityNetwork.getNetworkData()).thenReturn(networkData);

        when(accessibilityCalculator.calculateWithoutRestrictions(accessibilityRequest, accessibilityNetwork))
                .thenReturn(new HashSet<>(Set.of(roadSectionNoRestriction)));

        when(accessibilityCalculator.calculateWithRestrictions(accessibilityRequest, accessibilityNetwork))
                .thenReturn(new HashSet<>(Set.of(roadSectionRestriction)));

        when(roadSectionCombinator.combineNoRestrictionsWithAccessibilityRestrictions(
                Set.of(roadSectionNoRestriction),
                Set.of(roadSectionRestriction)))
                .thenReturn(Set.of(roadSectionCombined, roadSectionDestination));

        mockFindDestinationRoadSection(true);
        when(roadSectionDestination.getId()).thenReturn(30L);

        mockCalculateReasons();

        try (var logUtilStaticMock = Mockito.mockStatic(LogUtil.class)) {
            logUtilStaticMock.when(() -> LogUtil.keyValueJson(ACCESSIBILITY_REQUEST, accessibilityRequest))
                    .thenReturn(keyValue("key", "value"));

            Accessibility accessibility = accessibilityService.calculateAccessibility(networkData, accessibilityRequest);

            assertThat(accessibility.combinedAccessibility()).containsExactlyInAnyOrder(roadSectionCombined, roadSectionDestination);
            assertThat(accessibility.accessibleRoadsSectionsWithoutAppliedRestrictions()).containsExactlyInAnyOrder(roadSectionNoRestriction);
            assertThat(accessibility.accessibleRoadSectionsWithAppliedRestrictions()).containsExactlyInAnyOrder(roadSectionRestriction);
            assertThat(accessibility.unroutableRoadSections()).isEmpty();
            assertThat(accessibility.toRoadSection()).contains(roadSectionDestination);
            assertThat(accessibility.reasons()).isEqualTo(accessibilityReasonGroups);

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
        when(accessibilityNetworkProvider.get(networkData, restrictions, from, destination)).thenReturn(accessibilityNetwork);

        when(accessibilityCalculator.calculateWithoutRestrictions(accessibilityRequest, accessibilityNetwork))
                .thenReturn(new HashSet<>(Set.of(roadSectionNoRestriction)));

        when(accessibilityCalculator.calculateWithRestrictions(accessibilityRequest, accessibilityNetwork))
                .thenReturn(new HashSet<>(Set.of(roadSectionRestriction)));

        when(missingRoadSectionProvider.findAll(
                networkData,
                accessibilityRequest.municipalityId(),
                Set.of(roadSectionNoRestriction),
                false,
                accessibilityRequest.requestArea()))
                .thenReturn(List.of(missingRoadSection));

        when(roadSectionCombinator.combineNoRestrictionsWithAccessibilityRestrictions(
                Set.of(roadSectionNoRestriction, missingRoadSection),
                Set.of(roadSectionRestriction, missingRoadSection)))
                .thenReturn(Set.of(roadSectionCombined, roadSectionDestination));

        mockFindDestinationRoadSection(false);

        try (var logUtilStaticMock = Mockito.mockStatic(LogUtil.class)) {
            logUtilStaticMock.when(() -> LogUtil.keyValueJson(ACCESSIBILITY_REQUEST, accessibilityRequest))
                    .thenReturn(keyValue("key", "value"));

            Accessibility accessibility = accessibilityService.calculateAccessibility(networkData, accessibilityRequest);

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

    @Test
    void calculateAccessibility_noRequestedDestination() {

        accessibilityRequest = accessibilityRequest.withEndLocationLatitude(null)
                .withEndLocationLongitude(null);
        when(clockService.now())
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:00:00.000-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .thenReturn(OffsetDateTime.parse("2022-03-11T09:00:00.123-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        when(restrictionService.findAllBy(accessibilityRequest)).thenReturn(restrictions);

        mockFromAndDestination(accessibilityRequest);
        when(accessibilityNetworkProvider.get(networkData, restrictions, from, destination)).thenReturn(accessibilityNetwork);

        when(accessibilityCalculator.calculateWithoutRestrictions(accessibilityRequest, accessibilityNetwork))
                .thenReturn(new HashSet<>(Set.of(roadSectionNoRestriction)));

        when(accessibilityCalculator.calculateWithRestrictions(accessibilityRequest, accessibilityNetwork))
                .thenReturn(new HashSet<>(Set.of(roadSectionRestriction)));

        when(missingRoadSectionProvider.findAll(
                networkData,
                accessibilityRequest.municipalityId(),
                Set.of(roadSectionNoRestriction),
                false,
                accessibilityRequest.requestArea()))
                .thenReturn(List.of(missingRoadSection));

        when(roadSectionCombinator.combineNoRestrictionsWithAccessibilityRestrictions(
                Set.of(roadSectionNoRestriction, missingRoadSection),
                Set.of(roadSectionRestriction, missingRoadSection)))
                .thenReturn(Set.of(roadSectionCombined, roadSectionDestination));

        mockFindDestinationRoadSection(false);

        try (var logUtilStaticMock = Mockito.mockStatic(LogUtil.class)) {
            logUtilStaticMock.when(() -> LogUtil.keyValueJson(ACCESSIBILITY_REQUEST, accessibilityRequest))
                    .thenReturn(keyValue("key", "value"));

            Accessibility accessibility = accessibilityService.calculateAccessibility(networkData, accessibilityRequest);

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

    private void mockFromAndDestination(AccessibilityRequest accessibilityRequest) {
        when(locationFactory.mapCoordinate(accessibilityRequest.startLocationLatitude(), accessibilityRequest.startLocationLongitude()))
                .thenReturn(from);
        when(locationFactory.mapCoordinate(accessibilityRequest.endLocationLatitude(), accessibilityRequest.endLocationLongitude()))
                .thenReturn(destination);
    }

    private void mockCalculateReasons() {

        when(directionalSegmentRoadSectionDestination.getId()).thenReturn(100);
        when(roadSectionDestination.getRoadSectionFragments()).thenReturn(List.of(RoadSectionFragment.builder()
                .forwardSegment(directionalSegmentRoadSectionDestination)
                .build()));

        when(directionalSegmentRoadSectionCombined.getId()).thenReturn(101);
        when(roadSectionCombined.getRoadSectionFragments()).thenReturn(List.of(RoadSectionFragment.builder()
                .forwardSegment(directionalSegmentRoadSectionCombined)
                .build()));

        when(accessibilityReasonService.calculateReasons(
                Optional.of(roadSectionDestination),
                Map.of(
                        100, directionalSegmentRoadSectionDestination,
                        101, directionalSegmentRoadSectionCombined),
                accessibilityNetwork)
        ).thenReturn(accessibilityReasonGroups);
    }

    private void mockFindDestinationRoadSection(boolean hasDestination) {
        if (hasDestination) {
            when(accessibilityNetwork.getDestination()).thenReturn(destinationSnap);
            when(destinationSnap.getClosestEdge()).thenReturn(endSegmentClosestEdge);
            when(networkGraphHopper.getEncodingManager()).thenReturn(encodingManager);
            when(encodingManager.getIntEncodedValue(WAY_ID_KEY)).thenReturn(idIntEncodedValue);
            when(endSegmentClosestEdge.get(idIntEncodedValue)).thenReturn(30);
        }
    }

    @Test
    void calculateAccessibility_containsTimeAnnotation() {

        AnnotationUtil.methodContainsAnnotation(
                accessibilityService.getClass(),
                Timed.class,
                "calculateAccessibility",
                annotation -> {
                    assertThat(annotation).isNotNull();
                    assertThat(annotation.value()).isEqualTo("accessibilitymap.accessibility.calculate");
                }
        );
    }
}
