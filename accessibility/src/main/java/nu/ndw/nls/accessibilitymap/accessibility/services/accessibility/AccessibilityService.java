package nu.ndw.nls.accessibilitymap.accessibility.services.accessibility;

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
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.request.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.time.ClockService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.IsochroneService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.RestrictionWeightingAdapter;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.TrafficSignEdgeRestrictions;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.factory.IsochroneServiceFactory;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.querygraph.QueryGraphConfigurer;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.querygraph.QueryGraphFactory;
import nu.ndw.nls.accessibilitymap.accessibility.model.IsochroneArguments;
import nu.ndw.nls.accessibilitymap.accessibility.services.accessibility.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.accessibility.services.accessibility.dto.TrafficSignSnap;
import nu.ndw.nls.accessibilitymap.accessibility.services.accessibility.mappers.RoadSectionMapper;
import nu.ndw.nls.accessibilitymap.accessibility.services.accessibility.mappers.TrafficSignSnapMapper;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.TrafficSignDataService;
import nu.ndw.nls.accessibilitymap.shared.model.NetworkConstants;
import nu.ndw.nls.accessibilitymap.trafficsignclient.services.TrafficSignService;
import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(TrafficSignService.class)
public class AccessibilityService {

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

    @Timed(description = "Time spent calculating accessibility")
    public Accessibility calculateAccessibility(AccessibilityRequest accessibilityRequest, boolean includeOnlyTimeWindowedSigns) {

        OffsetDateTime startTime = clockService.now();
        List<TrafficSignSnap> snappedTrafficSigns = buildTrafficSignSnaps(accessibilityRequest, includeOnlyTimeWindowedSigns);
        Point startPoint = createPoint(
                accessibilityRequest.startLocationLatitude(),
                accessibilityRequest.startLocationLongitude());
        Snap startSegment = networkGraphHopper.getLocationIndex()
                .findClosest(startPoint.getY(), startPoint.getX(), EdgeFilter.ALL_EDGES);
        QueryGraph queryGraph = queryGraphFactory.createQueryGraphWithoutConfig(snappedTrafficSigns, startSegment);

        TrafficSignEdgeRestrictions trafficSignEdgeRestrictions = queryGraphConfigurer.createEdgeRestrictions(queryGraph,
                snappedTrafficSigns);

        Map<Integer, TrafficSign> trafficSignsById = buildTrafficSignById(snappedTrafficSigns);

        IsochroneService isochroneService = isochroneServiceFactory.createService(networkGraphHopper);

        Collection<RoadSection> accessibleRoadsSectionsWithoutAppliedRestrictions =
                getRoadSections(
                        accessibilityRequest,
                        isochroneService,
                        startPoint,
                        queryGraph,
                        startSegment,
                        trafficSignsById,
                        buildWeightingWithRestrictions(TrafficSignEdgeRestrictions.emptyRestrictions()),
                        trafficSignEdgeRestrictions);

        Collection<RoadSection> accessibleRoadSectionsWithAppliedRestrictions =
                getRoadSections(
                        accessibilityRequest,
                        isochroneService,
                        startPoint,
                        queryGraph,
                        startSegment,
                        trafficSignsById,
                        buildWeightingWithRestrictions(trafficSignEdgeRestrictions),
                        trafficSignEdgeRestrictions);

        Accessibility accessibility = Accessibility.builder()
                .accessibleRoadsSectionsWithoutAppliedRestrictions(accessibleRoadsSectionsWithoutAppliedRestrictions)
                .accessibleRoadSectionsWithAppliedRestrictions(accessibleRoadSectionsWithAppliedRestrictions)
                .combinedAccessibility(
                        roadSectionCombinator.combineNoRestrictionsWithAccessibilityRestrictions(
                                accessibleRoadsSectionsWithoutAppliedRestrictions,
                                accessibleRoadSectionsWithAppliedRestrictions))
                .build();

        log.debug("Accessibility generation done. It took: %s ms"
                .formatted(ChronoUnit.MILLIS.between(startTime, clockService.now())));
        return accessibility;
    }

    private Collection<RoadSection> getRoadSections(
            AccessibilityRequest accessibilityRequest,
            IsochroneService isochroneService,
            Point startPoint,
            QueryGraph queryGraph,
            Snap startSegment,
            Map<Integer, TrafficSign> trafficSignsById,
            Weighting weighting,
            TrafficSignEdgeRestrictions trafficSignEdgeRestrictions) {

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
                trafficSignsById,
                trafficSignEdgeRestrictions);
    }

    private static Map<Integer, TrafficSign> buildTrafficSignById(List<TrafficSignSnap> additionalSnaps) {

        return additionalSnaps.stream()
                .collect(Collectors.toMap(
                        additionalSnap -> additionalSnap.getTrafficSign().id(),
                        TrafficSignSnap::getTrafficSign));
    }

    private List<TrafficSignSnap> buildTrafficSignSnaps(AccessibilityRequest accessibilityRequest, boolean includeOnlyTimeWindowedSigns) {

        List<TrafficSign> trafficSigns = trafficSignDataService.findAllBy(accessibilityRequest);
        return trafficSingSnapMapper.map(trafficSigns, includeOnlyTimeWindowedSigns);
    }

    private Weighting buildWeightingWithRestrictions(TrafficSignEdgeRestrictions trafficSignEdgeRestrictions) {
        Profile profile = networkGraphHopper.getProfile(NetworkConstants.VEHICLE_NAME_CAR);
        return new RestrictionWeightingAdapter(networkGraphHopper.createWeighting(profile, new PMap()), trafficSignEdgeRestrictions);
    }

    private Point createPoint(double latitude, double longitude) {

        return geometryFactoryWgs84.createPoint(new Coordinate(longitude, latitude));
    }

}
