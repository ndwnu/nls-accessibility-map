package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.AccessibilityLink;
import nu.ndw.nls.routingmapmatcher.network.model.DirectionalDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityLinkCarMapperTest {

    @Mock
    private AccessibilityLink link;

    @InjectMocks
    private AccessibilityLinkCarMapper mapper;

    @ParameterizedTest
    @CsvSource({
            "true, true",
            "false, true",
            "true, false",
            "false, false"
    })
    void getAccessibility(boolean forwardDirection, boolean backwardDirection) {

        when(link.getAccessibility()).thenReturn(new DirectionalDto<>(forwardDirection, backwardDirection));
        assertThat(mapper.getAccessibility(link)).isEqualTo(new DirectionalDto<>(forwardDirection, backwardDirection));
    }

    @Test
    void getSpeed() {

        DirectionalDto<Double> result = mapper.getSpeed(link);

        assertThat(result.forward()).isEqualTo(50);
        assertThat(result.reverse()).isEqualTo(50);
    }
}
