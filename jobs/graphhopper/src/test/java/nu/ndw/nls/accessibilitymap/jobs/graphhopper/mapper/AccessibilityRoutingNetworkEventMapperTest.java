package nu.ndw.nls.accessibilitymap.jobs.graphhopper.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import nu.ndw.nls.events.NlsEvent;
import nu.ndw.nls.events.NlsEventSubject;
import nu.ndw.nls.events.NlsEventSubjectType;
import nu.ndw.nls.events.NlsEventType;
import org.junit.jupiter.api.Test;

class AccessibilityRoutingNetworkEventMapperTest {
    private static final int NWB_VERSION_ID = 20231001;
    private static final String NWB_VERSION_ID_STRING = "20231001";
    private static final String TRAFFIC_SIGN_TIMESTAMP_STRING = "2023-11-07T15:37:23Z";
    private static final Instant TRAFFIC_SIGN_TIMESTAMP = Instant.parse(TRAFFIC_SIGN_TIMESTAMP_STRING);

    private final AccessibilityRoutingNetworkEventMapper mapper = new AccessibilityRoutingNetworkEventMapper();
    @Test
    void map() {
        NlsEvent event = mapper.map(NWB_VERSION_ID, TRAFFIC_SIGN_TIMESTAMP);

        assertEquals(NlsEventType.ACCESSIBILITY_ROUTING_NETWORK_UPDATED, event.getType());
        assertEquals(NlsEventSubject.builder()
                .type(NlsEventSubjectType.ACCESSIBILITY_ROUTING_NETWORK)
                .nwbVersion(NWB_VERSION_ID_STRING)
                .timestamp(TRAFFIC_SIGN_TIMESTAMP_STRING)
                .build(), event.getSubject());
        assertNull(event.getSourceEvent());
    }
}