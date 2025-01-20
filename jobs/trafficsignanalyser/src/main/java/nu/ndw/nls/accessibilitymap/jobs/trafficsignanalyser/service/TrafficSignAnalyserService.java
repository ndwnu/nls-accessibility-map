package nu.ndw.nls.accessibilitymap.jobs.trafficsignanalyser.service;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.accessibility.services.accessibility.AccessibilityService;
import nu.ndw.nls.accessibilitymap.accessibility.services.accessibility.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.jobs.trafficsignanalyser.command.dto.AnalyseProperties;
import nu.ndw.nls.accessibilitymap.jobs.trafficsignanalyser.service.issue.mapper.IssueMapper;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.api.v1.IssueApiClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrafficSignAnalyserService {

    private final AccessibilityService accessibilityService;

    private final AccessibilityRequestMapper accessibilityRequestMapper;

    private final IssueApiClient issueApiClient;

    private final IssueMapper issueMapper;

    public void analyse(@Valid AnalyseProperties analyseProperties) {

        log.info("Analysing with the following properties: {}", analyseProperties);

        Accessibility accessibility = accessibilityService.calculateAccessibility(accessibilityRequestMapper.map(analyseProperties));

        analyseTrafficSigns(accessibility, analyseProperties);
    }

    private void analyseTrafficSigns(Accessibility accessibility, AnalyseProperties analyseProperties) {

        accessibility.combinedAccessibility().stream()
                .flatMap(roadSection -> roadSection.getRoadSectionFragments().stream())
                .filter(RoadSectionFragment::isPartiallyAccessible)
                .flatMap(roadSectionFragment -> roadSectionFragment.getSegments().stream())
                .filter(DirectionalSegment::hasTrafficSign)
                .map(directionalSegment -> issueMapper.mapToIssue(
                        directionalSegment,
                        "Nwb%s-%s".formatted(analyseProperties.nwbVersion(), UUID.randomUUID())))
                .forEach(createIssueJson -> {
                    log.info("Detected traffic sign issue: {}", createIssueJson);

                    if (analyseProperties.reportIssues()) {
                        issueApiClient.createIssue(createIssueJson);
                        log.info("Reported traffic sign issue: {}", createIssueJson);
                    }
                });
    }
}
