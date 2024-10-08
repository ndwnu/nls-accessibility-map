package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.mapper;

import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.dto.GeoGenerationProperties;
import org.springframework.stereotype.Component;

@Component
public final class AccessibilityRequestMapper {

    public AccessibilityRequest map(GeoGenerationProperties geoGenerationProperties) {

        return AccessibilityRequest.builder()
                .vehicleProperties(geoGenerationProperties.getVehicleProperties())
                .startLocationLatitude(geoGenerationProperties.getStartLocationLatitude())
                .startLocationLongitude(geoGenerationProperties.getStartLocationLongitude())
                .searchDistanceInMetres(geoGenerationProperties.getSearchRadiusInMeters())
                .trafficSignType(geoGenerationProperties.getTrafficSignType())
                .includeOnlyTimeWindowedSigns(geoGenerationProperties.isIncludeOnlyTimeWindowedSigns())
                .build();
    }
}
