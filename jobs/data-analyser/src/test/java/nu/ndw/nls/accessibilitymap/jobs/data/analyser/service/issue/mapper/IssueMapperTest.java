package nu.ndw.nls.accessibilitymap.jobs.data.analyser.service.issue.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IssueMapperTest {

    private IssueMapper mapper;

    @Mock
    private DirectionalSegment directionalSegment;

    @Mock
    private TrafficSign trafficSign;

    @Mock
    private RoadSection roadSection;

    @BeforeEach
    void setup() {
        mapper = new IssueMapper();
    }

    @Test
    void mapUnroutableNetworkIssue() {

        when(roadSection.getId()).thenReturn(234L);

        var issue = mapper.mapUnroutableNetworkIssue(roadSection, 123, "reportId", "reportGroupId");

        assertThat(issue).usingRecursiveComparison()
                .isEqualTo(CreateIssueJson
                        .builder()
                        .title("Unroutable network segment detected")
                        .description("Road section with id '234' is not linked to other road sections that are routable from other parts of"
                                     + " the network and is therefore part of a isolated section.")
                        .status(IssueStatusJson.OPEN)
                        .type(IssueTypeJson.UNREACHABLE_NETWORK_SEGMENT)
                        .violations(Collections.emptyList())
                        .priority(IssuePriorityJson.MEDIUM)
                        .reporterReportId("reportId")
                        .reporterReportGroupId("reportGroupId")
                        .dataLinks(List.of(DataLinkJson
                                .builder()
                                .versions(List.of("123"))
                                .source(DataLinkSourceJson.NWB)
                                .records(List.of(DataLinkRecordJson.builder()
                                        .id("234")
                                        .type("RoadSection")
                                        .build()
                                ))
                                .build()))
                );
    }

    @Test
    void mapToTrafficSignIssue() {

        when(directionalSegment.getTrafficSigns()).thenReturn(List.of(trafficSign));
        when(trafficSign.externalId()).thenReturn("id");
        when(directionalSegment.getRoadSectionId()).thenReturn(1L);

        var issue = mapper.mapToTrafficSignIssue(directionalSegment, "reportId", "reportGroupId");

        assertThat(issue).usingRecursiveComparison()
                .isEqualTo(CreateIssueJson
                        .builder()
                        .title("Asymmetric traffic sign placement")
                        .description("Asymmetric traffic sign placement")
                        .status(IssueStatusJson.OPEN)
                        .type(IssueTypeJson.MISSING)
                        .violations(Collections.emptyList())
                        .priority(IssuePriorityJson.MEDIUM)
                        .reporterReportId("reportId")
                        .reporterReportGroupId("reportGroupId")
                        .dataLinks(List.of(DataLinkJson
                                .builder()
                                .versions(List.of("1"))
                                .source(DataLinkSourceJson.TRAFFIC_SIGN_API)
                                .records(List.of(DataLinkRecordJson.builder()
                                                .id("id")
                                                .type("TrafficSign")
                                                .build(),
                                        DataLinkRecordJson.builder()
                                                .id("1")
                                                .type("RoadSection")
                                                .build()
                                ))
                                .build()))
                );
    }
}
