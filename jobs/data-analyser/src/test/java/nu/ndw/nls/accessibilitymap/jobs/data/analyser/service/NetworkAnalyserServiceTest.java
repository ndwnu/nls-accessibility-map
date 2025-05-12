package nu.ndw.nls.accessibilitymap.jobs.data.analyser.service;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.index.LocationIndexTree;
import com.graphhopper.storage.index.Snap;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service.NetworkCacheDataService;
import nu.ndw.nls.accessibilitymap.accessibility.service.MissingRoadSectionProvider;
import nu.ndw.nls.accessibilitymap.jobs.data.analyser.command.dto.AnalyseNetworkConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.data.analyser.service.issue.mapper.IssueMapper;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.api.v1.IssueApiClient;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.api.v1.ReportApiClient;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.CreateIssueJson;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
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
    private NetworkCacheDataService networkCacheDataService;

    @Mock
    private MissingRoadSectionProvider missingRoadSectionProvider;

    @Mock
    private IssueMapper issueMapper;

    @Mock
    private NetworkGraphHopper networkGraphHopper;

    @Mock
    private LocationIndexTree locationIndexTree;

    @Mock
    private Snap startSegmentSnap;

    @Mock
    private NetworkData networkData;

    @Mock
    private RoadSection roadSectionNoRestriction;

    @Mock
    private RoadSection missingRoadSection;

    @Mock
    private CreateIssueJson issue;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    private AnalyseNetworkConfiguration analyseNetworkConfiguration;

    @BeforeEach
    void setUp() {
        analyseNetworkConfiguration = AnalyseNetworkConfiguration.builder()
                .name("name")
                .nwbVersion(1234)
                .reportIssues(true)
                .startLocationLatitude(2d)
                .startLocationLongitude(3d)
                .searchRadiusInMeters(4d)
                .build();

        networkAnalyserService = new NetworkAnalyserService(
                issueApiClient,
                reportApiClient,
                networkCacheDataService,
                missingRoadSectionProvider,
                issueMapper);
    }

    @Test
    void analyse() {

        when(networkGraphHopper.getLocationIndex()).thenReturn(locationIndexTree);
        when(locationIndexTree.findClosest(
                2d,
                3d,
                EdgeFilter.ALL_EDGES))
                .thenReturn(startSegmentSnap);

        when(networkCacheDataService.getNetworkData(
                null,
                startSegmentSnap,
                4d,
                List.of(),
                networkGraphHopper)
        ).thenReturn(networkData);
        when(networkData.baseAccessibleRoads())
                .thenReturn(new ArrayList<>(List.of(roadSectionNoRestriction)));

        when(missingRoadSectionProvider.get(null, List.of(roadSectionNoRestriction), false))
                .thenReturn(List.of(missingRoadSection));
        when(issueMapper.mapUnroutableNetworkIssue(
                eq(missingRoadSection),
                eq(analyseNetworkConfiguration.nwbVersion()),
                argThat(reportId -> {
                    Pattern reportIdPattern = Pattern.compile("^Nwb-1234-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$",
                            Pattern.CASE_INSENSITIVE);
                    Matcher reportIdMatcher = reportIdPattern.matcher(reportId);
                    return reportIdMatcher.find();
                }),
                eq("UnreachableNetworkSegments")
        )).thenReturn(issue);
        networkAnalyserService.analyse(networkGraphHopper, analyseNetworkConfiguration);

        verify(issueApiClient).createIssue(issue);
        verify(reportApiClient).reportComplete(
                argThat(completeReportJson -> {
            Pattern pattern = Pattern.compile("^Nwb-1234-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$",
                    Pattern.CASE_INSENSITIVE);
            Matcher reportIdMatcher = pattern.matcher(completeReportJson.getReporterReportId());
            return reportIdMatcher.find()
                   && completeReportJson.getReporterReportGroupId()
                           .equals("UnreachableNetworkSegments");
        }));
        loggerExtension.containsLog(Level.INFO, "Analysing with the following properties: AnalyseNetworkConfiguration[name=name, "
                                                + "nwbVersion=1234, reportIssues=true, searchRadiusInMeters=4.0, startLocationLatitude=2.0, "
                                                + "startLocationLongitude=3.0]");
    }
}