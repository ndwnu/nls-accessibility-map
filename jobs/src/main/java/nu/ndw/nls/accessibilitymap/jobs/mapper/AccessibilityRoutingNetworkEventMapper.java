package nu.ndw.nls.accessibilitymap.jobs.mapper;

import java.time.Instant;
import nu.ndw.nls.events.NlsEvent;
import nu.ndw.nls.events.NlsEventSubject;
import nu.ndw.nls.events.NlsEventSubjectType;
import nu.ndw.nls.events.NlsEventType;
import org.springframework.stereotype.Component;

@Component
public class AccessibilityRoutingNetworkEventMapper {

    public NlsEvent map(int nwbVersionId, Instant trafficSignTimestamp) {
        return NlsEvent.builder()
                .type(NlsEventType.ACCESSIBILITY_ROUTING_NETWORK_UPDATED)
                .subject(NlsEventSubject.builder()
                        .type(NlsEventSubjectType.ACCESSIBILITY_ROUTING_NETWORK)
                        .nwbVersion(String.valueOf(nwbVersionId))
                        .timestamp(trafficSignTimestamp.toString())
                        .build())
                .build();
    }

}
