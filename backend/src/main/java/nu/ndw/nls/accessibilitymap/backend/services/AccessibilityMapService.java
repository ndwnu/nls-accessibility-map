package nu.ndw.nls.accessibilitymap.backend.services;

import com.google.common.collect.Sets;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.backend.model.Municipality;
import nu.ndw.nls.routingmapmatcher.domain.AccessibilityMap;
import nu.ndw.nls.routingmapmatcher.domain.MapMatcherFactory;
import nu.ndw.nls.routingmapmatcher.domain.model.IsochroneMatch;
import nu.ndw.nls.routingmapmatcher.domain.model.accessibility.AccessibilityRequest;
import nu.ndw.nls.routingmapmatcher.domain.model.accessibility.VehicleProperties;
import nu.ndw.nls.routingmapmatcher.graphhopper.NetworkGraphHopper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessibilityMapService {

    private final MapMatcherFactory<AccessibilityMap> accessibilityMapFactory;
    private final NetworkGraphHopper networkGraphHopper;
    private final MunicipalityService municipalityService;
    private final BaseAccessibleRoadsService baseIsochroneService;

    public Set<IsochroneMatch> calculateInaccessibleRoadSections(VehicleProperties vehicleProperties,
            String municipalityId) {
        AccessibilityMap accessibilityMap = accessibilityMapFactory
                .createMapMatcher(networkGraphHopper);
        Municipality municipality = municipalityService.getMunicipalityById(municipalityId);
        Set<IsochroneMatch> noRestrictions = baseIsochroneService.getBaseAccessibleRoadsByMunicipality(municipality);
        AccessibilityRequest accessibilityRequest = AccessibilityRequest
                .builder()
                .startPoint(municipality.getStartPoint())
                .vehicleProperties(vehicleProperties)
                .municipalityId(municipality.municipalityIdAsInteger())
                .searchDistanceInMetres(municipality.getSearchDistanceInMetres())
                .build();
        Set<IsochroneMatch> withRestrictions = accessibilityMap.getAccessibleRoadSections(accessibilityRequest);
        return Sets.difference(noRestrictions, withRestrictions);
    }
}