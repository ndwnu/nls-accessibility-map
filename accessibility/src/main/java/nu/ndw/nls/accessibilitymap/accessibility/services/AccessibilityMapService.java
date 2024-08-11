package nu.ndw.nls.accessibilitymap.accessibility.services;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.AccessibilityMap;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.factory.AccessibilityMapFactory;
import nu.ndw.nls.accessibilitymap.accessibility.model.AccessibleRoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.model.Municipality;
import nu.ndw.nls.accessibilitymap.accessibility.model.RoadSection;
import nu.ndw.nls.routingmapmatcher.model.IsochroneMatch;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessibilityMapService {

    private static final Boolean ACCESSIBLE_ROAD_DIRECTION_NOT_ACCESSIBLE_IN_NWB = null;
    private static final boolean ACCESSIBLE_ROAD_DIRECTION_IN_NWB_INITIALIZE_AS_INACCESSIBLE = false;
    private static final boolean ACCESSIBLE_ROAD_DIRECTION_IN_ISOCHRONE_RESULT = true;

    private final AccessibilityMapFactory accessibilityMapFactory;
    private final NetworkGraphHopper networkGraphHopper;
    private final MunicipalityService municipalityService;
    private final AccessibleRoadsService accessibleRoadsService;
    private final AccessibleRoadSectionsService accessibleRoadSectionsService;

    public enum ResultType {
        // Uses isochrone for determining base accessibility for the NWB road sections and then also for when
        // the additional road sign restrictions are applied and finally subtracts them to find the difference
        // This is useful for when you want to know how traffic signs are adding additional restrictions. In this mode,
        // you will not find road sections that have no (car accessible) roads connecting them, because they are not
        // accessible in the base isochrone response so there is no difference between the base accessibility and the
        // one generated with added traffic sign restrictions
        DIFFERENCE_OF_ADDED_RESTRICTIONS,
        // Includes all NWB road sections to determine the accessibility and then uses isochrone to determine how
        // traffic signs are adding additional restrictions. The result is a map that indicates which road sections
        // you cannot reach from the start point.
        EFFECTIVE_ACCESSIBILITY;
    }

    public SortedMap<Integer, RoadSection> determineAccessibilityByRoadSection(VehicleProperties vehicleProperties,
            Point startPoint, double searchDistanceInMeters, ResultType resultType) {
        AccessibilityMap accessibilityMap = accessibilityMapFactory.createMapMatcher(networkGraphHopper);

        List<IsochroneMatch> accessibleRoadsWithRestrictions = accessibleRoadsService
                .getVehicleAccessibleRoads(accessibilityMap, vehicleProperties, startPoint, searchDistanceInMeters);

        if (resultType == ResultType.EFFECTIVE_ACCESSIBILITY) {
            return determineUnion(accessibleRoadsWithRestrictions, accessibleRoadSectionsService::getRoadSections);
        } else {
            List<IsochroneMatch> baseAccessibleIsochrone = accessibleRoadsService.getBaseAccessibleRoads(
                    accessibilityMap, startPoint, searchDistanceInMeters);

            return determineAddedRestrictionsDifference(baseAccessibleIsochrone, accessibleRoadsWithRestrictions);
        }
    }

    public SortedMap<Integer, RoadSection> determineAccessibilityByRoadSection(VehicleProperties vehicleProperties,
            String municipalityId, ResultType resultType) {
        AccessibilityMap accessibilityMap = accessibilityMapFactory.createMapMatcher(networkGraphHopper);
        Municipality municipality = municipalityService.getMunicipalityById(municipalityId);

        List<IsochroneMatch> accessibleRoadsWithRestrictions = accessibleRoadsService
                .getVehicleAccessibleRoadsByMunicipality(accessibilityMap, vehicleProperties, municipality);

        if (resultType == ResultType.EFFECTIVE_ACCESSIBILITY) {
            return determineUnion(accessibleRoadsWithRestrictions,
                    () -> accessibleRoadSectionsService.getRoadSectionIdToRoadSection(
                            municipality.getMunicipalityIdInteger()));
        } else {
            List<IsochroneMatch> baseAccessibleIsochrone = accessibleRoadsService.getBaseAccessibleRoadsByMunicipality(
                    accessibilityMap, municipality);

            return determineAddedRestrictionsDifference(baseAccessibleIsochrone, accessibleRoadsWithRestrictions);
        }
    }

    private SortedMap<Integer, RoadSection> determineAddedRestrictionsDifference(
            List<IsochroneMatch> withoutRestrictions, List<IsochroneMatch> withRestrictions) {
        SortedMap<Integer, RoadSection> roadSections = new TreeMap<>();
        for (IsochroneMatch m : withoutRestrictions) {
            // Graphhopper returns the geometry in the driving direction, we want this geometry in its original
            // direction. Flip the geometry if isochrone returns the reversed as first result
            roadSections.computeIfAbsent(m.getMatchedLinkId(), i -> new RoadSection(i,
                    m.isReversed() ? m.getGeometry().reverse() : m.getGeometry()));
            RoadSection r = roadSections.get(m.getMatchedLinkId());
            // Accessible remains null in case road section is not present in both directions in baseline.
            // This way, non-existing directions of one-way roads (null) can be distinguished from inaccessible
            // directions due to restrictions (false).
            if (m.isReversed()) {
                r.setBackwardAccessible(ACCESSIBLE_ROAD_DIRECTION_IN_NWB_INITIALIZE_AS_INACCESSIBLE);
            } else {
                r.setForwardAccessible(ACCESSIBLE_ROAD_DIRECTION_IN_NWB_INITIALIZE_AS_INACCESSIBLE);
            }
        }
        for (IsochroneMatch m : withRestrictions) {
            RoadSection r = roadSections.get(m.getMatchedLinkId());
            if (m.isReversed()) {
                r.setBackwardAccessible(ACCESSIBLE_ROAD_DIRECTION_IN_ISOCHRONE_RESULT);
            } else {
                r.setForwardAccessible(ACCESSIBLE_ROAD_DIRECTION_IN_ISOCHRONE_RESULT);
            }
        }

        return roadSections;
    }

    private SortedMap<Integer, RoadSection> determineUnion(List<IsochroneMatch> withRestrictions,
            Supplier<List<AccessibleRoadSection>> listSupplier) {

        // For every road driving direction that is accessible for a car in the NWB municipality area, we initialize our
        // RoadSection with false. We then use the withRestrictions isochrone result to update all road section
        // directions which are accessible with true and effectively end up with RoadSection objects that have
        // accessible states with three values:
        // - null: road section driving direction not accessible in NWB
        // - false: road section driving direction accessible in NWB, but did not exist in the isochrone response and is
        //          inaccessible with the restrictions applied to the isochrone
        // - true: road section driving direction exists in NWB and was found in isochrone response and is accessible
        //         with the restrictions applied to the isochrone
        SortedMap<Integer, RoadSection> roadSections =
                listSupplier.get().stream()
                .map(this::intializeRoadSection)
                .collect(Collectors.toMap(RoadSection::getRoadSectionId, Function.identity(), (a, b) -> a,
                        TreeMap::new));

        for (IsochroneMatch m : withRestrictions) {
            RoadSection r = roadSections.get(m.getMatchedLinkId());
            if (m.isReversed()) {
                r.setBackwardAccessible(ACCESSIBLE_ROAD_DIRECTION_IN_ISOCHRONE_RESULT);
            } else {
                r.setForwardAccessible(ACCESSIBLE_ROAD_DIRECTION_IN_ISOCHRONE_RESULT);
            }
        }

        return roadSections;
    }

    private RoadSection intializeRoadSection(AccessibleRoadSection accessibleRoadSection) {
        return new RoadSection( accessibleRoadSection.getRoadSectionId(),
                                accessibleRoadSection.getGeometry(),
                                initializeNwbAccessibleRoads(accessibleRoadSection.isForwardAccessible()),
                                initializeNwbAccessibleRoads(accessibleRoadSection.isBackwardAccessible()));
    }

    private Boolean initializeNwbAccessibleRoads(boolean accessible) {
        if (accessible) {
            return ACCESSIBLE_ROAD_DIRECTION_IN_NWB_INITIALIZE_AS_INACCESSIBLE;
        } else {
            return ACCESSIBLE_ROAD_DIRECTION_NOT_ACCESSIBLE_IN_NWB;
        }
    }

}
