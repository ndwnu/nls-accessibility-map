package nu.ndw.nls.accessibilitymap.backend.controllers;

import static java.util.Collections.emptyList;

import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.backend.generated.api.v1.AccessibilityMapApiDelegate;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionsJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.VehicleTypeJson;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
@PreAuthorize("hasAuthority('NLS-ACCESSIBILITY-MAP-API')")
@RequiredArgsConstructor
public class AccessibilityMapApiDelegateImpl implements AccessibilityMapApiDelegate {

    @Override
    public ResponseEntity<RoadSectionsJson> getInaccessibleRoadSections(String municipalityId,
            VehicleTypeJson vehicleType, Float vehicleLength, Float vehicleWidth, Float vehicleHeight,
            Float vehicleWeight, Float vehicleAxleWeight, Boolean vehicleHasTrailer) {
        return ResponseEntity.ok(new RoadSectionsJson()
                .inaccessibleRoadSections(emptyList()));
    }
}
