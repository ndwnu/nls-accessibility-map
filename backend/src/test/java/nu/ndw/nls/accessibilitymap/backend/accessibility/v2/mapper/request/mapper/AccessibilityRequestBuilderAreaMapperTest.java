package nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.request.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.graphhopper.util.shapes.BBox;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest.AccessibilityRequestBuilder;
import nu.ndw.nls.accessibilitymap.backend.accessibility.v2.configuration.BoundingBoxAreaConfiguration;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.AreaRequestJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityRequestBuilderAreaMapperTest {

    private AccessibilityRequestBuilderAreaMapper accessibilityRequestBuilderAreaMapper;

    @Mock
    private BoundingBoxAreaConfiguration boundingBoxAreaConfiguration;

    @BeforeEach
    void setUp() {

        accessibilityRequestBuilderAreaMapper = new AccessibilityRequestBuilderAreaMapper(boundingBoxAreaConfiguration) {
            @Override
            public void build(AccessibilityRequestBuilder accessibilityRequestBuilder, AreaRequestJson areaRequestJson) {
                // do nothing
            }

            @Override
            public boolean canProcessAreaRequest(AreaRequestJson areaRequestJson) {
                return false;
            }
        };
    }

    @Test
    void applySearchDistance() {
        BBox requestArea = BBox.fromPoints(0, 1, 2, 3);

        BBox searchBox = accessibilityRequestBuilderAreaMapper.applySearchDistance(requestArea, 10_000);

        assertThat(searchBox).isEqualTo(BBox.fromPoints(-0.08983111749910169, 0.9101688825008983, 2.0898311174991018, 3.0898311174991018));
    }
}
