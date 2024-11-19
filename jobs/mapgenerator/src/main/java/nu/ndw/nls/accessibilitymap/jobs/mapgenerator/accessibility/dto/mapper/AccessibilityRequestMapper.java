package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.mapper;

import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.dto.ExportProperties;
import org.springframework.stereotype.Component;

@Component
public final class AccessibilityRequestMapper {

    public AccessibilityRequest map(ExportProperties exportProperties) {

        return AccessibilityRequest.builder()
                .vehicleProperties(exportProperties.vehicleProperties())
                .startLocationLatitude(exportProperties.startLocationLatitude())
                .startLocationLongitude(exportProperties.startLocationLongitude())
                .searchRadiusInMeters(exportProperties.generateConfiguration().searchRadiusInMeters())
                .trafficSignTypes(exportProperties.trafficSignTypes())
                .includeOnlyTimeWindowedSigns(exportProperties.includeOnlyTimeWindowedSigns())
                .build();
    }
}
