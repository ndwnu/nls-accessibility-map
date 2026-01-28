package nu.ndw.nls.accessibilitymap.accessibility.service;

import static java.time.temporal.ChronoUnit.MILLIS;
import static nu.ndw.nls.accessibilitymap.accessibility.core.log.LogConstants.ACCESSIBILITY_REQUEST;
import static nu.ndw.nls.accessibilitymap.accessibility.core.log.LogUtil.keyValueJson;
import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;

import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.PMap;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.Accessibility;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restrictions;
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
import nu.ndw.nls.springboot.core.time.ClockService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccessibilityService {

    private final LocationFactory locationFactory;

    private final IsochroneServiceFactory isochroneServiceFactory;

    private final RestrictionService restrictionService;

    private final RoadSectionMapper roadSectionMapper;

    private final ClockService clockService;

    private final BaseAccessibilityCalculator baseAccessibilityCalculator;

    private final RoadSectionCombinator roadSectionCombinator;

    private final MissingRoadSectionProvider missingRoadSectionProvider;

    private final AccessibilityReasonService accessibilityReasonService;

    private final GraphHopperService graphHopperService;

    @Timed(description = "Time spent calculating accessibility")
    public Accessibility calculateAccessibility(@Valid AccessibilityRequest accessibilityRequest) throws AccessibilityException {

        log.info("Calculating accessibility for {}", keyValueJson(ACCESSIBILITY_REQUEST, accessibilityRequest));
        Restrictions restrictions = restrictionService.findAllBy(accessibilityRequest);

        GraphHopperNetwork graphHopperNetwork = graphHopperService.getNetwork(
                restrictions,
                locationFactory.mapCoordinate(accessibilityRequest.startLocationLatitude(), accessibilityRequest.startLocationLongitude()),
                locationFactory.mapCoordinate(accessibilityRequest.endLocationLatitude(), accessibilityRequest.endLocationLongitude()));

        OffsetDateTime startTimeCalculatingAccessibility = clockService.now();

        Collection<RoadSection> accessibleRoadsSectionsWithoutAppliedRestrictions = baseAccessibilityCalculator.calculate(
                graphHopperNetwork,
                accessibilityRequest.municipalityId(),
                accessibilityRequest.searchRadiusInMeters());

        Collection<RoadSection> accessibleRoadsSectionsWithAppliedRestrictions = calculateRoadsSectionsWithAppliedRestrictions(
                accessibilityRequest,
                graphHopperNetwork);

        Collection<RoadSection> unroutableRoadSections = new ArrayList<>();
        if (accessibilityRequest.addMissingRoadsSectionsFromNwb()) {
            unroutableRoadSections.addAll(missingRoadSectionProvider.get(
                    graphHopperNetwork.getNwbVersion(),
                    accessibilityRequest.municipalityId(),
                    accessibleRoadsSectionsWithoutAppliedRestrictions,
                    false));
        }

        Accessibility accessibility = buildAccessibility(
                accessibilityRequest,
                accessibleRoadsSectionsWithoutAppliedRestrictions,
                accessibleRoadsSectionsWithAppliedRestrictions,
                unroutableRoadSections,
                graphHopperNetwork);

        log.debug("Accessibility calculation done. It took: {} ms", MILLIS.between(startTimeCalculatingAccessibility, clockService.now()));
        return accessibility;
    }

    private Accessibility buildAccessibility(
            AccessibilityRequest accessibilityRequest,
            Collection<RoadSection> accessibleRoadsSectionsWithoutAppliedRestrictions,
            Collection<RoadSection> accessibleRoadSectionsWithAppliedRestrictions,
            Collection<RoadSection> unroutableRoadSections,
            GraphHopperNetwork graphHopperNetwork) {

        accessibleRoadsSectionsWithoutAppliedRestrictions.addAll(unroutableRoadSections);
        accessibleRoadSectionsWithAppliedRestrictions.addAll(unroutableRoadSections);

        Collection<RoadSection> combinedRestrictions = roadSectionCombinator.combineNoRestrictionsWithAccessibilityRestrictions(
                accessibleRoadsSectionsWithoutAppliedRestrictions,
                accessibleRoadSectionsWithAppliedRestrictions);

        Optional<RoadSection> toRoadSection = findDestinationRoadSection(
                accessibilityRequest,
                graphHopperNetwork,
                combinedRestrictions);

        List<List<AccessibilityReason>> reasons = calculateReasons(accessibilityRequest, toRoadSection, graphHopperNetwork);

        return Accessibility.builder()
                .accessibleRoadsSectionsWithoutAppliedRestrictions(accessibleRoadsSectionsWithoutAppliedRestrictions)
                .accessibleRoadSectionsWithAppliedRestrictions(accessibleRoadSectionsWithAppliedRestrictions)
                .combinedAccessibility(combinedRestrictions)
                .unroutableRoadSections(unroutableRoadSections)
                .toRoadSection(toRoadSection)
                .reasons(reasons)
                .build();
    }

    @SuppressWarnings("java:S3553")
    private List<List<AccessibilityReason>> calculateReasons(
            AccessibilityRequest accessibilityRequest,
            Optional<RoadSection> toRoadSection,
            GraphHopperNetwork graphHopperNetwork) {

        return toRoadSection
                .filter(RoadSection::isRestrictedInAnyDirection)
                .map(roadSection -> accessibilityReasonService.calculateReasons(accessibilityRequest, graphHopperNetwork))
                .orElse(Collections.emptyList());
    }

    private Optional<RoadSection> findDestinationRoadSection(
            AccessibilityRequest accessibilityRequest,
            GraphHopperNetwork graphHopperNetwork,
            Collection<RoadSection> combinedRoadSections) {

        if (!accessibilityRequest.hasEndLocation()) {
            return Optional.empty();
        }

        Optional<Snap> destinationSnap = Optional.ofNullable(graphHopperNetwork.getDestination());

        if (destinationSnap.isEmpty()) {
            log.error("Could not find a snap point for end location (%s, %s).".formatted(
                    accessibilityRequest.endLocationLatitude(),
                    accessibilityRequest.endLocationLongitude()
            ));
            return Optional.empty();
        }

        int roadSectionId = destinationSnap.get()
                .getClosestEdge().get(graphHopperNetwork.getNetwork().getEncodingManager().getIntEncodedValue(WAY_ID_KEY));
        return combinedRoadSections.stream()
                .filter(roadSection -> roadSection.getId() == roadSectionId)
                .findFirst();
    }

    private Collection<RoadSection> calculateRoadsSectionsWithAppliedRestrictions(
            AccessibilityRequest accessibilityRequest,
            GraphHopperNetwork graphHopperNetwork) {

        RestrictionWeightingAdapter weighting = new RestrictionWeightingAdapter(
                graphHopperNetwork.getNetwork().createWeighting(NetworkConstants.CAR_PROFILE, new PMap()),
                graphHopperNetwork.getBlockedEdges());

        IsochroneService isochroneService = isochroneServiceFactory.createService(graphHopperNetwork);

        return roadSectionMapper.mapToRoadSections(
                isochroneService.getIsochroneMatchesByMunicipalityId(
                        IsochroneArguments.builder()
                                .weighting(weighting)
                                .municipalityId(accessibilityRequest.municipalityId())
                                .searchDistanceInMetres(accessibilityRequest.searchRadiusInMeters())
                                .build(),
                        graphHopperNetwork.getQueryGraph(),
                        graphHopperNetwork.getFrom()));
    }
}

