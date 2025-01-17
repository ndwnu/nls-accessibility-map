package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.issues;

import static nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.ExportType.ASYMMETRIC_TRAFFIC_SIGNS_ISSUES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.accessibility.services.accessibility.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.dto.ExportProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.issues.mappers.IssueMapper;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.api.v1.IssueApiClient;
import nu.ndw.nls.locationdataissuesapi.client.feign.generated.model.v1.IssueJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class IssuesExporterTest {

    @Mock
    private Accessibility accessibility;
    @Mock
    private RoadSection roadSection;
    @Mock
    private RoadSectionFragment roadSectionFragment;
    @Mock
    private DirectionalSegment directionalSegment;
    @Mock
    private IssueJson issueJson;
    @Mock
    private ResponseEntity<IssueJson> issueJsonResponseEntity;

    @Mock
    private IssueApiClient issueApiClient;
    @Mock
    private ExportProperties exportProperties;

    @Mock
    private IssueMapper issueMapper;

    private IssuesExporter exporter;

    @BeforeEach
    void setup() {
        exporter = new IssuesExporter(issueApiClient, issueMapper);
    }

    @Test
    void export_ok() {
        when(accessibility.combinedAccessibility())
                .thenReturn(List.of(roadSection));
        when(roadSection.getRoadSectionFragments())
                .thenReturn(List.of(roadSectionFragment));
        when(roadSection.getRoadSectionFragments())
                .thenReturn(List.of(roadSectionFragment));
        when(roadSectionFragment.isPartiallyAccessible())
                .thenReturn(true);
        when(roadSectionFragment.getSegments())
                .thenReturn(List.of(directionalSegment));
        when(directionalSegment.hasTrafficSign())
                .thenReturn(true);
        when(issueMapper.mapToIssue(directionalSegment)).thenReturn(issueJson);
        when(issueApiClient.createIssue(issueJson)).thenReturn(issueJsonResponseEntity);
        when(issueJsonResponseEntity.getStatusCode()).thenReturn(HttpStatusCode
                .valueOf(HttpStatus.CREATED.value()));

        exporter.export(accessibility, exportProperties);

        verify(issueMapper, times(1)).mapToIssue(directionalSegment);
        verify(issueApiClient, times(1)).createIssue(issueJson);
    }

    @Test
    void export_exception() {
        when(accessibility.combinedAccessibility())
                .thenReturn(List.of(roadSection));
        when(roadSection.getRoadSectionFragments())
                .thenReturn(List.of(roadSectionFragment));
        when(roadSection.getRoadSectionFragments())
                .thenReturn(List.of(roadSectionFragment));
        when(roadSectionFragment.isPartiallyAccessible())
                .thenReturn(true);
        when(roadSectionFragment.getSegments())
                .thenReturn(List.of(directionalSegment));
        when(directionalSegment.hasTrafficSign())
                .thenReturn(true);
        when(issueMapper.mapToIssue(directionalSegment)).thenReturn(issueJson);
        when(issueApiClient.createIssue(issueJson)).thenReturn(issueJsonResponseEntity);
        when(issueJsonResponseEntity.getStatusCode()).thenReturn(HttpStatusCode
                .valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> exporter.export(accessibility, exportProperties))
                .withMessageContaining("Failed to create issue");

    }

    @Test
    void isEnabled_ok() {
        assertThat(exporter.isEnabled(Set.of(ASYMMETRIC_TRAFFIC_SIGNS_ISSUES)))
                .isTrue();
    }
}
