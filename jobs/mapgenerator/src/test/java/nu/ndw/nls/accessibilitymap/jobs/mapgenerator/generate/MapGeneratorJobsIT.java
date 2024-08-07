package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate;

import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.HGV_ACCESS_FORBIDDEN_WINDOWED;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.MAX_HEIGHT;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.MAX_LENGTH;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.MOTOR_VEHICLE_ACCESS_FORBIDDEN;
import static nu.ndw.nls.accessibilitymap.shared.model.NetworkConstants.PROFILE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.util.EdgeIteratorState;
import io.micrometer.observation.ObservationRegistry;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import nu.ndw.nls.accessibilitymap.jobs.AccessibilityMapGeneratorJobCommandLineRunner;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.MapGeneratorJobsIT.TestConfig;
import nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink;
import nu.ndw.nls.accessibilitymap.shared.properties.GraphHopperConfiguration;
import nu.ndw.nls.accessibilitymap.shared.properties.GraphHopperProperties;
import nu.ndw.nls.events.NlsEvent;
import nu.ndw.nls.events.NlsEventSubject;
import nu.ndw.nls.events.NlsEventSubjectType;
import nu.ndw.nls.events.NlsEventType;
import nu.ndw.nls.routingmapmatcher.network.GraphHopperNetworkService;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import nu.ndw.nls.routingmapmatcher.network.model.RoutingNetworkSettings;
import nu.ndw.nls.springboot.messaging.MessagingConfig;
import nu.ndw.nls.springboot.messaging.dtos.MessageConsumeResult;
import nu.ndw.nls.springboot.messaging.services.MessageReceiveService.ReceiveKey;
import nu.ndw.nls.springboot.messaging.services.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = TestConfig.class)
@ActiveProfiles(profiles = {"integration-test"})
class MapGeneratorJobsIT {


    @Configuration
    @Import(MessagingConfig.class)
    public static class TestConfig {
    }

    @Autowired
    private MessageService messageService;

    @Test
    void messageReceived_ok_c6Published() {
        MessageConsumeResult<NlsEvent> messageConsumeResult = messageService.receive(ReceiveKey.builder()
                        .eventType(NlsEventType.MAP_GEOJSON_PUBLISHED_EVENT)
                        .eventSubjectType(NlsEventSubjectType.ACCESSIBILITY_RVV_CODE_C6)
                        .build(),
                nlsEvent -> nlsEvent);

        NlsEvent result = messageConsumeResult.getResult();

        assertEquals(NlsEventType.MAP_GEOJSON_PUBLISHED_EVENT, result.getType());
        assertEquals(NlsEventSubjectType.ACCESSIBILITY_RVV_CODE_C6, result.getSubject().getType());
        assertEquals("20240101", result.getSubject().getVersion());
        assertEquals("20240101", result.getSubject().getNwbVersion());
    }

}

