package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.AccessibilityLink;
import nu.ndw.nls.routingmapmatcher.network.model.DirectionalDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityLinkCarMapperTest {

    @Mock
    private AccessibilityLink link;

    @InjectMocks
    private AccessibilityLinkCarMapper mapper;

    @Test
    void getAccessibility() {

        assertThat(mapper.getAccessibility(link)).isEqualTo(new DirectionalDto<>(true, true));
    }

    @Test
    void getSpeed() {

        DirectionalDto<Double> result = mapper.getSpeed(link);

        assertThat(result.forward()).isEqualTo(50);
        assertThat(result.reverse()).isEqualTo(50);
    }
}
