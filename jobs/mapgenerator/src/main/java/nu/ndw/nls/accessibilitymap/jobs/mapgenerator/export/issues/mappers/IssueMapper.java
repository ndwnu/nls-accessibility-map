package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.issues.mappers;

import static java.util.Collections.emptyList;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.DirectionalSegment;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.DataLinkJson;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.DataLinkRecordJson;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.DataLinkSourceJson;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.IssueJson;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.IssueJson.PriorityEnum;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.IssueJson.StatusEnum;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.IssueJson.TypeEnum;
import org.springframework.stereotype.Component;

@Component
public class IssueMapper {

    private static final String TITLE = "Asymmetric traffic sign placement";
    private static final String VERSION = "1";

    public IssueJson mapToIssue(DirectionalSegment directionalSegment) {

        List<DataLinkRecordJson> dataLinkJsonList = List.of(
                DataLinkRecordJson.builder()
                        .type("TrafficSign")
                        .id(directionalSegment.getTrafficSign().externalId())
                        .build(),
                DataLinkRecordJson.builder()
                        .type("RoadSection")
                        .id(String.valueOf(directionalSegment.getTrafficSign().roadSectionId()))
                        .build());

        DataLinkJson dataLinkJsonTrafficSign = DataLinkJson.builder()
                .source(DataLinkSourceJson.TRAFFIC_SIGN_API)
                .revision(OffsetDateTime.now())
                .version(VERSION)
                .records(dataLinkJsonList)
                .build();

        return IssueJson.builder()
                .id(UUID.randomUUID())
                .type(TypeEnum.UNKNOWN)
                .createdAt(OffsetDateTime.now())
                .description(TITLE)
                .violations(emptyList())
                .status(StatusEnum.OPEN)
                .dataLinks(List.of(dataLinkJsonTrafficSign))
                .priority(PriorityEnum.HIGHEST)
                .title(TITLE)
                .build();
    }
}
