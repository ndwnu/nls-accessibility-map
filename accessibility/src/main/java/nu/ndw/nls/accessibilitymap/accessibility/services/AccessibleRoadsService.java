package nu.ndw.nls.accessibilitymap.accessibility.services;

import io.micrometer.core.annotation.Timed;
import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.AccessibilityMap;
import nu.ndw.nls.accessibilitymap.accessibility.model.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import nu.ndw.nls.routingmapmatcher.model.IsochroneMatch;
import org.locationtech.jts.geom.Point;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class AccessibleRoadsService {

    @Timed(description = "Time spent determining base accessible road sections for entire map")
    public List<IsochroneMatch> getBaseAccessibleRoads(AccessibilityMap accessibilityMap, Point startPoint,
            double searchDistanceInMeters) {
        return getVehicleAccessibleRoads(accessibilityMap, null, startPoint, searchDistanceInMeters, null);
    }

    @Cacheable(key = "#municipalityId", cacheNames = "baseAccessibleRoadsByMunicipality", sync = true)
    @Timed(description = "Time spent determining base accessible road sections within municipality")
    public List<IsochroneMatch> getBaseAccessibleRoadsByMunicipality(AccessibilityMap accessibilityMap,
            Point startPoint, double searchDistanceInMeters, int municipalityId) {
        return getVehicleAccessibleRoads(accessibilityMap, null, startPoint, searchDistanceInMeters, municipalityId);
    }

    @Timed(description = "Time spent determining vehicle accessible road sections within municipality")
    public List<IsochroneMatch> getVehicleAccessibleRoadsByMunicipality(AccessibilityMap accessibilityMap,
            VehicleProperties vehicleProperties, Point startPoint, double searchDistanceInMeters,
            int municipalityId) {
        return getVehicleAccessibleRoads(accessibilityMap, vehicleProperties, startPoint, searchDistanceInMeters,
                municipalityId);
    }

    @Timed(description = "Time spent determining vehicle accessible road sections within entire map")
    public List<IsochroneMatch> getVehicleAccessibleRoads(AccessibilityMap accessibilityMap,
            VehicleProperties vehicleProperties, Point startPoint, double searchDistanceInMeters) {
        return getVehicleAccessibleRoads(accessibilityMap, vehicleProperties, startPoint, searchDistanceInMeters,
                null);
    }

    private List<IsochroneMatch> getVehicleAccessibleRoads(AccessibilityMap accessibilityMap,
            @Nullable VehicleProperties vehicleProperties, Point startPoint, double searchDistanceInMeters,
            @Nullable Integer municipalityId) {
        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .startPoint(startPoint)
                .vehicleProperties(vehicleProperties)
                .searchDistanceInMetres(searchDistanceInMeters)
                .municipalityId(municipalityId)
                .build();

        return accessibilityMap.getAccessibleRoadSections(accessibilityRequest);
    }

}
