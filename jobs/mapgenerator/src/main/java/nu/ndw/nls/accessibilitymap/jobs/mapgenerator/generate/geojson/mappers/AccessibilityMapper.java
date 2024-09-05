package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;

import org.springframework.stereotype.Component;

@Component
public class AccessibilityMapper {

    /**
     * Turns our tri-state: - null: not accessible in unrestricted isochrone - false: not accessible due to traffic sign
     * restrictions - true: acessible into the true if it is accessible
     *
     * @param triStateAccessibility tri-state boolean
     * @return true if it is accessible
     */
    public boolean map(Boolean triStateAccessibility) {
        return Boolean.TRUE.equals(triStateAccessibility);
    }
}
