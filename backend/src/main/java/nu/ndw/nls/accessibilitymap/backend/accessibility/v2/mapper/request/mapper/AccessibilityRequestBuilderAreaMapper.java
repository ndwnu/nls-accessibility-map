package nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.request.mapper;

import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest.AccessibilityRequestBuilder;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.AreaRequestJson;

public interface AccessibilityRequestBuilderAreaMapper {

    void build(AccessibilityRequestBuilder accessibilityRequestBuilder, AreaRequestJson areaRequestJson);

    boolean canProcessAreaRequest(AreaRequestJson areaRequestJson);
}
