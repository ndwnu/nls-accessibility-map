package nu.ndw.nls.accessibilitymap.jobs.data.analyser.service;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.service.AccessibilityService;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.jobs.data.analyser.command.dto.AnalyseAsymmetricTrafficSignsConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.data.analyser.service.issue.mapper.IssueMapper;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.api.v1.IssueApiClient;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.api.v1.ReportApiClient;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.CreateIssueJson;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TrafficSignAnalyserService extends IssueReporterService {

    private final AccessibilityService accessibilityService;

    private final IssueMapper issueMapper;

    public TrafficSignAnalyserService(
            IssueApiClient issueApiClient,
            ReportApiClient reportApiClient,
            AccessibilityService accessibilityService,
            IssueMapper issueMapper) {

        super(issueApiClient, reportApiClient);
        this.accessibilityService = accessibilityService;
        this.issueMapper = issueMapper;
    }

    public void analyse(
            NetworkGraphHopper networkGraphHopper,
            @Valid AnalyseAsymmetricTrafficSignsConfiguration analyseAsymmetricTrafficSignsConfiguration) {

        log.info("Analysing with the following properties: {}", analyseAsymmetricTrafficSignsConfiguration);

        Accessibility accessibility = accessibilityService.calculateAccessibility(
                networkGraphHopper,
                analyseAsymmetricTrafficSignsConfiguration.accessibilityRequest());

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
                .filter(DirectionalSegment::hasTrafficSigns)
                .map(directionalSegment -> issueMapper.mapToTrafficSignIssue(directionalSegment, issueReportId, issueReportGroupId))
                .toList();

        logAndReportIssues(issues, analyseAsymmetricTrafficSignsConfiguration.reportIssues(), issueReportId, issueReportGroupId);
    }
}
