package nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto;

import lombok.Builder;
import nu.ndw.nls.springboot.test.graph.dto.Node;

@Builder
public record BaseNetworkAnalyserJobConfiguration(
        Node startNode,
        boolean reportIssues) {

}
