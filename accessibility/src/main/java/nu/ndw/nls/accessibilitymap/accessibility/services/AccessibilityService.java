package nu.ndw.nls.accessibilitymap.accessibility.services;

import static java.time.temporal.ChronoUnit.MILLIS;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.PMap;
import io.micrometer.core.annotation.Timed;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.NetworkConstants;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.IsochroneArguments;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.factory.IsochroneServiceFactory;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service.IsochroneService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service.NetworkCacheDataService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.weighting.RestrictionWeightingAdapter;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.services.mappers.RoadSectionMapper;
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

    private static final AccessibleRoadSectionModifier NO_MODIFICATIONS = (a, b) -> {
    };

    private final IsochroneServiceFactory isochroneServiceFactory;

    private final NetworkGraphHopper networkGraphHopper;

    private final TrafficSignDataService trafficSignDataService;

    private final GeometryFactoryWgs84 geometryFactoryWgs84;

    private final RoadSectionMapper roadSectionMapper;

    private final ClockService clockService;

    private final NetworkCacheDataService networkCacheDataService;

    private final RoadSectionCombinator roadSectionCombinator;

    public Accessibility calculateAccessibility(AccessibilityRequest accessibilityRequest) {
        return calculateAccessibility(accessibilityRequest, NO_MODIFICATIONS);
    }

    @Timed(description = "Time spent calculating accessibility")
    public Accessibility calculateAccessibility(
            AccessibilityRequest accessibilityRequest,
            AccessibleRoadSectionModifier accessibleRoadSectionModifier) {

        Point startPoint = createPoint(accessibilityRequest.startLocationLatitude(), accessibilityRequest.startLocationLongitude());
        Snap startSegment = networkGraphHopper.getLocationIndex().findClosest(startPoint.getY(), startPoint.getX(), EdgeFilter.ALL_EDGES);
        List<TrafficSign> trafficSigns = trafficSignDataService.findAllBy(accessibilityRequest);
        NetworkData networkData = networkCacheDataService.getNetworkData(accessibilityRequest.municipalityId(),
                startSegment,
                accessibilityRequest.searchRadiusInMeters(), trafficSigns);

        IsochroneService isochroneService = isochroneServiceFactory.createService(networkGraphHopper);

        OffsetDateTime startTimeCalculatingAccessibility = clockService.now();
        Collection<RoadSection> accessibleRoadsSectionsWithoutAppliedRestrictions = networkData.baseAccessibleRoads();

        Collection<RoadSection> accessibleRoadSectionsWithAppliedRestrictions =
                getRoadSections(
                        accessibilityRequest,
                        isochroneService,
                        networkData.queryGraph(),
                        startSegment,
                        buildWeightingWithRestrictions(networkData.edgeRestrictions().getBlockedEdges()));

        accessibleRoadSectionModifier.modify(
                accessibleRoadsSectionsWithoutAppliedRestrictions,
                accessibleRoadSectionsWithAppliedRestrictions);

        Accessibility accessibility = Accessibility.builder()
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

    private Weighting buildWeightingWithRestrictions(Set<Integer> blockedEdges) {

        return new RestrictionWeightingAdapter(networkGraphHopper.createWeighting(NetworkConstants.CAR_PROFILE, new PMap()), blockedEdges);
    }

    private Point createPoint(double latitude, double longitude) {

        return geometryFactoryWgs84.createPoint(new Coordinate(longitude, latitude));
    }

}
