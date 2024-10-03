package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.accessibility;

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
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.accessibility.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.accessibility.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.accessibility.dto.AdditionalSnap;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.Direction;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.RoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.trafficsign.services.TrafficSignDataService;
import nu.ndw.nls.accessibilitymap.shared.model.NetworkConstants;
import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
import nu.ndw.nls.routingmapmatcher.model.IsochroneMatch;
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

    @Timed(description = "Time spent calculating accessibility")
    public Accessibility calculateAccessibility(AccessibilityRequest accessibilityRequest) {

        OffsetDateTime startTime = OffsetDateTime.now();
        IsochroneService isochroneService = isochroneServiceFactory.createService(networkGraphHopper);

        List<AdditionalSnap> additionalSnaps = buildTrafficSignSnaps(accessibilityRequest);
        Point startPoint = createPoint(
                accessibilityRequest.getStartLocationLatitude(),
                accessibilityRequest.getStartLocationLongitude());

        Snap startSegment = networkGraphHopper.getLocationIndex()
                .findClosest(startPoint.getY(), startPoint.getX(),EdgeFilter.ALL_EDGES);
        additionalSnaps.add(AdditionalSnap.builder()
                .snap(startSegment)
                .build());

        QueryGraph queryGraph = QueryGraph.create(networkGraphHopper.getBaseGraph(), List.of(startSegment));

        //TODO loop through snaps and check all virtual nodes and update properties.

        Collection<RoadSection> accessibleRoadsSectionsWithoutAppliedRestrictions =
                mapToRoadSections(
                        isochroneService.getIsochroneMatchesByMunicipalityId(
                                IsochroneArguments.builder()
                                        .weighting(buildWeightingWithoutRestrictions(accessibilityRequest))
                                        .startPoint(startPoint)
                                        .municipalityId(accessibilityRequest.getMunicipalityId())
                                        .searchDistanceInMetres(accessibilityRequest.getSearchDistanceInMetres())
                                        .build(),
                                queryGraph,
                                startSegment));

        Collection<RoadSection> accessibleRoadSectionsWithAppliedRestrictions =
                mapToRoadSections(
                        isochroneService.getIsochroneMatchesByMunicipalityId(
                                IsochroneArguments.builder()
                                        .weighting(buildWeightingWithRestrictions(accessibilityRequest))
                                        .startPoint(startPoint)
                                        .municipalityId(accessibilityRequest.getMunicipalityId())
                                        .searchDistanceInMetres(accessibilityRequest.getSearchDistanceInMetres())
                                        .build(),
                                queryGraph,
                                startSegment));

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
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private boolean applyTimeWindowedSignFilter(AccessibilityRequest accessibilityRequest, TrafficSign trafficSign) {

        return accessibilityRequest.isIncludeOnlyTimeWindowedSigns() ? trafficSign.hasTimeWindowedSign() : false;
    }

    private Collection<RoadSection> mapToRoadSections(List<IsochroneMatch> isochroneMatches) {

        SortedMap<Integer, RoadSection> roadSectionsGroupedById = new TreeMap<>();

        isochroneMatches.forEach(isochroneMatch -> {
            RoadSection roadSection = roadSectionsGroupedById.computeIfAbsent(
                    isochroneMatch.getMatchedLinkId(),
                    roadSectionId -> RoadSection.builder()
                            .roadSectionId(isochroneMatch.getMatchedLinkId())
                            .build());

            if (isochroneMatch.isReversed()) {
                roadSection.getBackwardSegments().add(
                        buildDirectionalSegment(Direction.BACKWARD, isochroneMatch, roadSection));
            } else {
                roadSection.getForwardSegments().add(
                        buildDirectionalSegment(Direction.FORWARD, isochroneMatch, roadSection));
            }
        });

        return roadSectionsGroupedById.values();
    }

    private  DirectionalSegment buildDirectionalSegment(
            Direction direction,
            IsochroneMatch isochroneMatch,
            RoadSection roadSection) {

        return DirectionalSegment.builder()
                .id(isochroneMatch.getEdgeKey())
                .direction(direction)
                .accessible(true)
                .lineString(isochroneMatch.getGeometry())
                .roadSection(roadSection)
                .build();
    }

    private Collection<RoadSection> mergeNoRestrictionsWithAccessibilityRestrictions(
            Collection<RoadSection> accessibleRoadsSectionsWithoutAppliedRestrictions,
            Collection<RoadSection> accessibleRoadSectionsWithAppliedRestrictions) {

        List<DirectionalSegment> allDirectionalSegments =
                accessibleRoadsSectionsWithoutAppliedRestrictions.stream()
                        .flatMap(roadSection -> roadSection.getSegments().stream())
                        .toList();

        Map<Integer, DirectionalSegment> directionalSegmentsThatAreAccessible =
                accessibleRoadSectionsWithAppliedRestrictions.stream()
                        .flatMap(roadSection -> roadSection.getSegments().stream())
                        .collect(Collectors.toMap(DirectionalSegment::getId, Function.identity()));

        SortedMap<Integer, RoadSection> roadSectionsGroupedById = new TreeMap<>();
        allDirectionalSegments.forEach(directionalSegmentToCopyFrom -> {
            RoadSection newRoadSection = roadSectionsGroupedById.computeIfAbsent(
                    directionalSegmentToCopyFrom.getId(),
                    roadSectionId -> RoadSection.builder()
                            .roadSectionId(directionalSegmentToCopyFrom.getRoadSection().getRoadSectionId())
                            .build());

            addNewDirectionSegmentToRoadSection(newRoadSection, directionalSegmentToCopyFrom,
                    directionalSegmentsThatAreAccessible.get(directionalSegmentToCopyFrom.getId()));
        });

        return roadSectionsGroupedById.values();
    }

    private void addNewDirectionSegmentToRoadSection(
            RoadSection roadSection,
            DirectionalSegment directionalSegmentToCopyFrom,
            DirectionalSegment accessibleDirectionSegment) {

        if (directionalSegmentToCopyFrom.getDirection() == Direction.BACKWARD) {
            roadSection.getBackwardSegments().add(directionalSegmentToCopyFrom.withAccessible(
                    Objects.nonNull(accessibleDirectionSegment) && accessibleDirectionSegment.isAccessible()
            ));
        } else {
            roadSection.getForwardSegments().add(directionalSegmentToCopyFrom.withAccessible(
                    Objects.nonNull(accessibleDirectionSegment) && accessibleDirectionSegment.isAccessible()
            ));
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
