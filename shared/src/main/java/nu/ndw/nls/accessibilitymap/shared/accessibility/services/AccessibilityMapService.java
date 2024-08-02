package nu.ndw.nls.accessibilitymap.shared.accessibility.services;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.shared.accessibility.graphhopper.AccessibilityMap;
import nu.ndw.nls.accessibilitymap.shared.accessibility.graphhopper.factory.AccessibilityMapFactory;
import nu.ndw.nls.accessibilitymap.shared.accessibility.model.CachedRoadSection;
import nu.ndw.nls.accessibilitymap.shared.accessibility.model.Municipality;
import nu.ndw.nls.accessibilitymap.shared.accessibility.model.RoadSection;
import nu.ndw.nls.accessibilitymap.shared.accessibility.model.VehicleProperties;
import nu.ndw.nls.routingmapmatcher.model.IsochroneMatch;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
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
    private final CachedMunicipalityRoadSectionsService cachedMunicipalityRoadSectionsService;

    public SortedMap<Integer, RoadSection> determineAccessibilityByRoadSection(VehicleProperties vehicleProperties,
            String municipalityId) {
        AccessibilityMap accessibilityMap = accessibilityMapFactory.createMapMatcher(networkGraphHopper);
        Municipality municipality = municipalityService.getMunicipalityById(municipalityId);

        List<IsochroneMatch> accessibleRoadsWithRestrictions = accessibleRoadsService
                .getVehicleAccessibleRoadsByMunicipality(accessibilityMap, vehicleProperties, municipality);

        return determineDifference(accessibleRoadsWithRestrictions, municipality.getMunicipalityIdInteger());
    }

    private SortedMap<Integer, RoadSection> determineDifference(List<IsochroneMatch> withRestrictions,
            int municipalityId) {

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
                cachedMunicipalityRoadSectionsService.getRoadSectionIdToRoadSection(municipalityId)
                .stream()
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

    private RoadSection intializeRoadSection(CachedRoadSection cachedRoadSection) {
        return new RoadSection( cachedRoadSection.getRoadSectionId(),
                                cachedRoadSection.getGeometry(),
                                initializeNwbAccessibleRoads(cachedRoadSection.isForwardAccessible()),
                                initializeNwbAccessibleRoads(cachedRoadSection.isBackwardAccessible()));
    }

    private Boolean initializeNwbAccessibleRoads(boolean accessible) {
        if (accessible) {
            return ACCESSIBLE_ROAD_DIRECTION_IN_NWB_INITIALIZE_AS_INACCESSIBLE;
        } else {
            return ACCESSIBLE_ROAD_DIRECTION_NOT_ACCESSIBLE_IN_NWB;
        }
    }

}
