package nu.ndw.nls.accessibilitymap.accessibility.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.AccessibilityLink;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.mapper.NwbRoadSectionToLinkMapper;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.service.NwbRoadSectionService;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(MockitoExtension.class)
class AccessibilityLinkServiceTest {

    private AccessibilityLinkService accessibilityLinkService;

    @Mock
    private NwbRoadSectionService nwbRoadSectionService;

    @Mock
    private NwbRoadSectionToLinkMapper nwbRoadSectionToLinkMapper;

    @Mock
    private AccessibilityLink accessibilityLink1;

    @Mock
    private AccessibilityLink accessibilityLink2;

    @Mock
    private NwbRoadSectionDto nwbRoadSectionDto1;

    @Mock
    private NwbRoadSectionDto nwbRoadSectionDto2;

    @BeforeEach
    void setUp() {

        accessibilityLinkService = new AccessibilityLinkService(nwbRoadSectionService, nwbRoadSectionToLinkMapper);
    }

    @Test
    void getLinks() {

        int nwbVersionId = 123;

        when(nwbRoadSectionService.findLazyCar(nwbVersionId)).thenReturn(List.of(nwbRoadSectionDto1, nwbRoadSectionDto2).stream());
        when(nwbRoadSectionToLinkMapper.map(nwbRoadSectionDto1)).thenReturn(accessibilityLink1);
        when(nwbRoadSectionToLinkMapper.map(nwbRoadSectionDto2)).thenReturn(accessibilityLink2);

        assertThat(accessibilityLinkService.getLinks(nwbVersionId)).containsExactly(accessibilityLink1, accessibilityLink2);
    }

    @Test
    void getLinks_hasTransactionalAnnotation_readOnly() {

        AnnotationUtil.methodContainsAnnotation(
                accessibilityLinkService.getClass(),
                Transactional.class,
                "getLinks",
                annotation -> assertThat(annotation.readOnly()).isTrue());
    }
}
