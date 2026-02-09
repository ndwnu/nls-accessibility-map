package nu.ndw.nls.accessibilitymap.job.dataanalyser.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.Accessibility;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.network.NetworkDataService;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.service.AccessibilityService;
import nu.ndw.nls.accessibilitymap.job.dataanalyser.command.dto.AnalyseNetworkConfiguration;
import nu.ndw.nls.accessibilitymap.job.dataanalyser.service.issue.mapper.IssueBuilder;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.api.v1.IssueApiClient;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.api.v1.ReportApiClient;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.CreateIssueJson;
import nu.ndw.nls.springboot.core.time.ClockService;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NetworkAnalyserServiceTest {

    private NetworkAnalyserService networkAnalyserService;

    @Mock
    private IssueApiClient issueApiClient;

    @Mock
    private ReportApiClient reportApiClient;

    @Mock
    private AccessibilityService accessibilityService;

    @Mock
    private NetworkDataService networkDataService;

    @Mock
    private NetworkData networkData;

    @Mock
    private ClockService clockService;

    @Mock
    private IssueBuilder issueBuilder;

    @Mock
    private RoadSection missingRoadSection;

    @Mock
    private CreateIssueJson issue;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    private AnalyseNetworkConfiguration analyseNetworkConfiguration;

    private AccessibilityRequest accessibilityRequest;

    @Mock
    private OffsetDateTime timestamp;

    @BeforeEach
    void setUp() {
        when(clockService.now()).thenReturn(timestamp);

        analyseNetworkConfiguration = AnalyseNetworkConfiguration.builder()
                .name("name")
                .reportIssues(true)
                .startLocationLatitude(2d)
                .startLocationLongitude(3d)
                .searchRadiusInMeters(4d)
                .build();

        accessibilityRequest = AccessibilityRequest.builder()
                .timestamp(timestamp)
                .startLocationLatitude(2d)
                .startLocationLongitude(3d)
                .searchRadiusInMeters(4d)
                .addMissingRoadsSectionsFromNwb(true)
                .build();

        networkAnalyserService = new NetworkAnalyserService(
                issueApiClient,
                reportApiClient,
                issueBuilder,
                accessibilityService,
                networkDataService,
                clockService);
    }

    @Test
    void analyse() {
        when(issueBuilder.buildUnroutableNetworkIssue(
                eq(missingRoadSection),
                eq(1234),
                argThat(reportId -> {
                    Pattern reportIdPattern = Pattern.compile(
                            "^Nwb-1234-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$",
                            Pattern.CASE_INSENSITIVE);
                    Matcher reportIdMatcher = reportIdPattern.matcher(reportId);
                    return reportIdMatcher.find();
                }),
                eq("UnreachableNetworkSegments")
        )).thenReturn(issue);
        when(networkDataService.get()).thenReturn(networkData);
        when(networkData.getNwbVersion()).thenReturn(1234);
        when(accessibilityService.calculateAccessibility(
                eq(networkData),
                assertArg(actualAccessibilityRequest -> assertThat(actualAccessibilityRequest).isEqualTo(accessibilityRequest)))
        ).thenReturn(
                Accessibility.builder()
                        .unroutableRoadSections(Set.of(missingRoadSection))
                        .build());

        networkAnalyserService.analyse(analyseNetworkConfiguration);

        verify(issueApiClient).createIssue(issue);
        verify(reportApiClient).reportComplete(
                argThat(completeReportJson -> {
                    Pattern pattern = Pattern.compile(
                            "^Nwb-1234-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$",
                            Pattern.CASE_INSENSITIVE);
                    Matcher reportIdMatcher = pattern.matcher(completeReportJson.getReporterReportId());
                    return reportIdMatcher.find()
                           && completeReportJson.getReporterReportGroupId()
                                   .equals("UnreachableNetworkSegments");
                }));
        loggerExtension.containsLog(
                Level.INFO, "Analysing with the following properties: AnalyseNetworkConfiguration[name=name, "
                            + "reportIssues=true, searchRadiusInMeters=4.0, startLocationLatitude=2.0, startLocationLongitude=3.0]");
    }
}
