package nu.ndw.nls.accessibilitymap.jobs.data.analyser.service;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.Accessibility;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.service.AccessibilityService;
import nu.ndw.nls.accessibilitymap.jobs.data.analyser.command.dto.AnalyseNetworkConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.data.analyser.service.issue.mapper.IssueBuilder;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.api.v1.IssueApiClient;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.api.v1.ReportApiClient;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.CreateIssueJson;
import nu.ndw.nls.springboot.core.time.ClockService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NetworkAnalyserService extends IssueReporterService {

    private static final String ISSUE_REPORT_GROUP_ID = "UnreachableNetworkSegments";

    private final IssueBuilder issueBuilder;

    private final AccessibilityService accessibilityService;

    private final ClockService clockService;

    public NetworkAnalyserService(
            IssueApiClient issueApiClient,
            ReportApiClient reportApiClient,
            IssueBuilder issueBuilder,
            AccessibilityService accessibilityService,
            ClockService clockService) {

        super(issueApiClient, reportApiClient);

        this.issueBuilder = issueBuilder;
        this.accessibilityService = accessibilityService;
        this.clockService = clockService;
    }

    public void analyse(@Valid AnalyseNetworkConfiguration analyseNetworkConfiguration) {

        log.info("Analysing with the following properties: {}", analyseNetworkConfiguration);

        Accessibility accessibility = accessibilityService.calculateAccessibility(AccessibilityRequest.builder()
                .timestamp(clockService.now())
                .searchRadiusInMeters(analyseNetworkConfiguration.searchRadiusInMeters())
                .startLocationLatitude(analyseNetworkConfiguration.startLocationLatitude())
                .startLocationLongitude(analyseNetworkConfiguration.startLocationLongitude())
                .addMissingRoadsSectionsFromNwb(true)
                .build());

        String issueReportId = "Nwb-%s-%s".formatted(analyseNetworkConfiguration.nwbVersion(), UUID.randomUUID());
        List<CreateIssueJson> issues = accessibility.unroutableRoadSections().stream()
                .map(missingRoadSection -> issueBuilder.buildUnroutableNetworkIssue(
                        missingRoadSection,
                        analyseNetworkConfiguration.nwbVersion(),
                        issueReportId,
                        ISSUE_REPORT_GROUP_ID))
                .toList();
        logAndReportIssues(issues, analyseNetworkConfiguration.reportIssues(), issueReportId, ISSUE_REPORT_GROUP_ID);
    }
}
