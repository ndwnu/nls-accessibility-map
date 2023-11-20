package nu.ndw.nls.accessibilitymap.backend.controllers;

import java.util.Set;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.backend.generated.api.v1.AccessibilityMapApiDelegate;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionsJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.VehicleTypeJson;
import nu.ndw.nls.accessibilitymap.backend.mappers.RequestMapper;
import nu.ndw.nls.accessibilitymap.backend.mappers.ResponseMapper;
import nu.ndw.nls.accessibilitymap.backend.services.AccessibilityMapService;
import nu.ndw.nls.routingmapmatcher.domain.model.IsochroneMatch;
import nu.ndw.nls.routingmapmatcher.domain.model.accessibility.VehicleProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
@PreAuthorize("hasAuthority('NLS-ACCESSIBILITY-MAP-API')")
@RequiredArgsConstructor
public class AccessibilityMapApiDelegateImpl implements AccessibilityMapApiDelegate {

    private final RequestMapper requestMapper;
    private final ResponseMapper responseMapper;
    private final AccessibilityMapService accessibilityMapService;

    @Override
    public ResponseEntity<RoadSectionsJson> getInaccessibleRoadSections(String municipalityId,
            VehicleTypeJson vehicleType, Float vehicleLength, Float vehicleWidth, Float vehicleHeight,
            Float vehicleWeight, Float vehicleAxleWeight, Boolean vehicleHasTrailer) {
        RequestArguments requestArguments = RequestArguments
                .builder()
                .municipalityId(municipalityId)
                .build();
        VehicleProperties vehicleProperties = requestMapper
                .maptoVehicleProperties(requestArguments);
        Set<IsochroneMatch> inaccessibleRoadSections = accessibilityMapService
                .calculateInaccessibleRoadSections(vehicleProperties, municipalityId);
        return ResponseEntity.ok(responseMapper.mapToRoadSectionsJson(inaccessibleRoadSections));
    }

    @Builder
    public record RequestArguments(String municipalityId,
                                   VehicleTypeJson vehicleType, Float vehicleLength, Float vehicleWidth,
                                   Float vehicleHeight,
                                   Float vehicleWeight, Float vehicleAxleWeight, Boolean vehicleHasTrailer) {

    }
}
