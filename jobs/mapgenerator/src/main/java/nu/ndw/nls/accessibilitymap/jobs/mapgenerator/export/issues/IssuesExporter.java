package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.issues;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.dto.ExportProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.ExportType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.Exporter;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.issues.mappers.IssueMapper;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.api.v1.IssueApiClient;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.IssueJson;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IssuesExporter implements Exporter {

    private final IssueApiClient issueApiClient;

    private final IssueMapper issueMapper;

    @Override
    public void export(Accessibility accessibility, ExportProperties exportProperties) {

        accessibility.combinedAccessibility().stream()
                .flatMap(roadSection -> roadSection.getRoadSectionFragments().stream())
                .filter(RoadSectionFragment::isPartiallyAccessible)
                .flatMap(roadSectionFragment -> roadSectionFragment.getSegments().stream())
                .filter(DirectionalSegment::hasTrafficSign)
                .map(issueMapper::mapToIssue)
                .forEach(issueJson -> {
                    ResponseEntity<IssueJson> response = issueApiClient.createIssue(issueJson);
                    if (!response.getStatusCode().is2xxSuccessful()) {
                        throw new IllegalStateException("Failed to create issue %s"
                                .formatted(response.getBody()));
                    }
                });
    }

    @Override
    public boolean isEnabled(Set<ExportType> exportTypes) {
        return exportTypes.contains(ExportType.ASYMMETRIC_TRAFFIC_SIGNS_ISSUES);
    }
}
