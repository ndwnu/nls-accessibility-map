package nu.ndw.nls.accessibilitymap.backend.services;

import io.micrometer.core.annotation.Timed;
import java.util.List;
import nu.ndw.nls.accessibilitymap.backend.graphhopper.AccessibilityMap;
import nu.ndw.nls.accessibilitymap.backend.model.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.backend.model.Municipality;
import nu.ndw.nls.accessibilitymap.backend.model.VehicleProperties;
import nu.ndw.nls.routingmapmatcher.model.IsochroneMatch;
import org.springframework.stereotype.Service;

@Service
public class AccessibleRoadsService {

    @Timed(description = "Time spent determining vehicle accessible road sections")
    public List<IsochroneMatch> getVehicleAccessibleRoadsByMunicipality(AccessibilityMap accessibilityMap,
            VehicleProperties vehicleProperties, Municipality municipality) {
        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .startPoint(municipality.getStartPoint())
                .vehicleProperties(vehicleProperties)
                .municipalityId(municipality.getMunicipalityIdInteger())
                .searchDistanceInMetres(municipality.getSearchDistanceInMetres())
                .build();

        return accessibilityMap.getAccessibleRoadSections(accessibilityRequest);
    }
}
