package nu.ndw.nls.accessibilitymap.backend.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.backend.generated.api.v1.AccessibilityMapApiDelegate;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.MapTypeJson;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@PreAuthorize("hasAuthority('NLS-ACCESSIBILITY-MAP-API')")
@RequiredArgsConstructor
public class AccessibilityMapApiDelegateImpl implements AccessibilityMapApiDelegate {

    @Override
    public ResponseEntity<String> testEndpoint(MapTypeJson map) {
        return ResponseEntity.ok("Test working");
    }
}
