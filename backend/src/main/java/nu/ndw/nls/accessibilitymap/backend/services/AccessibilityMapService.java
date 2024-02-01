package nu.ndw.nls.accessibilitymap.backend.services;

import com.google.common.base.Stopwatch;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.backend.graphhopper.AccessibilityMap;
import nu.ndw.nls.accessibilitymap.backend.graphhopper.factory.AccessibilityMapFactory;
import nu.ndw.nls.accessibilitymap.backend.model.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.backend.model.Municipality;
import nu.ndw.nls.accessibilitymap.backend.model.RoadSection;
import nu.ndw.nls.accessibilitymap.backend.model.VehicleProperties;
import nu.ndw.nls.routingmapmatcher.model.IsochroneMatch;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccessibilityMapService {

    private final AccessibilityMapFactory accessibilityMapFactory;
    private final NetworkGraphHopper networkGraphHopper;
    private final MunicipalityService municipalityService;
    private final BaseAccessibleRoadsService baseIsochroneService;

    public SortedMap<Integer, RoadSection> determineAccessibilityByRoadSection(VehicleProperties vehicleProperties,
            String municipalityId) {
        AccessibilityMap accessibilityMap = accessibilityMapFactory.createMapMatcher(networkGraphHopper);
        Stopwatch timerAll = Stopwatch.createStarted();
        Municipality municipality = municipalityService.getMunicipalityById(municipalityId);
        Set<IsochroneMatch> allAccessibleRoads = baseIsochroneService.getBaseAccessibleRoadsByMunicipality(
                municipality);
        AccessibilityRequest accessibilityRequest = AccessibilityRequest
                .builder()
                .startPoint(municipality.getStartPoint())
                .vehicleProperties(vehicleProperties)
                .municipalityId(municipality.getMunicipalityIdAsInteger())
                .searchDistanceInMetres(municipality.getSearchDistanceInMetres())
                .build();
        Set<IsochroneMatch> accessibleRoadsWithRestrictions = accessibilityMap.getAccessibleRoadSections(
                accessibilityRequest);
        SortedMap<Integer, RoadSection> idToRoadSectionMap = determineDifference(allAccessibleRoads,
                accessibleRoadsWithRestrictions);

        log.trace("Determining inaccessible roads took {}", timerAll.stop());
        return idToRoadSectionMap;
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
