package nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto;

import java.util.Set;
import lombok.Builder;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper.dto.Node;

@Builder
public record JobConfiguration(
        String exportName,
        Set<String> trafficSignTypes,
        Set<String> exportTypes,
        Node startNode,
        boolean includeOnlyWindowSigns,
        boolean publishEvents,
        Double polygonMaxDistanceBetweenPoints) {

}
