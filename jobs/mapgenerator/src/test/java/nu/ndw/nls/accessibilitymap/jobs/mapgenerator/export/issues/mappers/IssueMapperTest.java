package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.issues.mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import lombok.SneakyThrows;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.DataLinkJson;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.DataLinkRecordJson;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.DataLinkSourceJson;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.IssueJson;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.IssueJson.PriorityEnum;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.IssueJson.StatusEnum;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.IssueJson.TypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IssueMapperTest {

    @Mock
    private DirectionalSegment directionalSegment;

    @Mock
    private TrafficSign trafficSign;

    private IssueMapper mapper;

    @BeforeEach
    void setup() {
        mapper = new IssueMapper();
    }

    @SneakyThrows
    @Test
    void mapToIssue_ok() {
        when(directionalSegment.getTrafficSign()).thenReturn(trafficSign);
        when(trafficSign.externalId()).thenReturn("id");
        when(trafficSign.roadSectionId()).thenReturn(1);
        IssueJson issueJson = mapper.mapToIssue(directionalSegment);
        assertThat(issueJson).usingRecursiveComparison()
                .ignoringFields("id", "createdAt")
                .ignoringFieldsMatchingRegexes("dataLinks*.revision")
                .isEqualTo(IssueJson
                        .builder()
                        .description("Asymmetric traffic sign placement")
                        .title("Asymmetric traffic sign placement")
                        .status(StatusEnum.OPEN)
                        .type(TypeEnum.UNKNOWN)
                        .violations(Collections.emptyList())
                        .priority(PriorityEnum.HIGHEST)
                        .dataLinks(List.of(DataLinkJson
                                .builder()
                                .version("1")
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
