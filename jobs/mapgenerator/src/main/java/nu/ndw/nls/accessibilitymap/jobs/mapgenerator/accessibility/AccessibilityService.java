package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility;

import static java.util.stream.Collectors.toCollection;

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.IsochroneService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.factory.IsochroneServiceFactory;
import nu.ndw.nls.accessibilitymap.accessibility.model.IsochroneArguments;
import nu.ndw.nls.accessibilitymap.accessibility.services.VehicleRestrictionsModelFactory;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.AdditionalSnap;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.mapper.RoadSectionMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.model.RoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.model.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.graphhopper.QueryGraphConfigurer;
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

    private final NetworkGraphHopper network;

    private final VehicleRestrictionsModelFactory modelFactory;

    private final NetworkGraphHopper networkGraphHopper;

    private final TrafficSignDataService trafficSignDataService;

    private final GeometryFactoryWgs84 geometryFactoryWgs84;

    private final RoadSectionMapper roadSectionMapper;

    private final QueryGraphConfigurer queryGraphConfigurer;

    private final RoadSectionMerger roadSectionMerger;

    @Timed(description = "Time spent calculating accessibility")
    public Accessibility calculateAccessibility(AccessibilityRequest accessibilityRequest) {

        OffsetDateTime startTime = OffsetDateTime.now();
        IsochroneService isochroneService = isochroneServiceFactory.createService(networkGraphHopper);

        List<AdditionalSnap> additionalSnaps = buildTrafficSignSnaps(accessibilityRequest);
        Point startPoint = createPoint(
                accessibilityRequest.getStartLocationLatitude(),
                accessibilityRequest.getStartLocationLongitude());

        Snap startSegment = networkGraphHopper.getLocationIndex()
                .findClosest(startPoint.getY(), startPoint.getX(), EdgeFilter.ALL_EDGES);

        List<Snap> snaps = additionalSnaps
                .stream()
                .map(AdditionalSnap::getSnap)
                .collect(toCollection(ArrayList::new));
        snaps.add(startSegment);

        QueryGraph queryGraph = QueryGraph.create(networkGraphHopper.getBaseGraph(), snaps);
        queryGraphConfigurer.configure(queryGraph, additionalSnaps);
        Map<Integer, TrafficSign> trafficSignById = buildTrafficSignById(additionalSnaps);

        Collection<RoadSection> accessibleRoadsSectionsWithoutAppliedRestrictions =
                getRoadSections(
                        accessibilityRequest,
                        isochroneService,
                        startPoint,
                        queryGraph,
                        startSegment,
                        trafficSignById,
                        buildWeightingWithoutRestrictions(accessibilityRequest));

        Collection<RoadSection> accessibleRoadSectionsWithAppliedRestrictions =
                getRoadSections(
                        accessibilityRequest,
                        isochroneService,
                        startPoint,
                        queryGraph,
                        startSegment,
                        trafficSignById,
                        buildWeightingWithRestrictions(accessibilityRequest));

        Accessibility accessibility = Accessibility.builder()
                .accessibleRoadsSectionsWithoutAppliedRestrictions(accessibleRoadsSectionsWithoutAppliedRestrictions)
                .accessibleRoadSectionsWithAppliedRestrictions(accessibleRoadSectionsWithAppliedRestrictions)
                .mergedAccessibility(
                        roadSectionMerger.mergeNoRestrictionsWithAccessibilityRestrictions(
                                accessibleRoadsSectionsWithoutAppliedRestrictions,
                                accessibleRoadSectionsWithAppliedRestrictions))
                .build();

        log.debug("Accessibility generation done. It took: %s ms"
                .formatted(ChronoUnit.MILLIS.between(startTime, OffsetDateTime.now())));
        return accessibility;
    }

    private Collection<RoadSection> getRoadSections(
            AccessibilityRequest accessibilityRequest,
            IsochroneService isochroneService,
            Point startPoint,
            QueryGraph queryGraph,
            Snap startSegment,
            Map<Integer, TrafficSign> trafficSignById,
            Weighting weighting) {

        return roadSectionMapper.mapToRoadSections(
                isochroneService.getIsochroneMatchesByMunicipalityId(
                        IsochroneArguments.builder()
                                .weighting(weighting)
                                .startPoint(startPoint)
                                .municipalityId(accessibilityRequest.getMunicipalityId())
                                .searchDistanceInMetres(accessibilityRequest.getSearchDistanceInMetres())
                                .build(),
                        queryGraph,
                        startSegment),
                trafficSignById);
    }

    private Map<Integer, TrafficSign> buildTrafficSignById(List<AdditionalSnap> additionalSnaps) {

        return additionalSnaps.stream()
                .collect(Collectors.toMap(
                        additionalSnap -> additionalSnap.getTrafficSign().id(),
                        AdditionalSnap::getTrafficSign));
    }

    private List<AdditionalSnap> buildTrafficSignSnaps(AccessibilityRequest accessibilityRequest) {

        List<TrafficSign> trafficSigns = trafficSignDataService.findAllByType(
                accessibilityRequest.getTrafficSignType());

        return trafficSigns.stream()
                .filter(trafficSign -> applyTimeWindowedSignFilter(accessibilityRequest, trafficSign))
                .map(trafficSign -> AdditionalSnap.builder()
                        .trafficSign(trafficSign)
                        .snap(networkGraphHopper.getLocationIndex().findClosest(
                                trafficSign.latitude(),
                                trafficSign.longitude(),
                                EdgeFilter.ALL_EDGES))
                        .build())
                .collect(toCollection(ArrayList::new));
    }

    private boolean applyTimeWindowedSignFilter(AccessibilityRequest accessibilityRequest, TrafficSign trafficSign) {

        return accessibilityRequest.isIncludeOnlyTimeWindowedSigns() && trafficSign.hasTimeWindowedSign();
    }

    private Weighting buildWeightingWithoutRestrictions(AccessibilityRequest accessibilityRequest) {
        accessibilityRequest = accessibilityRequest.withVehicleProperties(null);
        Profile profile = networkGraphHopper.getProfile(NetworkConstants.VEHICLE_NAME_CAR);
        CustomModel model = modelFactory.getModel(accessibilityRequest.getVehicleProperties());
        PMap hints = new PMap().putObject(CustomModel.KEY, model);

        return network.createWeighting(profile, hints);
    }

    private Weighting buildWeightingWithRestrictions(AccessibilityRequest accessibilityRequest) {
        Profile profile = networkGraphHopper.getProfile(NetworkConstants.VEHICLE_NAME_CAR);
        CustomModel model = modelFactory.getModel(accessibilityRequest.getVehicleProperties());
        PMap hints = new PMap().putObject(CustomModel.KEY, model);
        return network.createWeighting(profile, hints);
    }

    private Point createPoint(double latitude, double longitude) {
        return geometryFactoryWgs84.createPoint(new Coordinate(longitude, latitude));
    }

}
