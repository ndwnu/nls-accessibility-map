package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.services;

import com.graphhopper.config.Profile;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.CustomModel;
import com.graphhopper.util.PMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.IsochroneService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.factory.IsochroneServiceFactory;
import nu.ndw.nls.accessibilitymap.accessibility.model.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.model.IsochroneArguments;
import nu.ndw.nls.accessibilitymap.accessibility.services.VehicleRestrictionsModelFactory;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.Direction;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.RoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.services.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.services.dto.AdditionalSnap;
import nu.ndw.nls.accessibilitymap.shared.model.NetworkConstants;
import nu.ndw.nls.routingmapmatcher.model.IsochroneMatch;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessibilityService {

    private final IsochroneServiceFactory isochroneServiceFactory;

    private final NetworkGraphHopper network;

    private final VehicleRestrictionsModelFactory modelFactory;

    private final NetworkGraphHopper networkGraphHopper;

    public Accessibility calculateAccessibility(
            AccessibilityRequest accessibilityRequest,
            List<AdditionalSnap> additionalSnaps) {
        IsochroneService isochroneService = isochroneServiceFactory.createService(networkGraphHopper);

        List<Snap> snaps = additionalSnaps.stream()
                .map(AdditionalSnap::snap)
                .toList();

        Snap startSegment = networkGraphHopper.getLocationIndex()
                .findClosest(accessibilityRequest.startPoint().getX(), accessibilityRequest.startPoint().getY(),
                        EdgeFilter.ALL_EDGES);
        snaps.add(startSegment);

        QueryGraph queryGraph = QueryGraph.create(networkGraphHopper.getBaseGraph(), snaps);

        //TODO loop through snaps and check all virtual nodes and update properties.

        Collection<RoadSection> accessibleRoadsSectionsWithoutAppliedRestrictions =
                mapToRoadSections(
                        isochroneService.getIsochroneMatchesByMunicipalityId(
                                IsochroneArguments.builder()
                                        .weighting(buildWeightingWithoutRestrictions(accessibilityRequest))
                                        .startPoint(accessibilityRequest.startPoint())
                                        .municipalityId(accessibilityRequest.municipalityId())
                                        .searchDistanceInMetres(accessibilityRequest.searchDistanceInMetres())
                                        .build(),
                                queryGraph,
                                startSegment));

        Collection<RoadSection> accessibleRoadSectionsWithAppliedRestrictions =
                mapToRoadSections(
                        isochroneService.getIsochroneMatchesByMunicipalityId(
                                IsochroneArguments.builder()
                                        .weighting(buildWeightingWithRestrictions(accessibilityRequest))
                                        .startPoint(accessibilityRequest.startPoint())
                                        .municipalityId(accessibilityRequest.municipalityId())
                                        .searchDistanceInMetres(accessibilityRequest.searchDistanceInMetres())
                                        .build(),
                                queryGraph,
                                startSegment));

        return Accessibility.builder()
                .accessibleRoadsSectionsWithoutAppliedRestrictions(accessibleRoadsSectionsWithoutAppliedRestrictions)
                .accessibleRoadSectionsWithAppliedRestrictions(accessibleRoadSectionsWithAppliedRestrictions)
                .mergedAccessibility(
                        mergeNoRestrictionsWithAccessibilityRestrictions(
                                accessibleRoadsSectionsWithoutAppliedRestrictions,
                                accessibleRoadSectionsWithAppliedRestrictions))
                .build();
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
                roadSection.getBackwardSegments().add(buildDirectionalSegment(isochroneMatch, roadSection));
            } else {
                roadSection.getForwardSegments().add(buildDirectionalSegment(isochroneMatch, roadSection));
            }
        });

        return roadSectionsGroupedById.values();
    }

    private static DirectionalSegment buildDirectionalSegment(IsochroneMatch isochroneMatch, RoadSection roadSection) {

        return DirectionalSegment.builder()
                .id(isochroneMatch.getEdgeKey())
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

    private static void addNewDirectionSegmentToRoadSection(
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
        CustomModel model = modelFactory.getModel(accessibilityRequest.vehicleProperties());
        PMap hints = new PMap().putObject(CustomModel.KEY, model);

        return network.createWeighting(profile, hints);
    }

    private Weighting buildWeightingWithRestrictions(AccessibilityRequest accessibilityRequest) {
        Profile profile = networkGraphHopper.getProfile(NetworkConstants.VEHICLE_NAME_CAR);
        CustomModel model = modelFactory.getModel(accessibilityRequest.vehicleProperties());
        PMap hints = new PMap().putObject(CustomModel.KEY, model);

        return network.createWeighting(profile, hints);
    }
}
