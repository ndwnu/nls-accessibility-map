package nu.ndw.nls.accessibilitymap.job.dataanalyser.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import ch.qos.logback.classic.Level;
import java.util.List;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.api.v1.IssueApiClient;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.api.v1.ReportApiClient;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.CompleteReportJson;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.CreateIssueJson;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IssueReporterServiceTest {

    private IssueReporterService issueReporterService;

    @Mock
    private IssueApiClient issueApiClient;

    @Mock
    private ReportApiClient reportApiClient;

    @Mock
    private CreateIssueJson issue;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @BeforeEach
    void setUp() {
        issueReporterService = new IssueReporterService(issueApiClient, reportApiClient) {

        };
    }

    @Test
    void logAndReportIssues() {
        List<CreateIssueJson> issues = List.of(issue);

        issueReporterService.logAndReportIssues(issues, true, "reportId", "reportGroupId");

        verify(issueApiClient).createIssue(issue);
        verify(reportApiClient).reportComplete(CompleteReportJson.builder()
                .reporterReportId("reportId")
                .reporterReportGroupId("reportGroupId")
                .build());
        loggerExtension.containsLog(Level.DEBUG, "Reported traffic sign issue: issue");
    }

    @Test
    void logAndReportIssues_noIssues() {
        List<CreateIssueJson> issues = List.of();

        issueReporterService.logAndReportIssues(issues, true, "reportId", "reportGroupId");

        verify(issueApiClient, never()).createIssue(any());
        verify(reportApiClient, never()).reportComplete(any());
    }

    @Test
    void logAndReportIssues_reportNone() {
        List<CreateIssueJson> issues = List.of(issue);

        issueReporterService.logAndReportIssues(issues, false, "reportId", "reportGroupId");

        verify(issueApiClient, never()).createIssue(any());
        verify(reportApiClient, never()).reportComplete(any());
    }
}
