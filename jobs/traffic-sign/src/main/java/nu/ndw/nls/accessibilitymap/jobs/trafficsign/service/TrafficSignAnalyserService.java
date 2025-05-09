package nu.ndw.nls.accessibilitymap.jobs.trafficsign.service;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.services.AccessibilityService;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.jobs.trafficsign.command.dto.AnalyseProperties;
import nu.ndw.nls.accessibilitymap.jobs.trafficsign.service.issue.mapper.IssueMapper;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.api.v1.IssueApiClient;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.api.v1.ReportApiClient;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.CompleteReportJson;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.CreateIssueJson;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrafficSignAnalyserService {

    private final AccessibilityService accessibilityService;

    private final IssueApiClient issueApiClient;

    private final ReportApiClient reportApiClient;

    private final IssueMapper issueMapper;

    public void analyse(NetworkGraphHopper networkGraphHopper, @Valid AnalyseProperties analyseProperties) {

        log.info("Analysing with the following properties: {}", analyseProperties);

        Accessibility accessibility = accessibilityService.calculateAccessibility(
                networkGraphHopper,
                analyseProperties.accessibilityRequest());

        analyseTrafficSigns(accessibility, analyseProperties);
    }

    private void analyseTrafficSigns(Accessibility accessibility, AnalyseProperties analyseProperties) {

        String issueReportId = "Nwb-%s-%s".formatted(analyseProperties.nwbVersion(), UUID.randomUUID());
        String issueReportGroupId = "AsymmetricTrafficSignPlacement-%s".formatted(
                analyseProperties.accessibilityRequest().trafficSignTypes().stream()
                        .map(TrafficSignType::getRvvCode)
                        .sorted()
                        .collect(Collectors.joining("-")));

        List<CreateIssueJson> issues = accessibility.combinedAccessibility()
                .stream()
                .flatMap(roadSection -> roadSection.getRoadSectionFragments().stream())
                .filter(RoadSectionFragment::isPartiallyAccessible)
                .flatMap(roadSectionFragment -> roadSectionFragment.getSegments().stream())
                .filter(DirectionalSegment::hasTrafficSigns)
                .map(directionalSegment -> issueMapper.mapToIssue(directionalSegment, issueReportId, issueReportGroupId))
                .toList();

        issues.forEach(createIssueJson -> {
            log.debug("Detected traffic sign issue: {}", createIssueJson);

            if (analyseProperties.reportIssues()) {
                issueApiClient.createIssue(createIssueJson);
                log.info("Reported traffic sign issue: {}", createIssueJson);
            }
        });

        if (analyseProperties.reportIssues()) {
            reportApiClient.reportComplete(CompleteReportJson.builder()
                    .reporterReportId(issueReportId)
                    .reporterReportGroupId(issueReportGroupId)
                    .build());
        }
    }
}
