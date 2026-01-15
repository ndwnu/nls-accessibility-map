package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.controller;

import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperService;
import nu.ndw.nls.accessibilitymap.accessibility.service.AccessibilityService;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.mapper.request.AccessibilityRequestMapperV2;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.mapper.response.AccessibilityResponseMapperV2;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.validator.AccessibilityRequestValidator;
import nu.ndw.nls.accessibilitymap.generated.api.v2.AccessibilityV2ApiDelegate;
import nu.ndw.nls.accessibilitymap.generated.model.v2.AccessibilityRequestJson;
import nu.ndw.nls.accessibilitymap.generated.model.v2.AccessibilityResponseJson;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AccessibilityV2ApiDelegateImpl implements AccessibilityV2ApiDelegate {

    private final GraphHopperService graphHopperService;

    private final AccessibilityResponseMapperV2 accessibilityResponseMapperV2;

    private final AccessibilityRequestMapperV2 accessibilityRequestMapperV2;

    private final AccessibilityService accessibilityService;

    private final AccessibilityRequestValidator accessibilityRequestValidator;

    public AccessibilityV2ApiDelegateImpl(
            GraphHopperService graphHopperService,
            AccessibilityResponseMapperV2 accessibilityResponseMapperV2,
            AccessibilityRequestMapperV2 accessibilityRequestMapperV2,
            AccessibilityService accessibilityService,
            AccessibilityRequestValidator accessibilityRequestValidator) {
        this.graphHopperService = graphHopperService;
        this.accessibilityResponseMapperV2 = accessibilityResponseMapperV2;
        this.accessibilityRequestMapperV2 = accessibilityRequestMapperV2;
        this.accessibilityService = accessibilityService;
        this.accessibilityRequestValidator = accessibilityRequestValidator;
    }

    @Override
    public ResponseEntity<AccessibilityResponseJson> getAccessibility(AccessibilityRequestJson accessibilityRequestJson) {

        accessibilityRequestValidator.verify(accessibilityRequestJson);

        Accessibility accessibility = accessibilityService.calculateAccessibility(
                graphHopperService.getNetworkGraphHopper(),
                accessibilityRequestMapperV2.map(accessibilityRequestJson));

        return ResponseEntity.ok(accessibilityResponseMapperV2.map(accessibilityRequestJson, accessibility));
    }
}
