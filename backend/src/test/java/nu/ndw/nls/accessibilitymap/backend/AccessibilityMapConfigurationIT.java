package nu.ndw.nls.accessibilitymap.backend;

import static org.junit.jupiter.api.Assertions.*;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AccessibilityMapConfigurationIT {

    @Autowired
    private MeterRegistryCustomizer<MeterRegistry> metricsCommonTags;

    @Test
    void metricsCommonTags_ok() {
        // Assert meter registry customizer is on classpath to add service and environment tags to all metrics.
        assertNotNull(metricsCommonTags);
    }
}
