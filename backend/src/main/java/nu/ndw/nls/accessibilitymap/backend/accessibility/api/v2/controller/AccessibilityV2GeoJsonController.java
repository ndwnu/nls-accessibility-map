package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.controller;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperService;
import nu.ndw.nls.accessibilitymap.accessibility.service.AccessibilityService;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.dto.AccessibilityGeoJsonResponse;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.mapper.request.AccessibilityRequestMapperV2;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.mapper.response.AccessibilityResponseGeoJsonMapperV2;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.validator.AccessibilityRequestValidator;
import nu.ndw.nls.accessibilitymap.generated.model.v2.AccessibilityRequestJson;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("${openapi.accessibilityMapV2.base-path:/api/rest/static-road-data/accessibility-map/v2}")
public class AccessibilityV2GeoJsonController {

    private static final String PATH_GET_ACCESSIBILITY_AS_GEO_JSON = "/accessiblility.geojson";

    private final GraphHopperService graphHopperService;

    private final AccessibilityResponseGeoJsonMapperV2 accessibilityResponseGeoJsonMapperV2;

    private final AccessibilityRequestMapperV2 accessibilityRequestMapperV2;

    private final AccessibilityService accessibilityService;

    private final AccessibilityRequestValidator accessibilityRequestValidator;

    @PostMapping(
            value = PATH_GET_ACCESSIBILITY_AS_GEO_JSON,
            produces = {"application/geo+json", "application/json"},
            consumes = {"application/json"}
    )
    public ResponseEntity<AccessibilityGeoJsonResponse> getAccessibilityAsGeoJson(
            @Parameter(name = "AccessibilityRequestJson", required = true)
            @Valid
            @RequestBody AccessibilityRequestJson accessibilityRequestJson
    ) {
        accessibilityRequestValidator.verify(accessibilityRequestJson);

        Accessibility accessibility = accessibilityService.calculateAccessibility(
                graphHopperService.getNetworkGraphHopper(),
                accessibilityRequestMapperV2.map(accessibilityRequestJson));

        return ResponseEntity.ok(accessibilityResponseGeoJsonMapperV2.map(accessibilityRequestJson, accessibility));
    }
}
