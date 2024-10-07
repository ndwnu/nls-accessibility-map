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
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
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
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.graphhopper.QueryGraphConfigurer;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.model.Direction;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.model.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.model.RoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.model.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.model.trafficsign.TrafficSign;
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

    @Timed(description = "Time spent calculating accessibility")
    public Accessibility calculateAccessibility(AccessibilityRequest accessibilityRequest) {

        OffsetDateTime startTime = OffsetDateTime.now();
        IsochroneService isochroneService = isochroneServiceFactory.createService(networkGraphHopper);

        List<AdditionalSnap> additionalSnaps = buildTrafficSignSnaps(accessibilityRequest);
        Point startPoint = createPoint(
                accessibilityRequest.getStartLocationLatitude(),
                accessibilityRequest.getStartLocationLongitude());

        Snap startSegment = networkGraphHopper.getLocationIndex()
                .findClosest(startPoint.getY(), startPoint.getX(),
                        EdgeFilter.ALL_EDGES);

        List<Snap> snaps = additionalSnaps
                .stream()
                .map(AdditionalSnap::getSnap)
                .collect(toCollection(ArrayList::new));
        snaps.add(startSegment);

        QueryGraph queryGraph = QueryGraph.create(networkGraphHopper.getBaseGraph(), snaps);
        queryGraphConfigurer.configure(queryGraph, additionalSnaps);
        Map<Integer, TrafficSign> trafficSignById = buildTrafficSignById(additionalSnaps);

        Collection<RoadSection> accessibleRoadsSectionsWithoutAppliedRestrictions =
                roadSectionMapper.mapToRoadSections(
                        isochroneService.getIsochroneMatchesByMunicipalityId(
                                IsochroneArguments.builder()
                                        .weighting(buildWeightingWithoutRestrictions(accessibilityRequest))
                                        .startPoint(startPoint)
                                        .municipalityId(accessibilityRequest.getMunicipalityId())
                                        .searchDistanceInMetres(accessibilityRequest.getSearchDistanceInMetres())
                                        .build(),
                                queryGraph,
                                startSegment),
                        trafficSignById);

        Collection<RoadSection> accessibleRoadSectionsWithAppliedRestrictions =
                roadSectionMapper.mapToRoadSections(
                        isochroneService.getIsochroneMatchesByMunicipalityId(
                                IsochroneArguments.builder()
                                        .weighting(
                                                buildWeightingWithRestrictions(accessibilityRequest))
                                        .startPoint(startPoint)
                                        .municipalityId(accessibilityRequest.getMunicipalityId())
                                        .searchDistanceInMetres(accessibilityRequest.getSearchDistanceInMetres())
                                        .build(),
                                queryGraph,
                                startSegment),
                        trafficSignById);

        Accessibility accessibility = Accessibility.builder()
                .accessibleRoadsSectionsWithoutAppliedRestrictions(accessibleRoadsSectionsWithoutAppliedRestrictions)
                .accessibleRoadSectionsWithAppliedRestrictions(accessibleRoadSectionsWithAppliedRestrictions)
                .mergedAccessibility(
                        mergeNoRestrictionsWithAccessibilityRestrictions(
                                accessibleRoadsSectionsWithoutAppliedRestrictions,
                                accessibleRoadSectionsWithAppliedRestrictions))
                .build();

        log.debug("Accessibility generation done. It took: %s ms"
                .formatted(ChronoUnit.MILLIS.between(startTime, OffsetDateTime.now())));
        return accessibility;
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

        return accessibilityRequest.isIncludeOnlyTimeWindowedSigns() ? trafficSign.hasTimeWindowedSign() : false;
    }

    private Collection<RoadSection> mergeNoRestrictionsWithAccessibilityRestrictions(
            Collection<RoadSection> accessibleRoadsSectionsWithoutAppliedRestrictions,
            Collection<RoadSection> accessibleRoadSectionsWithAppliedRestrictions) {

        List<DirectionalSegment> allDirectionalSegments =
                accessibleRoadsSectionsWithoutAppliedRestrictions.stream()
                        .flatMap(roadSection -> roadSection.getRoadSectionFragments().stream())
                        .flatMap(roadSectionFragment -> roadSectionFragment.getSegments().stream())
                        .toList();

        Map<Integer, DirectionalSegment> directionalSegmentsThatAreAccessible =
                accessibleRoadSectionsWithAppliedRestrictions.stream()
                        .flatMap(roadSection -> roadSection.getRoadSectionFragments().stream())
                        .flatMap(roadSectionFragment -> roadSectionFragment.getSegments().stream())
                        .collect(Collectors.toMap(DirectionalSegment::getId, Function.identity()));

        SortedMap<Long, RoadSection> roadSectionsById = new TreeMap<>();
        SortedMap<Integer, RoadSectionFragment> roadSectionsFragmentsById = new TreeMap<>();

        allDirectionalSegments.forEach(
                directionalSegmentToCopyFrom -> {
                    RoadSection newRoadSection = roadSectionsById.computeIfAbsent(
                            directionalSegmentToCopyFrom.getRoadSectionFragment().getRoadSection().getId(),
                            roadSectionId -> RoadSection.builder()
                                    .id(roadSectionId)
                                    .build());

                    RoadSectionFragment newRoadSectionFraction = roadSectionsFragmentsById.computeIfAbsent(
                            directionalSegmentToCopyFrom.getRoadSectionFragment().getId(),
                            roadSectionFragmentId -> {
                                RoadSectionFragment newRoadSectionFragment = RoadSectionFragment.builder()
                                        .id(roadSectionFragmentId)
                                        .roadSection(newRoadSection)
                                        .build();

                                newRoadSection.getRoadSectionFragments().add(newRoadSectionFragment);
                                return newRoadSectionFragment;
                            });

                    addNewDirectionSegmentToRoadSection(
                            newRoadSectionFraction,
                            directionalSegmentToCopyFrom,
                            directionalSegmentsThatAreAccessible.get(directionalSegmentToCopyFrom.getId()));
                });

        return roadSectionsById.values();
    }

    private void addNewDirectionSegmentToRoadSection(
            RoadSectionFragment newRoadSectionFraction,
            DirectionalSegment directionalSegmentToCopyFrom,
            DirectionalSegment accessibleDirectionSegment) {

        DirectionalSegment newDirectionSegment = directionalSegmentToCopyFrom
                .withAccessible(
                        Objects.nonNull(accessibleDirectionSegment) && accessibleDirectionSegment.isAccessible()
                )
                .withRoadSectionFragment(newRoadSectionFraction);

        if (directionalSegmentToCopyFrom.getDirection() == Direction.BACKWARD) {
            newRoadSectionFraction.getBackwardSegments().add(newDirectionSegment);
        } else {
            newRoadSectionFraction.getForwardSegments().add(newDirectionSegment);
        }
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
