package nu.ndw.nls.accessibilitymap.jobs.trafficsignanalyser.service.issue.mapper;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
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
public class IssueMapper {

    private static final String TITLE = "Asymmetric traffic sign placement";

    private static final String VERSION = "1";

    public CreateIssueJson mapToIssue(DirectionalSegment directionalSegment, String reportId, String reportGroupId) {
        List<DataLinkRecordJson> dataLinkJsonList = directionalSegment.getTrafficSigns()
                .stream()
                .map(IssueMapper::maptoIssueJson
                ).collect(Collectors.toCollection(ArrayList::new));

        dataLinkJsonList.add(DataLinkRecordJson.builder()
                .type("RoadSection")
                .id(String.valueOf(directionalSegment.getRoadSectionId()))
                .build());

        DataLinkJson dataLinkJsonTrafficSign = DataLinkJson.builder()
                .source(DataLinkSourceJson.TRAFFIC_SIGN_API)
                .versions(List.of(VERSION))
                .records(dataLinkJsonList)
                .build();

        return CreateIssueJson.builder()
                .type(IssueTypeJson.MISSING)
                .title(TITLE)
                .description(TITLE)
                .violations(emptyList())
                .status(IssueStatusJson.OPEN)
                .dataLinks(List.of(dataLinkJsonTrafficSign))
                .priority(IssuePriorityJson.MEDIUM)
                .reporterReportId(reportId)
                .reporterReportGroupId(reportGroupId)
                .build();
    }

    private static DataLinkRecordJson maptoIssueJson(TrafficSign trafficSign) {
        return DataLinkRecordJson.builder()
                .type("TrafficSign")
                .id(trafficSign.externalId())
                .build();
    }
}
