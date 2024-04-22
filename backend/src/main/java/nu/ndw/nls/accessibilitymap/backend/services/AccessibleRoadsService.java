package nu.ndw.nls.accessibilitymap.backend.services;

import io.micrometer.core.annotation.Timed;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.backend.graphhopper.AccessibilityMap;
import nu.ndw.nls.accessibilitymap.backend.model.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.backend.model.Municipality;
import nu.ndw.nls.accessibilitymap.backend.model.VehicleProperties;
import nu.ndw.nls.routingmapmatcher.model.IsochroneMatch;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class AccessibleRoadsService {

    @Cacheable(key = "#municipality.municipalityId", cacheNames = "baseAccessibleRoadsByMunicipality", sync = true)
    @Timed(description = "Time spent determining base accessible road sections")
    public Set<IsochroneMatch> getBaseAccessibleRoadsByMunicipality(AccessibilityMap accessibilityMap,
            Municipality municipality) {
        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .startPoint(municipality.getStartPoint())
                .municipalityId(municipality.getMunicipalityIdAsInteger())
                .searchDistanceInMetres(municipality.getSearchDistanceInMetres())
                .build();

        return accessibilityMap.getAccessibleRoadSections(accessibilityRequest);
    }

    @Timed(description = "Time spent determining vehicle accessible road sections")
    public Set<IsochroneMatch> getVehicleAccessibleRoadsByMunicipality(AccessibilityMap accessibilityMap,
            VehicleProperties vehicleProperties, Municipality municipality) {
        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .startPoint(municipality.getStartPoint())
                .vehicleProperties(vehicleProperties)
                .municipalityId(municipality.getMunicipalityIdAsInteger())
                .searchDistanceInMetres(municipality.getSearchDistanceInMetres())
                .build();

        return accessibilityMap.getAccessibleRoadSections(accessibilityRequest);
    }
}
