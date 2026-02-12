package nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.request.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.graphhopper.util.shapes.BBox;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest.AccessibilityRequestBuilder;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.AreaRequestJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.BoundingBoxAreaRequestJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityRequestMapperBoundingBoxAreaMapperTest {

    private AccessibilityRequestMapperBoundingBoxAreaMapper accessibilityRequestMapperBoundingBoxAreaMapper;

    @BeforeEach
    void setUp() {

        accessibilityRequestMapperBoundingBoxAreaMapper = new AccessibilityRequestMapperBoundingBoxAreaMapper();
    }

    @Test
    void build() {

        BoundingBoxAreaRequestJson boundingBoxAreaRequestJson = BoundingBoxAreaRequestJson.builder()
                .minLatitude(1D)
                .minLongitude(2D)
                .maxLatitude(3D)
                .maxLongitude(4D)
                .build();

        AccessibilityRequestBuilder accessibilityRequestBuilder = AccessibilityRequest.builder();

        accessibilityRequestMapperBoundingBoxAreaMapper.build(accessibilityRequestBuilder, boundingBoxAreaRequestJson);

        AccessibilityRequest accessibilityRequest = accessibilityRequestBuilder.build();
        assertThat(accessibilityRequest).isNotNull();
        assertThat(accessibilityRequest.requestArea()).isEqualTo(BBox.fromPoints(
                boundingBoxAreaRequestJson.getMinLatitude(),
                boundingBoxAreaRequestJson.getMinLongitude(),
                boundingBoxAreaRequestJson.getMaxLatitude(),
                boundingBoxAreaRequestJson.getMaxLongitude()
        ));

        int metersPerDegree = 111_320;
        double expansionSearchAreaInMeters = 10_000;
        double expansionInDegrees = expansionSearchAreaInMeters / metersPerDegree;
        assertThat(accessibilityRequest.searchArea()).isEqualTo(BBox.fromPoints(
                boundingBoxAreaRequestJson.getMinLatitude() - expansionInDegrees,
                boundingBoxAreaRequestJson.getMinLongitude() - expansionInDegrees,
                boundingBoxAreaRequestJson.getMaxLatitude() + expansionInDegrees,
                boundingBoxAreaRequestJson.getMaxLongitude() + expansionInDegrees
        ));

        assertThat(accessibilityRequest.maxSearchDistanceInMeters()).isEqualTo(343144.77877420775);
    }

    @Test
    @SuppressWarnings("java:S5778")
    void build_invalidType() {

        assertThatThrownBy(() -> accessibilityRequestMapperBoundingBoxAreaMapper
                .build(AccessibilityRequest.builder(), mock(AreaRequestJson.class)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("AreaRequestJson must be of type BoundingBoxAreaRequestJson");
    }

    @Test
    void canProcessAreaRequest() {

        assertThat(accessibilityRequestMapperBoundingBoxAreaMapper.canProcessAreaRequest(mock(BoundingBoxAreaRequestJson.class)))
                .isTrue();
    }

    @Test
    void canProcessAreaRequest_false() {

        assertThat(accessibilityRequestMapperBoundingBoxAreaMapper.canProcessAreaRequest(mock(AreaRequestJson.class)))
                .isFalse();
    }

}
