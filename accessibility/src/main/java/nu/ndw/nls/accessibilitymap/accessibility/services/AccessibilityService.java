package nu.ndw.nls.accessibilitymap.accessibility.services;

import static java.time.temporal.ChronoUnit.MILLIS;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.PMap;
import io.micrometer.core.annotation.Timed;
import java.util.Collection;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.IsochroneArguments;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.factory.IsochroneServiceFactory;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service.IsochroneService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service.NetworkCacheDataService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.weighting.RestrictionWeightingAdapter;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.services.mappers.RoadSectionMapper;
import nu.ndw.nls.accessibilitymap.accessibility.time.ClockService;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.TrafficSignDataService;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccessibilityService {

    private final IsochroneServiceFactory isochroneServiceFactory;

    private final TrafficSignDataService trafficSignDataService;

    private final RoadSectionMapper roadSectionMapper;

    private final ClockService clockService;

    private final NetworkCacheDataService networkCacheDataService;

    private final RoadSectionCombinator roadSectionCombinator;

    private final MissingRoadSectionProvider missingRoadSectionProvider;

    @Timed(description = "Time spent calculating accessibility")
    public Accessibility calculateAccessibility(
            NetworkGraphHopper networkGraphHopper,
            AccessibilityRequest accessibilityRequest) {

        var startSegment = networkGraphHopper.getLocationIndex()
                .findClosest(
                        accessibilityRequest.startLocationLatitude(),
                        accessibilityRequest.startLocationLongitude(),
                        EdgeFilter.ALL_EDGES);
        var trafficSigns = trafficSignDataService.findAllBy(accessibilityRequest);
        var networkData = networkCacheDataService.getNetworkData(
                accessibilityRequest.municipalityId(),
                startSegment,
                accessibilityRequest.searchRadiusInMeters(),
                trafficSigns,
                networkGraphHopper);

        var isochroneService = isochroneServiceFactory.createService(networkGraphHopper);

        var startTimeCalculatingAccessibility = clockService.now();
        var accessibleRoadsSectionsWithoutAppliedRestrictions = networkData.baseAccessibleRoads();

        var accessibleRoadSectionsWithAppliedRestrictions =
                getRoadSections(
                        accessibilityRequest,
                        isochroneService,
                        networkData.queryGraph(),
                        startSegment,
                        buildWeightingWithRestrictions(networkGraphHopper, networkData.edgeRestrictions().getBlockedEdges()));

        if (accessibilityRequest.addMissingRoadsSectionsFromNwb()) {
            var missingRoadSections = missingRoadSectionProvider.get(
                    accessibilityRequest.municipalityId(),
                    accessibleRoadsSectionsWithoutAppliedRestrictions,
                    false);

            accessibleRoadsSectionsWithoutAppliedRestrictions.addAll(missingRoadSections);
            accessibleRoadSectionsWithAppliedRestrictions.addAll(missingRoadSections);
        }

        var accessibility = Accessibility.builder()
                .accessibleRoadsSectionsWithoutAppliedRestrictions(accessibleRoadsSectionsWithoutAppliedRestrictions)
                .accessibleRoadSectionsWithAppliedRestrictions(accessibleRoadSectionsWithAppliedRestrictions)
                .combinedAccessibility(roadSectionCombinator.combineNoRestrictionsWithAccessibilityRestrictions(
                        accessibleRoadsSectionsWithoutAppliedRestrictions, accessibleRoadSectionsWithAppliedRestrictions))
                .build();

        log.info("Accessibility calculation done. It took: %s ms"
                .formatted(MILLIS.between(startTimeCalculatingAccessibility, clockService.now())));
        return accessibility;
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
