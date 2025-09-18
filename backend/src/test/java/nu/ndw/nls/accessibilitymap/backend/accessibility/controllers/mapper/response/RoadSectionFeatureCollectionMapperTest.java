package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureCollectionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureCollectionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoadSectionFeatureCollectionMapperTest {

    private RoadSectionFeatureCollectionMapper roadSectionFeatureCollectionMapper;

    @Mock
    private RoadSectionFeatureMapper roadSectionFeatureMapper;

    @Mock
    private RoadSectionFeatureJson roadSectionFeatureJson;

    @Mock
    private RoadSection roadSection;

    @BeforeEach
    void setUp() {

        roadSectionFeatureCollectionMapper = new RoadSectionFeatureCollectionMapper(roadSectionFeatureMapper);
    }

    @Test
    void map() {

        when(roadSectionFeatureMapper.map(roadSection, true, 2L, true))
                .thenReturn(List.of(roadSectionFeatureJson));

        RoadSectionFeatureCollectionJson roadSectionFeatureCollection = roadSectionFeatureCollectionMapper.map(List.of(roadSection),
                true,
                2L,
                true);

        assertThat(roadSectionFeatureCollection).isEqualTo(
                new RoadSectionFeatureCollectionJson()
                        .type(TypeEnum.FEATURE_COLLECTION)
                        .features(List.of(roadSectionFeatureJson)));
    }
}
