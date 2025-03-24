package nu.ndw.nls.accessibilitymap.jobs.trafficsignanalyser.service;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.services.accessibility.AccessibilityService;
import nu.ndw.nls.accessibilitymap.accessibility.services.accessibility.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.jobs.trafficsignanalyser.command.dto.AnalyseProperties;
import nu.ndw.nls.accessibilitymap.jobs.trafficsignanalyser.service.issue.mapper.IssueMapper;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.api.v1.IssueApiClient;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.api.v1.ReportApiClient;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.CompleteReportJson;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.CreateIssueJson;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrafficSignAnalyserService {

    private final AccessibilityService accessibilityService;

    private final IssueApiClient issueApiClient;

    private final ReportApiClient reportApiClient;

    private final IssueMapper issueMapper;

    public void analyse(@Valid AnalyseProperties analyseProperties) {

        log.info("Analysing with the following properties: {}", analyseProperties);

        Accessibility accessibility = accessibilityService.calculateAccessibility(analyseProperties.accessibilityRequest(), false);

        analyseTrafficSigns(accessibility, analyseProperties);
    }

    private void analyseTrafficSigns(Accessibility accessibility, AnalyseProperties analyseProperties) {

        String issueReportId = "Nwb-%s-%s".formatted(analyseProperties.nwbVersion(), UUID.randomUUID());
        String issueReportGroupId = "AsymmetricTrafficSignPlacement-%s".formatted(
                analyseProperties.trafficSignTypes().stream()
                        .map(TrafficSignType::getRvvCode)
                        .collect(Collectors.joining("-")));

        List<CreateIssueJson> issues = accessibility.combinedAccessibility()
                .stream()
                .flatMap(roadSection -> roadSection.getRoadSectionFragments().stream())
                .filter(RoadSectionFragment::isPartiallyAccessible)
                .flatMap(roadSectionFragment -> roadSectionFragment.getSegments().stream())
                .filter(DirectionalSegment::hasTrafficSign)
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
