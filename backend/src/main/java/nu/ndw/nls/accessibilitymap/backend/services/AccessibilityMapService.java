package nu.ndw.nls.accessibilitymap.backend.services;

import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.backend.graphhopper.AccessibilityMap;
import nu.ndw.nls.accessibilitymap.backend.graphhopper.factory.AccessibilityMapFactory;
import nu.ndw.nls.accessibilitymap.backend.model.Municipality;
import nu.ndw.nls.accessibilitymap.backend.model.RoadSection;
import nu.ndw.nls.accessibilitymap.backend.model.VehicleProperties;
import nu.ndw.nls.routingmapmatcher.model.IsochroneMatch;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessibilityMapService {

    private final AccessibilityMapFactory accessibilityMapFactory;
    private final NetworkGraphHopper networkGraphHopper;
    private final MunicipalityService municipalityService;
    private final AccessibleRoadsService accessibleRoadsService;

    public SortedMap<Integer, RoadSection> determineAccessibilityByRoadSection(VehicleProperties vehicleProperties,
            String municipalityId) {
        AccessibilityMap accessibilityMap = accessibilityMapFactory.createMapMatcher(networkGraphHopper);
        Municipality municipality = municipalityService.getMunicipalityById(municipalityId);

        Set<IsochroneMatch> allAccessibleRoads = accessibleRoadsService.getBaseAccessibleRoadsByMunicipality(
                accessibilityMap, municipality);
        Set<IsochroneMatch> accessibleRoadsWithRestrictions = accessibleRoadsService
                .getVehicleAccessibleRoadsByMunicipality(accessibilityMap, vehicleProperties, municipality);

        return determineDifference(allAccessibleRoads, accessibleRoadsWithRestrictions);
    }

    private SortedMap<Integer, RoadSection> determineDifference(Set<IsochroneMatch> withoutRestrictions,
            Set<IsochroneMatch> withRestrictions) {
        SortedMap<Integer, RoadSection> roadSections = new TreeMap<>();
        for (IsochroneMatch m : withoutRestrictions) {
            roadSections.computeIfAbsent(m.getMatchedLinkId(), RoadSection::new);
            RoadSection r = roadSections.get(m.getMatchedLinkId());
            // Accessible remains null in case road section is not present in both directions in baseline.
            // This way, non-existing directions of one-way roads (null) can be distinguished from inaccessible
            // directions due to restrictions (false).
            if (m.isReversed()) {
                r.setBackwardAccessible(false);
            } else {
                r.setForwardAccessible(false);
            }
        }
        for (IsochroneMatch m : withRestrictions) {
            RoadSection r = roadSections.get(m.getMatchedLinkId());
            if (m.isReversed()) {
                r.setBackwardAccessible(true);
            } else {
                r.setForwardAccessible(true);
            }
        }

        return roadSections;
    }
}
