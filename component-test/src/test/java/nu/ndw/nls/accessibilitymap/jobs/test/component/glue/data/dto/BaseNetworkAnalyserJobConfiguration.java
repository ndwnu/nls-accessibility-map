package nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto;

import lombok.Builder;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper.dto.Node;

@Builder
public record BaseNetworkAnalyserJobConfiguration(
        Node startNode,
        boolean reportIssues) {

}
