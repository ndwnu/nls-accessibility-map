package nu.ndw.nls.accessibilitymap.jobs.trafficsignanalyser.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import feign.FeignException;
import feign.FeignException.FeignClientException;
import feign.FeignException.FeignServerException;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.request.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.services.accessibility.AccessibilityService;
import nu.ndw.nls.accessibilitymap.accessibility.services.accessibility.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.jobs.trafficsignanalyser.command.dto.AnalyseProperties;
import nu.ndw.nls.accessibilitymap.jobs.trafficsignanalyser.service.issue.mapper.IssueMapper;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.api.v1.IssueApiClient;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.api.v1.ReportApiClient;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.CreateIssueJson;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.IssueJson;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class TrafficSignAnalyserServiceTest {

    private TrafficSignAnalyserService trafficSignAnalyserService;

    @Mock
    private AccessibilityService accessibilityService;

    @Mock
    private IssueApiClient issueApiClient;

    @Mock
    private ReportApiClient reportApiClient;

    @Mock
    private IssueMapper issueMapper;

    @Mock
    private AccessibilityRequest accessibilityRequest;

    @Mock
    private Accessibility accessibility;

    @Mock
    private AnalyseProperties analyseProperties;

    @Mock
    private RoadSection roadSection;

    @Mock
    private RoadSectionFragment roadSectionFragment;

    @Mock
    private DirectionalSegment directionalSegment;

    @Mock
    private CreateIssueJson createIssueJson;

    @Mock
    private ResponseEntity<IssueJson> createIssueResponse;

    @Mock
    private FeignException.FeignClientException feignClientException;

    @Mock
    private FeignException.FeignServerException feignServerException;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @BeforeEach
    void setUp() {

        trafficSignAnalyserService = new TrafficSignAnalyserService(accessibilityService, issueApiClient, reportApiClient, issueMapper);
    }

    @Test
    void analyse() {

        when(analyseProperties.nwbVersion()).thenReturn(1234);
        when(analyseProperties.reportIssues()).thenReturn(true);
        when(analyseProperties.accessibilityRequest()).thenReturn(accessibilityRequest);
        when(accessibilityRequest.trafficSignTypes()).thenReturn(Set.of(TrafficSignType.C21, TrafficSignType.C22C));

        when(accessibilityService.calculateAccessibility(accessibilityRequest)).thenReturn(accessibility);

        when(accessibility.combinedAccessibility()).thenReturn(List.of(roadSection));
        when(roadSection.getRoadSectionFragments()).thenReturn(List.of(roadSectionFragment));
        when(roadSection.getRoadSectionFragments()).thenReturn(List.of(roadSectionFragment));
        when(roadSectionFragment.isPartiallyAccessible()).thenReturn(true);
        when(roadSectionFragment.getSegments()).thenReturn(List.of(directionalSegment));
        when(directionalSegment.hasTrafficSigns()).thenReturn(true);
        when(issueMapper.mapToIssue(
                eq(directionalSegment),
                argThat(reportId -> {
                    Pattern pattern = Pattern.compile("^Nwb-1234-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$",
                            Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(reportId);
                    return matcher.find();
                }),
                eq("AsymmetricTrafficSignPlacement-%s-%s".formatted(TrafficSignType.C21.getRvvCode(), TrafficSignType.C22C.getRvvCode()))
        )).thenReturn(createIssueJson);

        when(issueApiClient.createIssue(createIssueJson)).thenReturn(createIssueResponse);

        trafficSignAnalyserService.analyse(analyseProperties);

        verify(issueApiClient).createIssue(createIssueJson);
        verify(reportApiClient).reportComplete(argThat(completeReportJson -> {
            Pattern pattern = Pattern.compile("^Nwb-1234-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$",
                    Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(completeReportJson.getReporterReportId());
            return matcher.find()
                    && completeReportJson.getReporterReportGroupId()
                    .equals("AsymmetricTrafficSignPlacement-%s-%s".formatted(TrafficSignType.C21.getRvvCode(),
                            TrafficSignType.C22C.getRvvCode()));
        }));

        loggerExtension.containsLog(Level.INFO, "Analysing with the following properties: analyseProperties");
        loggerExtension.containsLog(Level.DEBUG, "Detected traffic sign issue: createIssueJson");
        loggerExtension.containsLog(Level.INFO, "Reported traffic sign issue: createIssueJson");
    }

    @Test
    void analyse_doNotReportIssues() {

        when(analyseProperties.nwbVersion()).thenReturn(1234);
        when(analyseProperties.reportIssues()).thenReturn(false);
        when(analyseProperties.accessibilityRequest()).thenReturn(accessibilityRequest);
        when(accessibilityRequest.trafficSignTypes()).thenReturn(Set.of(TrafficSignType.C21, TrafficSignType.C22C));

        when(accessibilityService.calculateAccessibility(accessibilityRequest)).thenReturn(accessibility);

        when(accessibility.combinedAccessibility()).thenReturn(List.of(roadSection));
        when(roadSection.getRoadSectionFragments()).thenReturn(List.of(roadSectionFragment));
        when(roadSection.getRoadSectionFragments()).thenReturn(List.of(roadSectionFragment));
        when(roadSectionFragment.isPartiallyAccessible()).thenReturn(true);
        when(roadSectionFragment.getSegments()).thenReturn(List.of(directionalSegment));
        when(directionalSegment.hasTrafficSigns()).thenReturn(true);
        when(issueMapper.mapToIssue(
                eq(directionalSegment),
                argThat(reportId -> {
                    Pattern pattern = Pattern.compile("^Nwb-1234-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$",
                            Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(reportId);
                    return matcher.find();
                }),
                eq("AsymmetricTrafficSignPlacement-%s-%s".formatted(TrafficSignType.C21.getRvvCode(), TrafficSignType.C22C.getRvvCode()))
        )).thenReturn(createIssueJson);

        trafficSignAnalyserService.analyse(analyseProperties);

        verify(issueApiClient, never()).createIssue(any());
        verify(reportApiClient, never()).reportComplete(any());

        loggerExtension.containsLog(Level.INFO, "Analysing with the following properties: analyseProperties");
        loggerExtension.containsLog(Level.DEBUG, "Detected traffic sign issue: createIssueJson");
    }

    @Test
    void analyse_locationDataIssuesApi_serverError() {

        when(analyseProperties.nwbVersion()).thenReturn(1234);
        when(analyseProperties.reportIssues()).thenReturn(true);
        when(analyseProperties.accessibilityRequest()).thenReturn(accessibilityRequest);
        when(accessibilityRequest.trafficSignTypes()).thenReturn(Set.of(TrafficSignType.C21, TrafficSignType.C22C));

        when(accessibilityService.calculateAccessibility(accessibilityRequest)).thenReturn(accessibility);

        when(accessibility.combinedAccessibility()).thenReturn(List.of(roadSection));
        when(roadSection.getRoadSectionFragments()).thenReturn(List.of(roadSectionFragment));
        when(roadSection.getRoadSectionFragments()).thenReturn(List.of(roadSectionFragment));
        when(roadSectionFragment.isPartiallyAccessible()).thenReturn(true);
        when(roadSectionFragment.getSegments()).thenReturn(List.of(directionalSegment));
        when(directionalSegment.hasTrafficSigns()).thenReturn(true);
        when(issueMapper.mapToIssue(
                eq(directionalSegment),
                argThat(reportId -> {
                    Pattern pattern = Pattern.compile("^Nwb-1234-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$",
                            Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(reportId);
                    return matcher.find();
                }),
                eq("AsymmetricTrafficSignPlacement-%s-%s".formatted(TrafficSignType.C21.getRvvCode(), TrafficSignType.C22C.getRvvCode())))
        ).thenReturn(createIssueJson);

        when(issueApiClient.createIssue(createIssueJson)).thenThrow(feignServerException);

        assertThat(catchThrowable(() -> trafficSignAnalyserService.analyse(analyseProperties)))
                .isInstanceOf(FeignServerException.class);

        loggerExtension.containsLog(Level.INFO, "Analysing with the following properties: analyseProperties");
        loggerExtension.containsLog(Level.DEBUG, "Detected traffic sign issue: createIssueJson");
    }

    @Test
    void analyse_locationDataIssuesApi_clientError() {

        when(analyseProperties.nwbVersion()).thenReturn(1234);
        when(analyseProperties.reportIssues()).thenReturn(true);
        when(analyseProperties.accessibilityRequest()).thenReturn(accessibilityRequest);
        when(accessibilityRequest.trafficSignTypes()).thenReturn(Set.of(TrafficSignType.C21, TrafficSignType.C22C));

        when(accessibilityService.calculateAccessibility(accessibilityRequest)).thenReturn(accessibility);

        when(accessibility.combinedAccessibility()).thenReturn(List.of(roadSection));
        when(roadSection.getRoadSectionFragments()).thenReturn(List.of(roadSectionFragment));
        when(roadSection.getRoadSectionFragments()).thenReturn(List.of(roadSectionFragment));
        when(roadSectionFragment.isPartiallyAccessible()).thenReturn(true);
        when(roadSectionFragment.getSegments()).thenReturn(List.of(directionalSegment));
        when(directionalSegment.hasTrafficSigns()).thenReturn(true);
        when(issueMapper.mapToIssue(
                eq(directionalSegment),
                argThat(reportId -> {
                    Pattern pattern = Pattern.compile("^Nwb-1234-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$",
                            Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(reportId);
                    return matcher.find();
                }),
                eq("AsymmetricTrafficSignPlacement-%s-%s".formatted(TrafficSignType.C21.getRvvCode(),
                        TrafficSignType.C22C.getRvvCode())))).thenReturn(createIssueJson);

        when(issueApiClient.createIssue(createIssueJson)).thenThrow(feignClientException);

        assertThat(catchThrowable(() -> trafficSignAnalyserService.analyse(analyseProperties)))
                .isInstanceOf(FeignClientException.class);

        loggerExtension.containsLog(Level.INFO, "Analysing with the following properties: analyseProperties");
        loggerExtension.containsLog(Level.DEBUG, "Detected traffic sign issue: createIssueJson");
    }
}
