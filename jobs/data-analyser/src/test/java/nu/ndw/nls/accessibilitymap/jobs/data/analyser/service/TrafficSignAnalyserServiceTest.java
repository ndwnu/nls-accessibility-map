package nu.ndw.nls.accessibilitymap.jobs.data.analyser.service;

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
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.Accessibility;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.service.AccessibilityService;
import nu.ndw.nls.accessibilitymap.jobs.data.analyser.command.dto.AnalyseAsymmetricTrafficSignsConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.data.analyser.service.issue.mapper.IssueBuilder;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.api.v1.IssueApiClient;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.api.v1.ReportApiClient;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.CreateIssueJson;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.IssueJson;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
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
    private NetworkGraphHopper networkGraphHopper;

    @Mock
    private IssueApiClient issueApiClient;

    @Mock
    private ReportApiClient reportApiClient;

    @Mock
    private IssueBuilder issueBuilder;

    @Mock
    private AccessibilityRequest accessibilityRequest;

    @Mock
    private Accessibility accessibility;

    @Mock
    private AnalyseAsymmetricTrafficSignsConfiguration analyseAsymmetricTrafficSignsConfiguration;

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

        trafficSignAnalyserService = new TrafficSignAnalyserService(
                issueApiClient,
                reportApiClient,
                accessibilityService,
                issueBuilder);
    }

    @Test
    void analyse() {

        when(analyseAsymmetricTrafficSignsConfiguration.nwbVersion()).thenReturn(1234);
        when(analyseAsymmetricTrafficSignsConfiguration.reportIssues()).thenReturn(true);
        when(analyseAsymmetricTrafficSignsConfiguration.accessibilityRequest()).thenReturn(accessibilityRequest);
        when(accessibilityRequest.trafficSignTypes()).thenReturn(Set.of(TrafficSignType.C21, TrafficSignType.C22C));

        when(accessibilityService.calculateAccessibility(networkGraphHopper, accessibilityRequest)).thenReturn(accessibility);

        when(accessibility.combinedAccessibility()).thenReturn(List.of(roadSection));
        when(roadSection.getRoadSectionFragments()).thenReturn(List.of(roadSectionFragment));
        when(roadSection.getRoadSectionFragments()).thenReturn(List.of(roadSectionFragment));
        when(roadSectionFragment.isPartiallyAccessible()).thenReturn(true);
        when(roadSectionFragment.getSegments()).thenReturn(List.of(directionalSegment));
        when(directionalSegment.hasTrafficSigns()).thenReturn(true);
        when(issueBuilder.buildTrafficSignIssue(
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

        trafficSignAnalyserService.analyse(networkGraphHopper, analyseAsymmetricTrafficSignsConfiguration);

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

        loggerExtension.containsLog(Level.INFO, "Analysing with the following properties: analyseAsymmetricTrafficSignsConfiguration");
    }

    @Test
    void analyse_doNotReportIssues() {

        when(analyseAsymmetricTrafficSignsConfiguration.nwbVersion()).thenReturn(1234);
        when(analyseAsymmetricTrafficSignsConfiguration.reportIssues()).thenReturn(false);
        when(analyseAsymmetricTrafficSignsConfiguration.accessibilityRequest()).thenReturn(accessibilityRequest);
        when(accessibilityRequest.trafficSignTypes()).thenReturn(Set.of(TrafficSignType.C21, TrafficSignType.C22C));

        when(accessibilityService.calculateAccessibility(networkGraphHopper, accessibilityRequest)).thenReturn(accessibility);

        when(accessibility.combinedAccessibility()).thenReturn(List.of(roadSection));
        when(roadSection.getRoadSectionFragments()).thenReturn(List.of(roadSectionFragment));
        when(roadSection.getRoadSectionFragments()).thenReturn(List.of(roadSectionFragment));
        when(roadSectionFragment.isPartiallyAccessible()).thenReturn(true);
        when(roadSectionFragment.getSegments()).thenReturn(List.of(directionalSegment));
        when(directionalSegment.hasTrafficSigns()).thenReturn(true);
        when(issueBuilder.buildTrafficSignIssue(
                eq(directionalSegment),
                argThat(reportId -> {
                    Pattern pattern = Pattern.compile("^Nwb-1234-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$",
                            Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(reportId);
                    return matcher.find();
                }),
                eq("AsymmetricTrafficSignPlacement-%s-%s".formatted(TrafficSignType.C21.getRvvCode(), TrafficSignType.C22C.getRvvCode()))
        )).thenReturn(createIssueJson);

        trafficSignAnalyserService.analyse(networkGraphHopper, analyseAsymmetricTrafficSignsConfiguration);

        verify(issueApiClient, never()).createIssue(any());
        verify(reportApiClient, never()).reportComplete(any());

        loggerExtension.containsLog(Level.INFO, "Analysing with the following properties: analyseAsymmetricTrafficSignsConfiguration");
    }

    @Test
    void analyse_locationDataIssuesApi_serverError() {

        when(analyseAsymmetricTrafficSignsConfiguration.nwbVersion()).thenReturn(1234);
        when(analyseAsymmetricTrafficSignsConfiguration.reportIssues()).thenReturn(true);
        when(analyseAsymmetricTrafficSignsConfiguration.accessibilityRequest()).thenReturn(accessibilityRequest);
        when(accessibilityRequest.trafficSignTypes()).thenReturn(Set.of(TrafficSignType.C21, TrafficSignType.C22C));

        when(accessibilityService.calculateAccessibility(networkGraphHopper, accessibilityRequest)).thenReturn(accessibility);

        when(accessibility.combinedAccessibility()).thenReturn(List.of(roadSection));
        when(roadSection.getRoadSectionFragments()).thenReturn(List.of(roadSectionFragment));
        when(roadSection.getRoadSectionFragments()).thenReturn(List.of(roadSectionFragment));
        when(roadSectionFragment.isPartiallyAccessible()).thenReturn(true);
        when(roadSectionFragment.getSegments()).thenReturn(List.of(directionalSegment));
        when(directionalSegment.hasTrafficSigns()).thenReturn(true);
        when(issueBuilder.buildTrafficSignIssue(
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

        assertThat(catchThrowable(() -> trafficSignAnalyserService.analyse(networkGraphHopper, analyseAsymmetricTrafficSignsConfiguration)))
                .isInstanceOf(FeignServerException.class);

        loggerExtension.containsLog(Level.INFO, "Analysing with the following properties: analyseAsymmetricTrafficSignsConfiguration");
    }

    @Test
    void analyse_locationDataIssuesApi_clientError() {

        when(analyseAsymmetricTrafficSignsConfiguration.nwbVersion()).thenReturn(1234);
        when(analyseAsymmetricTrafficSignsConfiguration.reportIssues()).thenReturn(true);
        when(analyseAsymmetricTrafficSignsConfiguration.accessibilityRequest()).thenReturn(accessibilityRequest);
        when(accessibilityRequest.trafficSignTypes()).thenReturn(Set.of(TrafficSignType.C21, TrafficSignType.C22C));

        when(accessibilityService.calculateAccessibility(networkGraphHopper, accessibilityRequest)).thenReturn(accessibility);

        when(accessibility.combinedAccessibility()).thenReturn(List.of(roadSection));
        when(roadSection.getRoadSectionFragments()).thenReturn(List.of(roadSectionFragment));
        when(roadSection.getRoadSectionFragments()).thenReturn(List.of(roadSectionFragment));
        when(roadSectionFragment.isPartiallyAccessible()).thenReturn(true);
        when(roadSectionFragment.getSegments()).thenReturn(List.of(directionalSegment));
        when(directionalSegment.hasTrafficSigns()).thenReturn(true);
        when(issueBuilder.buildTrafficSignIssue(
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

        assertThat(catchThrowable(() -> trafficSignAnalyserService.analyse(networkGraphHopper, analyseAsymmetricTrafficSignsConfiguration)))
                .isInstanceOf(FeignClientException.class);

        loggerExtension.containsLog(Level.INFO, "Analysing with the following properties: analyseAsymmetricTrafficSignsConfiguration");
    }
}
