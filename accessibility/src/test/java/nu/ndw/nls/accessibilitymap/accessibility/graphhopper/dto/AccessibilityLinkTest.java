package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto;

import static org.assertj.core.api.Assertions.assertThat;

import nu.ndw.nls.routingmapmatcher.network.annotations.EncodedValue;
import nu.ndw.nls.routingmapmatcher.network.model.DirectionalDto;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.LineString;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityLinkTest {

    @Mock
    private DirectionalDto<Boolean> accessibility;

    @Mock
    private LineString geometry;

    @Test
    void constructor() {

        AccessibilityLink accessibilityLink = new AccessibilityLink(1, 2, 3, accessibility, 4d, geometry, 5);

        assertThat(accessibilityLink.getId()).isEqualTo(1);
        assertThat(accessibilityLink.getFromNodeId()).isEqualTo(2);
        assertThat(accessibilityLink.getToNodeId()).isEqualTo(3);
        assertThat(accessibilityLink.getAccessibility()).isEqualTo(accessibility);
        assertThat(accessibilityLink.getDistanceInMeters()).isEqualTo(4d);
        assertThat(accessibilityLink.getGeometry()).isEqualTo(geometry);
        assertThat(accessibilityLink.getMunicipalityCode()).isEqualTo(5);
    }

    @Test
    void staticValues() {
        assertThat(AccessibilityLink.MUNICIPALITY_CODE).isEqualTo("municipality_code");
    }

    @Test
    void annotation() {
        AnnotationUtil.fieldContainsAnnotation(
                AccessibilityLink.class,
                EncodedValue.class,
                "municipalityCode",
                annotation -> {
                    assertThat(annotation.bits()).isEqualTo(17);
                    assertThat(annotation.key()).isEqualTo("municipality_code");
                });
    }
}