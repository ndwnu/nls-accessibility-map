package nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto;

import java.util.List;
import java.util.Set;
import lombok.Builder;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper.dto.Node;

@Builder
public record TrafficSignAnalyserJobConfiguration(
        List<Set<String>> trafficSignGroups,
        Node startNode,
        boolean reportIssues) {

}
