package nu.ndw.nls.accessibilitymap.accessibility.services;

import io.micrometer.core.annotation.Timed;

import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.AccessibilityMap;
import nu.ndw.nls.accessibilitymap.accessibility.model.Municipality;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import nu.ndw.nls.accessibilitymap.accessibility.model.AccessibilityRequest;
import nu.ndw.nls.routingmapmatcher.model.IsochroneMatch;
import org.locationtech.jts.geom.Point;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class AccessibleRoadsService {

    @Cacheable(key = "#municipality.municipalityId", cacheNames = "baseAccessibleRoadsByMunicipality", sync = true)
    @Timed(description = "Time spent determining base accessible road sections")
    public List<IsochroneMatch> getBaseAccessibleRoads(AccessibilityMap accessibilityMap,  Point startPoint,
            double searchDistanceInMeters ) {
        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .startPoint(startPoint)
                .searchDistanceInMetres(searchDistanceInMeters)
                .build();

        return accessibilityMap.getAccessibleRoadSections(accessibilityRequest);
    }

    @Cacheable(key = "#municipality.municipalityId", cacheNames = "baseAccessibleRoadsByMunicipality", sync = true)
    @Timed(description = "Time spent determining base accessible road sections")
    public List<IsochroneMatch> getBaseAccessibleRoadsByMunicipality(AccessibilityMap accessibilityMap,
            Municipality municipality) {
        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .startPoint(municipality.getStartPoint())
                .municipalityId(municipality.getMunicipalityIdInteger())
                .searchDistanceInMetres(municipality.getSearchDistanceInMetres())
                .build();

        return accessibilityMap.getAccessibleRoadSections(accessibilityRequest);
    }

    @Timed(description = "Time spent determining vehicle accessible road sections within municipality")
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

    @Timed(description = "Time spent determining vehicle accessible road sections within entire map")
    public List<IsochroneMatch> getVehicleAccessibleRoads(AccessibilityMap accessibilityMap,
            VehicleProperties vehicleProperties, Point startPoint, double searchDistanceInMeters) {
        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .startPoint(startPoint)
                .vehicleProperties(vehicleProperties)
                .searchDistanceInMetres(searchDistanceInMeters)
                .build();

        return accessibilityMap.getAccessibleRoadSections(accessibilityRequest);
    }

}
