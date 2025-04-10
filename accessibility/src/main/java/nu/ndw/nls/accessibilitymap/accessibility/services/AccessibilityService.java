package nu.ndw.nls.accessibilitymap.accessibility.services;

import com.graphhopper.config.Profile;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.PMap;
import io.micrometer.core.annotation.Timed;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.IsochroneArguments;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.factory.IsochroneServiceFactory;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.querygraph.QueryGraphConfigurer;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.querygraph.QueryGraphFactory;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service.IsochroneService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.weighting.RestrictionWeightingAdapter;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.TrafficSignSnap;
import nu.ndw.nls.accessibilitymap.accessibility.services.mappers.RoadSectionMapper;
import nu.ndw.nls.accessibilitymap.accessibility.services.mappers.TrafficSignSnapMapper;
import nu.ndw.nls.accessibilitymap.accessibility.time.ClockService;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.TrafficSignDataService;
import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccessibilityService {

    private static final AccessibleRoadSectionModifier NO_MODIFICATIONS =  (a, b) -> {};

    private final IsochroneServiceFactory isochroneServiceFactory;

    private final NetworkGraphHopper networkGraphHopper;

    private final TrafficSignDataService trafficSignDataService;

    private final GeometryFactoryWgs84 geometryFactoryWgs84;

    private final RoadSectionMapper roadSectionMapper;

    private final RoadSectionCombinator roadSectionCombinator;

    private final ClockService clockService;

    private final TrafficSignSnapMapper trafficSingSnapMapper;

    private final QueryGraphFactory queryGraphFactory;

    private final QueryGraphConfigurer queryGraphConfigurer;

    public Accessibility calculateAccessibility(AccessibilityRequest accessibilityRequest) {

        return calculateAccessibility(accessibilityRequest, NO_MODIFICATIONS);
    }

    @Timed(description = "Time spent calculating accessibility")
    public Accessibility calculateAccessibility(
            AccessibilityRequest accessibilityRequest,
            AccessibleRoadSectionModifier accessibleRoadSectionModifier) {

        OffsetDateTime startTime = clockService.now();
        List<TrafficSignSnap> snappedTrafficSigns = buildTrafficSignSnaps(accessibilityRequest);
        log.info("Building snaps took: %s ms"
                .formatted(ChronoUnit.MILLIS.between(startTime, clockService.now())));
        Point startPoint = createPoint(
                accessibilityRequest.startLocationLatitude(),
                accessibilityRequest.startLocationLongitude());
        Snap startSegment = networkGraphHopper.getLocationIndex()
                .findClosest(startPoint.getY(), startPoint.getX(), EdgeFilter.ALL_EDGES);
        OffsetDateTime startTime2 = clockService.now();
        QueryGraph queryGraph = queryGraphFactory.createQueryGraph(snappedTrafficSigns, startSegment);
        log.info("Building query graph took: %s ms"
                .formatted(ChronoUnit.MILLIS.between(startTime2, clockService.now())));
        OffsetDateTime startTime3 = clockService.now();
        var edgeRestrictions = queryGraphConfigurer.createEdgeRestrictions(queryGraph, snappedTrafficSigns);
        log.info("Building edge restrictions took: %s ms"
                .formatted(ChronoUnit.MILLIS.between(startTime3, clockService.now())));
        IsochroneService isochroneService = isochroneServiceFactory.createService(networkGraphHopper);

        OffsetDateTime startTime4 = clockService.now();
        Collection<RoadSection> accessibleRoadsSectionsWithoutAppliedRestrictions =
                getRoadSections(
                        accessibilityRequest,
                        isochroneService,
                        startPoint,
                        queryGraph,
                        startSegment,
                        buildWeightingWithRestrictions(Set.of()),
                        edgeRestrictions.getTrafficSignsByEdgeKey());

        Collection<RoadSection> accessibleRoadSectionsWithAppliedRestrictions =
                getRoadSections(
                        accessibilityRequest,
                        isochroneService,
                        startPoint,
                        queryGraph,
                        startSegment,
                        buildWeightingWithRestrictions(edgeRestrictions.getBlockedEdges()),
                        edgeRestrictions.getTrafficSignsByEdgeKey());

        accessibleRoadSectionModifier.modify(
                accessibleRoadsSectionsWithoutAppliedRestrictions,
                accessibleRoadSectionsWithAppliedRestrictions);

        Accessibility accessibility = Accessibility.builder()
                .accessibleRoadsSectionsWithoutAppliedRestrictions(accessibleRoadsSectionsWithoutAppliedRestrictions)
                .accessibleRoadSectionsWithAppliedRestrictions(accessibleRoadSectionsWithAppliedRestrictions)
                .combinedAccessibility(
                        roadSectionCombinator.combineNoRestrictionsWithAccessibilityRestrictions(
                                accessibleRoadsSectionsWithoutAppliedRestrictions,
                                accessibleRoadSectionsWithAppliedRestrictions))
                .build();

        log.info("Accessibility calculation done. It took: %s ms"
                .formatted(ChronoUnit.MILLIS.between(startTime4, clockService.now())));
        return accessibility;
    }

    private Collection<RoadSection> getRoadSections(
            AccessibilityRequest accessibilityRequest,
            IsochroneService isochroneService,
            Point startPoint,
            QueryGraph queryGraph,
            Snap startSegment,
            Weighting weighting,
            Map<Integer, List<TrafficSign>> trafficSignsByEdgeKey) {

        return roadSectionMapper.mapToRoadSections(
                isochroneService.getIsochroneMatchesByMunicipalityId(
                        IsochroneArguments.builder()
                                .weighting(weighting)
                                .startPoint(startPoint)
                                .municipalityId(accessibilityRequest.municipalityId())
                                .searchDistanceInMetres(accessibilityRequest.searchRadiusInMeters())
                                .build(),
                        queryGraph,
                        startSegment),
                trafficSignsByEdgeKey);
    }

    private List<TrafficSignSnap> buildTrafficSignSnaps(AccessibilityRequest accessibilityRequest) {

        List<TrafficSign> trafficSigns = trafficSignDataService.findAllBy(accessibilityRequest);
        return trafficSingSnapMapper.map(trafficSigns);
    }

    private Weighting buildWeightingWithRestrictions(Set<Integer> blockedEdges) {
        Profile profile = networkGraphHopper.getProfile(NetworkConstants.VEHICLE_NAME_CAR);
        return new RestrictionWeightingAdapter(networkGraphHopper.createWeighting(profile, new PMap()), blockedEdges);
    }

    private Point createPoint(double latitude, double longitude) {

        return geometryFactoryWgs84.createPoint(new Coordinate(longitude, latitude));
    }

}
