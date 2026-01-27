package nu.ndw.nls.accessibilitymap.jobs.data.analyser.service;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.Accessibility;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.service.AccessibilityService;
import nu.ndw.nls.accessibilitymap.jobs.data.analyser.command.dto.AnalyseAsymmetricTrafficSignsConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.data.analyser.service.issue.mapper.IssueBuilder;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.api.v1.IssueApiClient;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.api.v1.ReportApiClient;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.CreateIssueJson;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TrafficSignAnalyserService extends IssueReporterService {

    private final AccessibilityService accessibilityService;

    private final IssueBuilder issueBuilder;

    public TrafficSignAnalyserService(
            IssueApiClient issueApiClient,
            ReportApiClient reportApiClient,
            AccessibilityService accessibilityService,
            IssueBuilder issueBuilder) {

        super(issueApiClient, reportApiClient);
        this.accessibilityService = accessibilityService;
        this.issueBuilder = issueBuilder;
    }

    public void analyse(@Valid AnalyseAsymmetricTrafficSignsConfiguration analyseAsymmetricTrafficSignsConfiguration) {

        log.info("Analysing with the following properties: {}", analyseAsymmetricTrafficSignsConfiguration);

        var accessibility = accessibilityService.calculateAccessibility(analyseAsymmetricTrafficSignsConfiguration.accessibilityRequest());

        analyseTrafficSigns(accessibility, analyseAsymmetricTrafficSignsConfiguration);
    }

    private void analyseTrafficSigns(
            Accessibility accessibility,
            AnalyseAsymmetricTrafficSignsConfiguration analyseAsymmetricTrafficSignsConfiguration) {

        String issueReportId = "Nwb-%s-%s".formatted(analyseAsymmetricTrafficSignsConfiguration.nwbVersion(), UUID.randomUUID());
        String issueReportGroupId = "AsymmetricTrafficSignPlacement-%s".formatted(
                analyseAsymmetricTrafficSignsConfiguration.accessibilityRequest().trafficSignTypes().stream()
                        .map(TrafficSignType::getRvvCode)
                        .sorted()
                        .collect(Collectors.joining("-")));

        List<CreateIssueJson> issues = accessibility.combinedAccessibility()
                .stream()
                .flatMap(roadSection -> roadSection.getRoadSectionFragments().stream())
                .filter(RoadSectionFragment::isPartiallyAccessible)
                .flatMap(roadSectionFragment -> roadSectionFragment.getSegments().stream())
                .filter(DirectionalSegment::hasRestrictions)
                .map(directionalSegment -> issueBuilder.buildTrafficSignIssue(directionalSegment, issueReportId, issueReportGroupId))
                .toList();

        logAndReportIssues(issues, analyseAsymmetricTrafficSignsConfiguration.reportIssues(), issueReportId, issueReportGroupId);
    }
}
