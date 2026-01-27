package nu.ndw.nls.accessibilitymap.accessibility.service;

import static java.time.temporal.ChronoUnit.MILLIS;
import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.PMap;
import io.micrometer.core.annotation.Timed;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.Accessibility;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.IsochroneArguments;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.factory.IsochroneServiceFactory;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service.IsochroneService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service.NetworkCacheDataService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.weighting.RestrictionWeightingAdapter;
import nu.ndw.nls.accessibilitymap.accessibility.reason.mapper.PointMapper;
import nu.ndw.nls.accessibilitymap.accessibility.reason.mapper.RoadSectionMapper;
import nu.ndw.nls.accessibilitymap.accessibility.reason.service.AccessibilityReasonService;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.TrafficSignDataService;
import nu.ndw.nls.routingmapmatcher.model.singlepoint.SinglePointMatch.CandidateMatch;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import nu.ndw.nls.springboot.core.time.ClockService;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccessibilityService {

    private final PointMatchService pointMatchService;

    private final PointMapper pointMapper;

    private final IsochroneServiceFactory isochroneServiceFactory;

    private final TrafficSignDataService trafficSignDataService;

    private final RoadSectionMapper roadSectionMapper;

    private final ClockService clockService;

    private final NetworkCacheDataService networkCacheDataService;

    private final RoadSectionCombinator roadSectionCombinator;

    private final MissingRoadSectionProvider missingRoadSectionProvider;

    private final AccessibilityReasonService accessibilityReasonService;

    @Timed(description = "Time spent calculating accessibility")
    public Accessibility calculateAccessibility(
            NetworkGraphHopper networkGraphHopper,
            AccessibilityRequest accessibilityRequest) throws AccessibilityException {

        Optional<Snap> from = findSnap(
                networkGraphHopper,
                accessibilityRequest.startLocationLatitude(),
                accessibilityRequest.startLocationLongitude());

        if (from.isEmpty()) {
            throw new AccessibilityException("Could not find a snap point for start location (%s, %s).".formatted(
                    accessibilityRequest.startLocationLatitude(),
                    accessibilityRequest.startLocationLongitude()
            ));
        }

        List<TrafficSign> trafficSigns = trafficSignDataService.findAllBy(accessibilityRequest);
        NetworkData networkData = networkCacheDataService.getNetworkData(
                accessibilityRequest.municipalityId(),
                from.get(),
                accessibilityRequest.searchRadiusInMeters(),
                trafficSigns,
                networkGraphHopper);

        IsochroneService isochroneService = isochroneServiceFactory.createService(networkGraphHopper);

        OffsetDateTime startTimeCalculatingAccessibility = clockService.now();
        Collection<RoadSection> accessibleRoadsSectionsWithoutAppliedRestrictions = networkData.baseAccessibleRoads();

        Collection<RoadSection> accessibleRoadSectionsWithAppliedRestrictions = getRoadSections(
                accessibilityRequest,
                isochroneService,
                networkData.queryGraph(),
                from.get(),
                buildWeightingWithRestrictions(networkGraphHopper, networkData.edgeRestrictions().getBlockedEdges()));

        if (accessibilityRequest.addMissingRoadsSectionsFromNwb()) {
            Collection<RoadSection> missingRoadSections = missingRoadSectionProvider.get(
                    accessibilityRequest.municipalityId(),
                    accessibleRoadsSectionsWithoutAppliedRestrictions,
                    false);

            accessibleRoadsSectionsWithoutAppliedRestrictions.addAll(missingRoadSections);
            accessibleRoadSectionsWithAppliedRestrictions.addAll(missingRoadSections);
        }

        Accessibility accessibility = buildAccessibility(
                accessibilityRequest,
                accessibleRoadsSectionsWithoutAppliedRestrictions,
                accessibleRoadSectionsWithAppliedRestrictions,
                networkData,
                trafficSigns);

        log.info("Accessibility calculation done. It took: {} ms", MILLIS.between(startTimeCalculatingAccessibility, clockService.now()));
        return accessibility;
    }

    private Accessibility buildAccessibility(
            AccessibilityRequest accessibilityRequest,
            Collection<RoadSection> accessibleRoadsSectionsWithoutAppliedRestrictions,
            Collection<RoadSection> accessibleRoadSectionsWithAppliedRestrictions,
            NetworkData networkData,
            List<TrafficSign> trafficSigns) {

        Collection<RoadSection> combinedRestrictions = roadSectionCombinator.combineNoRestrictionsWithAccessibilityRestrictions(
                accessibleRoadsSectionsWithoutAppliedRestrictions,
                accessibleRoadSectionsWithAppliedRestrictions);

        Optional<RoadSection> toRoadSection = findDestinationRoadSection(
                accessibilityRequest,
                networkData.networkGraphHopper(),
                combinedRestrictions);

        List<List<AccessibilityReason>> reasons = calculateReasons(accessibilityRequest, networkData, toRoadSection, trafficSigns);

        return Accessibility.builder()
                .accessibleRoadsSectionsWithoutAppliedRestrictions(accessibleRoadsSectionsWithoutAppliedRestrictions)
                .accessibleRoadSectionsWithAppliedRestrictions(accessibleRoadSectionsWithAppliedRestrictions)
                .combinedAccessibility(combinedRestrictions)
                .toRoadSection(toRoadSection)
                .reasons(reasons)
                .build();
    }

    @SuppressWarnings("java:S3553")
    private List<List<AccessibilityReason>> calculateReasons(
            AccessibilityRequest accessibilityRequest,
            NetworkData networkData,
            Optional<RoadSection> toRoadSection,
            List<TrafficSign> trafficSigns) {

        return toRoadSection
                .filter(RoadSection::isRestrictedInAnyDirection)
                .map(roadSection -> accessibilityReasonService.calculateReasons(accessibilityRequest, networkData, trafficSigns))
                .orElse(Collections.emptyList());
    }

    private Optional<Snap> findSnap(NetworkGraphHopper networkGraphHopper, Double latitude, Double longitude) {

        return pointMapper.mapCoordinate(latitude, longitude)
                .flatMap(point -> pointMatchService.match(networkGraphHopper, point)
                        .map(CandidateMatch::getSnappedPoint)
                        .filter(Geometry::isValid)
                        .map(snappedPoint -> networkGraphHopper.getLocationIndex().findClosest(
                                snappedPoint.getY(),
                                snappedPoint.getX(),
                                EdgeFilter.ALL_EDGES)));
    }

    private Optional<RoadSection> findDestinationRoadSection(
            AccessibilityRequest accessibilityRequest,
            NetworkGraphHopper networkGraphHopper,
            Collection<RoadSection> combinedRoadSections) {

        if (!accessibilityRequest.hasEndLocation()) {
            return Optional.empty();
        }

        Optional<Snap> destinationSnap = findSnap(
                networkGraphHopper,
                accessibilityRequest.endLocationLatitude(),
                accessibilityRequest.endLocationLongitude());

        if (destinationSnap.isEmpty()) {
            log.error("Could not find a snap point for end location (%s, %s).".formatted(
                    accessibilityRequest.endLocationLatitude(),
                    accessibilityRequest.endLocationLongitude()
            ));
            return Optional.empty();
        }

        int roadSectionId = destinationSnap.get()
                .getClosestEdge().get(networkGraphHopper.getEncodingManager().getIntEncodedValue(WAY_ID_KEY));
        return combinedRoadSections.stream()
                .filter(roadSection -> roadSection.getId() == roadSectionId)
                .findFirst();
    }

    private Collection<RoadSection> getRoadSections(
            AccessibilityRequest accessibilityRequest,
            IsochroneService isochroneService,
            QueryGraph queryGraph,
            Snap startSegment,
            Weighting weighting) {

        return roadSectionMapper.mapToRoadSections(
                isochroneService.getIsochroneMatchesByMunicipalityId(
                        IsochroneArguments.builder()
                                .weighting(weighting)
                                .municipalityId(accessibilityRequest.municipalityId())
                                .searchDistanceInMetres(accessibilityRequest.searchRadiusInMeters())
                                .build(),
                        queryGraph,
                        startSegment));
    }

    private Weighting buildWeightingWithRestrictions(NetworkGraphHopper networkGraphHopper, Set<Integer> blockedEdges) {

        return new RestrictionWeightingAdapter(networkGraphHopper.createWeighting(NetworkConstants.CAR_PROFILE, new PMap()), blockedEdges);
    }
}
