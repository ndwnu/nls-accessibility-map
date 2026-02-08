package nu.ndw.nls.accessibilitymap.job.dataanalyser.service;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.Accessibility;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.network.NetworkDataService;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.service.AccessibilityService;
import nu.ndw.nls.accessibilitymap.job.dataanalyser.command.dto.AnalyseAsymmetricTrafficSignsConfiguration;
import nu.ndw.nls.accessibilitymap.job.dataanalyser.service.issue.mapper.IssueBuilder;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.api.v1.IssueApiClient;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.api.v1.ReportApiClient;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.CreateIssueJson;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TrafficSignAnalyserService extends IssueReporterService {

    private final AccessibilityService accessibilityService;

    private final NetworkDataService networkDataService;

    private final IssueBuilder issueBuilder;

    public TrafficSignAnalyserService(
            IssueApiClient issueApiClient,
            ReportApiClient reportApiClient,
            AccessibilityService accessibilityService,
            NetworkDataService networkDataService,
            IssueBuilder issueBuilder) {

        super(issueApiClient, reportApiClient);
        this.accessibilityService = accessibilityService;
        this.networkDataService = networkDataService;
        this.issueBuilder = issueBuilder;
    }

    public void analyse(@Valid AnalyseAsymmetricTrafficSignsConfiguration analyseAsymmetricTrafficSignsConfiguration) {

        log.info("Analysing with the following properties: {}", analyseAsymmetricTrafficSignsConfiguration);

        NetworkData networkData = networkDataService.get();
        var accessibility = accessibilityService.calculateAccessibility(
                networkData,
                analyseAsymmetricTrafficSignsConfiguration.accessibilityRequest());

        analyseTrafficSigns(accessibility, networkData, analyseAsymmetricTrafficSignsConfiguration);
    }

    private void analyseTrafficSigns(
            Accessibility accessibility,
            NetworkData networkData,
            AnalyseAsymmetricTrafficSignsConfiguration analyseAsymmetricTrafficSignsConfiguration) {

        String issueReportId = "Nwb-%s-%s".formatted(networkData.getNwbVersion(), UUID.randomUUID());
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
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        logAndReportIssues(issues, analyseAsymmetricTrafficSignsConfiguration.reportIssues(), issueReportId, issueReportGroupId);
    }
}
