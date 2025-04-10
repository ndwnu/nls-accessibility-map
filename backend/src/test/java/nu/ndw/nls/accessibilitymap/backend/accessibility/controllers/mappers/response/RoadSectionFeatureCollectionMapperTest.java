package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mappers.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureCollectionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureCollectionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureJson;
import nu.ndw.nls.routingmapmatcher.model.singlepoint.SinglePointMatch.CandidateMatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoadSectionFeatureCollectionMapperTest {

    private static final boolean START_POINT_REQUESTED = true;
    private static final boolean ACCESSIBLE = true;
    @Mock
    private RoadSectionFeatureMapper roadSectionFeatureMapper;
    @Mock
    private RoadSectionFeatureJson roadSectionFeatureJson;
    @Mock
    private RoadSection roadSection;
    @Mock
    private Accessibility accessibility;
    @Mock
    private CandidateMatch candidateMatch;

    private RoadSectionFeatureCollectionMapper roadSectionFeatureCollectionMapper;

    @BeforeEach
    void setUp() {
        roadSectionFeatureCollectionMapper = new RoadSectionFeatureCollectionMapper(roadSectionFeatureMapper);
    }

    @Test
    void map() {
        when(accessibility.combinedAccessibility()).thenReturn(List.of(roadSection));
        when(roadSectionFeatureMapper.map(roadSection,
                START_POINT_REQUESTED,
                candidateMatch,
                ACCESSIBLE))
                .thenReturn(List.of(roadSectionFeatureJson));
        RoadSectionFeatureCollectionJson result = roadSectionFeatureCollectionMapper.map(accessibility, START_POINT_REQUESTED,
                candidateMatch, ACCESSIBLE);
        assertThat(result).isEqualTo(
                new RoadSectionFeatureCollectionJson()
                        .type(TypeEnum.FEATURE_COLLECTION)
                        .features(List.of(roadSectionFeatureJson)));
    }
}
