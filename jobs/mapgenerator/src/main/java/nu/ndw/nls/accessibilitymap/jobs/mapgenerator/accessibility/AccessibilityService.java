package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility;

import com.graphhopper.config.Profile;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.CustomModel;
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
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.IsochroneService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.factory.IsochroneServiceFactory;
import nu.ndw.nls.accessibilitymap.accessibility.model.IsochroneArguments;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import nu.ndw.nls.accessibilitymap.accessibility.services.VehicleRestrictionsModelFactory;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.TrafficSignSnap;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.mappers.RoadSectionMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.mappers.TrafficSignSnapMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.time.ClockService;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.graphhopper.QueryGraphFactory;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.trafficsign.services.TrafficSignDataService;
import nu.ndw.nls.accessibilitymap.shared.model.NetworkConstants;
import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccessibilityService {

    private final IsochroneServiceFactory isochroneServiceFactory;

    private final NetworkGraphHopper networkGraphHopper;

    private final VehicleRestrictionsModelFactory vehicleRestrictionsModelFactory;

    private final TrafficSignDataService trafficSignDataService;

    private final GeometryFactoryWgs84 geometryFactoryWgs84;

    private final RoadSectionMapper roadSectionMapper;

    private final RoadSectionCombinator roadSectionCombinator;

    private final ClockService clockService;

    private final TrafficSignSnapMapper trafficSingSnapMapper;

    private final QueryGraphFactory queryGraphFactory;

    @Timed(description = "Time spent calculating accessibility")
    public Accessibility calculateAccessibility(AccessibilityRequest accessibilityRequest) {

        OffsetDateTime startTime = clockService.now();
        List<TrafficSignSnap> snappedTrafficSigns = buildTrafficSignSnaps(accessibilityRequest);
        Point startPoint = createPoint(
                accessibilityRequest.getStartLocationLatitude(),
                accessibilityRequest.getStartLocationLongitude());
        Snap startSegment = networkGraphHopper.getLocationIndex()
                .findClosest(startPoint.getY(), startPoint.getX(), EdgeFilter.ALL_EDGES);
        QueryGraph queryGraph = queryGraphFactory.createQueryGraph(snappedTrafficSigns, startSegment);

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
                        buildWeighting(null));

        Collection<RoadSection> accessibleRoadSectionsWithAppliedRestrictions =
                getRoadSections(
                        accessibilityRequest,
                        isochroneService,
                        startPoint,
                        queryGraph,
                        startSegment,
                        trafficSignsById,
                        buildWeighting(accessibilityRequest.getVehicleProperties()));

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
            Weighting weighting) {

        return roadSectionMapper.mapToRoadSections(
                isochroneService.getIsochroneMatchesByMunicipalityId(
                        IsochroneArguments.builder()
                                .weighting(weighting)
                                .startPoint(startPoint)
                                .municipalityId(accessibilityRequest.getMunicipalityId())
                                .searchDistanceInMetres(accessibilityRequest.getSearchRadiusInMeters())
                                .build(),
                        queryGraph,
                        startSegment),
                trafficSignsById);
    }

    private Map<Integer, TrafficSign> buildTrafficSignById(List<TrafficSignSnap> additionalSnaps) {

        return additionalSnaps.stream()
                .collect(Collectors.toMap(
                        additionalSnap -> additionalSnap.getTrafficSign().id(),
                        TrafficSignSnap::getTrafficSign));
    }

    private List<TrafficSignSnap> buildTrafficSignSnaps(AccessibilityRequest accessibilityRequest) {

        List<TrafficSign> trafficSigns = trafficSignDataService.findAllByTypes(
                accessibilityRequest.getTrafficSignTypes());
        return trafficSingSnapMapper.map(trafficSigns, accessibilityRequest.isIncludeOnlyTimeWindowedSigns());
    }

    private Weighting buildWeighting(VehicleProperties vehicleProperties) {

        Profile profile = networkGraphHopper.getProfile(NetworkConstants.VEHICLE_NAME_CAR);
        CustomModel model = vehicleRestrictionsModelFactory.getModel(vehicleProperties);
        PMap hints = new PMap().putObject(CustomModel.KEY, model);

        return networkGraphHopper.createWeighting(profile, hints);
    }

    private Point createPoint(double latitude, double longitude) {

        return geometryFactoryWgs84.createPoint(new Coordinate(longitude, latitude));
    }

}
