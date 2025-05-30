package nu.ndw.nls.accessibilitymap.jobs.data.analyser.service;

import com.graphhopper.routing.util.EdgeFilter;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.service.NetworkCacheDataService;
import nu.ndw.nls.accessibilitymap.accessibility.service.MissingRoadSectionProvider;
import nu.ndw.nls.accessibilitymap.jobs.data.analyser.command.dto.AnalyseNetworkConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.data.analyser.service.issue.mapper.IssueBuilder;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.api.v1.IssueApiClient;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.api.v1.ReportApiClient;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NetworkAnalyserService extends IssueReporterService {

    private static final String ISSUE_REPORT_GROUP_ID = "UnreachableNetworkSegments";

    private final NetworkCacheDataService networkCacheDataService;

    private final MissingRoadSectionProvider missingRoadSectionProvider;

    private final IssueBuilder issueBuilder;

    public NetworkAnalyserService(
            IssueApiClient issueApiClient,
            ReportApiClient reportApiClient,
            NetworkCacheDataService networkCacheDataService,
            MissingRoadSectionProvider missingRoadSectionProvider,
            IssueBuilder issueBuilder) {

        super(issueApiClient, reportApiClient);

        this.issueBuilder = issueBuilder;
        this.missingRoadSectionProvider = missingRoadSectionProvider;
        this.networkCacheDataService = networkCacheDataService;
    }

    public void analyse(NetworkGraphHopper networkGraphHopper, @Valid AnalyseNetworkConfiguration analyseNetworkConfiguration) {

        log.info("Analysing with the following properties: {}", analyseNetworkConfiguration);

        var startSegment = networkGraphHopper.getLocationIndex()
                .findClosest(
                        analyseNetworkConfiguration.startLocationLatitude(),
                        analyseNetworkConfiguration.startLocationLongitude(),
                        EdgeFilter.ALL_EDGES);

        var accessibleRoadsSectionsWithoutAppliedRestrictions = networkCacheDataService.getNetworkData(
                        null,
                        startSegment,
                        analyseNetworkConfiguration.searchRadiusInMeters(),
                        List.of(),
                        networkGraphHopper)
                .baseAccessibleRoads();

        var missingRoadSections = missingRoadSectionProvider.get(
                null,
                accessibleRoadsSectionsWithoutAppliedRestrictions,
                false);

        var issueReportId = "Nwb-%s-%s".formatted(analyseNetworkConfiguration.nwbVersion(), UUID.randomUUID());
        var issues = missingRoadSections.stream()
                .map(missingRoadSection -> issueBuilder.buildUnroutableNetworkIssue(
                        missingRoadSection,
                        analyseNetworkConfiguration.nwbVersion(),
                        issueReportId,
                        ISSUE_REPORT_GROUP_ID))
                .toList();
        logAndReportIssues(issues, analyseNetworkConfiguration.reportIssues(), issueReportId, ISSUE_REPORT_GROUP_ID);
    }
}
