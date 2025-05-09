package nu.ndw.nls.accessibilitymap.jobs.data.analyser.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.api.v1.IssueApiClient;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.api.v1.ReportApiClient;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.CompleteReportJson;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.CreateIssueJson;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public abstract class IssueReporterService {

    private final IssueApiClient issueApiClient;

    private final ReportApiClient reportApiClient;

    protected void logAndReportIssues(
            List<CreateIssueJson> issues,
            boolean reportIssues,
            String issueReportId,
            String issueReportGroupId) {

        issues.forEach(createIssueJson -> {
            log.debug("Detected traffic sign issue: {}", createIssueJson);

            if (reportIssues) {
                issueApiClient.createIssue(createIssueJson);
                log.info("Reported traffic sign issue: {}", createIssueJson);
            }
        });

        if (reportIssues && !issues.isEmpty()) {
            reportApiClient.reportComplete(CompleteReportJson.builder()
                    .reporterReportId(issueReportId)
                    .reporterReportGroupId(issueReportGroupId)
                    .build());
        }
    }
}
