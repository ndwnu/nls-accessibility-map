package nu.ndw.nls.accessibilitymap.jobs.trafficsignanalyser.service;

import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.services.accessibility.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.jobs.trafficsignanalyser.command.dto.AnalyseProperties;
import org.springframework.stereotype.Component;

@Component
public final class AccessibilityRequestMapper {

    public AccessibilityRequest map(AnalyseProperties analyseProperties) {

        return AccessibilityRequest.builder()
                .vehicleProperties(analyseProperties.vehicleProperties())
                .startLocationLatitude(analyseProperties.startLocationLatitude())
                .startLocationLongitude(analyseProperties.startLocationLongitude())
                .searchRadiusInMeters(analyseProperties.searchRadiusInMeters())
                .trafficSignTypes(List.of(analyseProperties.trafficSignType()))
                .includeOnlyTimeWindowedSigns(false)
                .build();
    }
}
