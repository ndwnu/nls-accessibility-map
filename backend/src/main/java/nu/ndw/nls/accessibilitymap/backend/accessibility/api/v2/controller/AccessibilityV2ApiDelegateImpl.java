package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperService;
import nu.ndw.nls.accessibilitymap.accessibility.service.AccessibilityService;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.mapper.request.AccessibilityRequestMapperV2;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.mapper.response.AccessibilityResponseGeoJsonMapperV2;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.validator.AccessibilityRequestValidator;
import nu.ndw.nls.accessibilitymap.backend.openapi.api.v2.AccessibilityV2ApiDelegate;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.AccessibilityRequestJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.AccessibilityResponseGeoJsonJson;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccessibilityV2ApiDelegateImpl implements AccessibilityV2ApiDelegate {

    private final GraphHopperService graphHopperService;

    private final AccessibilityResponseGeoJsonMapperV2 accessibilityResponseGeoJsonMapperV2;

    private final AccessibilityRequestMapperV2 accessibilityRequestMapperV2;

    private final AccessibilityService accessibilityService;

    private final AccessibilityRequestValidator accessibilityRequestValidator;

    @Override
    public ResponseEntity<AccessibilityResponseGeoJsonJson> getAccessibilityAsGeoJson(
            AccessibilityRequestJson accessibilityRequestJson,
            String acceptEncoding) {

        accessibilityRequestValidator.verify(accessibilityRequestJson);

        Accessibility accessibility = accessibilityService.calculateAccessibility(
                graphHopperService.getNetworkGraphHopper(),
                accessibilityRequestMapperV2.map(accessibilityRequestJson));

        return ResponseEntity.ok(accessibilityResponseGeoJsonMapperV2.map(accessibilityRequestJson, accessibility));
    }
}
