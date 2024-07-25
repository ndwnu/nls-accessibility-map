package nu.ndw.nls.accessibilitymap.backend.services;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.backend.graphhopper.AccessibilityMap;
import nu.ndw.nls.accessibilitymap.backend.graphhopper.factory.AccessibilityMapFactory;
import nu.ndw.nls.accessibilitymap.backend.model.Municipality;
import nu.ndw.nls.accessibilitymap.backend.model.RoadSection;
import nu.ndw.nls.accessibilitymap.backend.model.VehicleProperties;
import nu.ndw.nls.accessibilitymap.backend.municipality.mappers.MunicipalityIdMapper;
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
    private final CachedMunicipalityRoadSectionsService cachedMunicipalityRoadSectionsService;
    private final MunicipalityIdMapper municipalityIdMapper;


    public SortedMap<Integer, RoadSection> determineAccessibilityByRoadSection(VehicleProperties vehicleProperties,
            String municipalityId, boolean newMethod) {
        AccessibilityMap accessibilityMap = accessibilityMapFactory.createMapMatcher(networkGraphHopper);
        Municipality municipality = municipalityService.getMunicipalityById(municipalityId);

        List<IsochroneMatch> allAccessibleRoads = accessibleRoadsService.getBaseAccessibleRoadsByMunicipality(
                accessibilityMap, municipality);
        List<IsochroneMatch> accessibleRoadsWithRestrictions = accessibleRoadsService
                .getVehicleAccessibleRoadsByMunicipality(accessibilityMap, vehicleProperties, municipality);


        if (newMethod) {
            return determineDifference(accessibleRoadsWithRestrictions, municipalityIdMapper.map(municipalityId));
        } else {
            return determineDifference(allAccessibleRoads, accessibleRoadsWithRestrictions);
        }
    }

    private SortedMap<Integer, RoadSection> determineDifference(List<IsochroneMatch> withoutRestrictions,
            List<IsochroneMatch> withRestrictions) {
        SortedMap<Integer, RoadSection> roadSections = new TreeMap<>();
        for (IsochroneMatch m : withoutRestrictions) {
            roadSections.computeIfAbsent(m.getMatchedLinkId(), i -> new RoadSection(i,
                    m.isReversed() ? m.getGeometry().reverse() : m.getGeometry()));
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

    private SortedMap<Integer, RoadSection> determineDifference(List<IsochroneMatch> withRestrictions,
            int municipalityId) {

        // Accessible remains null in case road section is not present in both directions in baseline.
        // This way, non-existing directions of one-way roads (null) can be distinguished from inaccessible
        // directions due to restrictions (false).
        SortedMap<Integer, RoadSection> roadSections = cachedMunicipalityRoadSectionsService.getRoadSectionIdToRoadSection(
                        municipalityId)
                .stream()
                .map(cachedRoadSection -> new RoadSection(cachedRoadSection.getRoadSectionId(),
                        cachedRoadSection.getGeometry(), cachedRoadSection.getForwardAccessible(),
                        cachedRoadSection.getBackwardAccessible()))
                .collect(Collectors.toMap(RoadSection::getRoadSectionId, Function.identity(),
                        (a, b) -> a,
                        TreeMap::new));

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
