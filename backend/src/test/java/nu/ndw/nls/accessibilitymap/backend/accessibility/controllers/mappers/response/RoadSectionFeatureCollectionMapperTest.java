package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mappers.response;

import nu.ndw.nls.accessibilitymap.accessibility.services.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoadSectionFeatureCollectionMapperTest {

    @Mock
    private RoadSectionFeatureMapper roadSectionFeatureMapper;


    @Mock
    private RoadSectionFeatureJson roadSectionFeatureJson;
    @Mock
    private Accessibility accessibility;

    private RoadSectionFeatureCollectionMapper roadSectionFeatureCollectionMapper;

    @BeforeEach
    void setUp() {
        roadSectionFeatureCollectionMapper = new RoadSectionFeatureCollectionMapper(roadSectionFeatureMapper);
    }

    @Test
    void map() {

    }
}
