package nu.ndw.nls.accessibilitymap.jobs.data.analyser.service.issue.mapper;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.CreateIssueJson;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.DataLinkJson;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.DataLinkRecordJson;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.DataLinkSourceJson;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.IssuePriorityJson;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.IssueStatusJson;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.IssueTypeJson;
import org.springframework.stereotype.Component;

@Component
public class IssueBuilder {

    private static final String DATA_LINK_RECORD_TYPE_ROAD_SECTION = "RoadSection";

    public CreateIssueJson buildUnroutableNetworkIssue(RoadSection roadSection, int nwbVersion, String reportId, String reportGroupId) {

        return CreateIssueJson.builder()
                .type(IssueTypeJson.UNREACHABLE_NETWORK_SEGMENT)
                .title("Unroutable network segment detected")
                .description(("Road section with id '%s' is not linked to other road sections that are routable from other parts of the "
                              + "network and is therefore part of a isolated section.").formatted(roadSection.getId()))
                .violations(emptyList())
                .status(IssueStatusJson.OPEN)
                .dataLinks(List.of(DataLinkJson.builder()
                        .source(DataLinkSourceJson.NWB)
                        .versions(List.of("" + nwbVersion))
                        .records(List.of(DataLinkRecordJson.builder()
                                .type(DATA_LINK_RECORD_TYPE_ROAD_SECTION)
                                .id(String.valueOf(roadSection.getId()))
                                .build()))
                        .build()))
                .priority(IssuePriorityJson.MEDIUM)
                .reporterReportId(reportId)
                .reporterReportGroupId(reportGroupId)
                .build();
    }

    public CreateIssueJson buildTrafficSignIssue(DirectionalSegment directionalSegment, String reportId, String reportGroupId) {
        List<DataLinkRecordJson> recordsList = directionalSegment.getTrafficSigns()
                .stream()
                .map(IssueBuilder::mapTrafficSign)
                .collect(Collectors.toCollection(ArrayList::new));

        recordsList.add(DataLinkRecordJson.builder()
                .type(DATA_LINK_RECORD_TYPE_ROAD_SECTION)
                .id(String.valueOf(directionalSegment.getRoadSectionId()))
                .build());

        DataLinkJson dataLinkJsonTrafficSign = DataLinkJson.builder()
                .source(DataLinkSourceJson.TRAFFIC_SIGN_API)
                .versions(List.of("1"))
                .records(recordsList)
                .build();

        final var title = "Asymmetric traffic sign placement";
        return CreateIssueJson.builder()
                .type(IssueTypeJson.MISSING)
                .title(title)
                .description(title)
                .violations(emptyList())
                .status(IssueStatusJson.OPEN)
                .dataLinks(List.of(dataLinkJsonTrafficSign))
                .priority(IssuePriorityJson.MEDIUM)
                .reporterReportId(reportId)
                .reporterReportGroupId(reportGroupId)
                .build();
    }

    private static DataLinkRecordJson mapTrafficSign(TrafficSign trafficSign) {
        return DataLinkRecordJson.builder()
                .type("TrafficSign")
                .id(trafficSign.externalId())
                .build();
    }
}
