package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.event;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.events.NlsEvent;
import nu.ndw.nls.events.NlsEventSubject;
import nu.ndw.nls.events.NlsEventSubjectType;
import nu.ndw.nls.events.NlsEventType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessibilityGeoJsonGeneratedEventMapper {

    public NlsEvent map(
            TrafficSignType trafficSignType,
            int version,
            int nwbVersionId,
            Instant trafficSignTimestamp) {

        return NlsEvent.builder()
                .type(NlsEventType.MAP_GEOJSON_PUBLISHED_EVENT)
                .subject(NlsEventSubject.builder()
                        .type(mapNlsEventSubjectType(trafficSignType))
                        .version(String.valueOf(version))
                        .nwbVersion(String.valueOf(nwbVersionId))
                        .timestamp(trafficSignTimestamp.toString())
                        .build())
                .build();
    }

    private NlsEventSubjectType mapNlsEventSubjectType(TrafficSignType trafficSignType) {
        return NlsEventSubjectType.valueOf(
                "accessibility_windows_times_rvv_code_%s"
                        .formatted(trafficSignType.name())
                        .toUpperCase());
    }
}
