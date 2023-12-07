package nu.ndw.nls.accessibilitymap.backend.controllers;

import java.util.List;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.backend.exceptions.VehicleWeightRequiredException;
import nu.ndw.nls.accessibilitymap.backend.generated.api.v1.AccessibilityMapApiDelegate;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionsJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.VehicleTypeJson;
import nu.ndw.nls.accessibilitymap.backend.mappers.RequestMapper;
import nu.ndw.nls.accessibilitymap.backend.mappers.ResponseMapper;
import nu.ndw.nls.accessibilitymap.backend.model.RoadSection;
import nu.ndw.nls.accessibilitymap.backend.services.AccessibilityMapService;
import nu.ndw.nls.routingmapmatcher.domain.model.accessibility.VehicleProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessibilityMapApiDelegateImpl implements AccessibilityMapApiDelegate {

    private final RequestMapper requestMapper;
    private final ResponseMapper responseMapper;
    private final AccessibilityMapService accessibilityMapService;

    @Override
    public ResponseEntity<RoadSectionsJson> getInaccessibleRoadSections(String municipalityId,
            VehicleTypeJson vehicleType, Float vehicleLength, Float vehicleWidth, Float vehicleHeight,
            Float vehicleWeight, Float vehicleAxleLoad, Boolean vehicleHasTrailer) {
        checkWeightConstraint(vehicleType, vehicleWeight);
        VehicleArguments requestArguments = new VehicleArguments(vehicleType, vehicleLength, vehicleWidth,
                vehicleHeight, vehicleWeight, vehicleAxleLoad, vehicleHasTrailer == Boolean.TRUE);
        VehicleProperties vehicleProperties = requestMapper.mapToVehicleProperties(requestArguments);
        List<RoadSection> inaccessibleRoadSections = accessibilityMapService
                .determineInaccessibleRoadSections(vehicleProperties, municipalityId);
        return ResponseEntity.ok(responseMapper.mapToRoadSectionsJson(inaccessibleRoadSections));
    }

    private static void checkWeightConstraint(VehicleTypeJson vehicleType, Float vehicleWeight) {
        if (VehicleTypeJson.COMMERCIAL_VEHICLE == vehicleType && vehicleWeight == null) {
            throw new VehicleWeightRequiredException("When selecting 'commercial_vehicle' as vehicle type "
                    + "vehicle weight is required");
        }
    }

    @Builder
    public record VehicleArguments(VehicleTypeJson vehicleType, Float vehicleLength, Float vehicleWidth,
                                   Float vehicleHeight, Float vehicleWeight, Float vehicleAxleLoad,
                                   boolean vehicleHasTrailer) {

    }
}
