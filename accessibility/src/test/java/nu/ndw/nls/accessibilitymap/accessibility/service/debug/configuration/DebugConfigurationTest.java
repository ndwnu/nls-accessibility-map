package nu.ndw.nls.accessibilitymap.accessibility.service.debug.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DebugConfigurationTest {

    @Test
    void isDisabled() {
        DebugConfiguration debugConfiguration = new DebugConfiguration();
        assertThat(debugConfiguration.isDisabled()).isTrue();

        debugConfiguration.setEnabled(true);
        assertThat(debugConfiguration.isDisabled()).isFalse();
    }

    @Test
    void getDebugFolder() {
        DebugConfiguration debugConfiguration = new DebugConfiguration();

        assertThat(debugConfiguration.getDebugFolder()).isEqualTo(Path.of("./.debug"));
    }
}
