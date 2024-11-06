package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.event;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
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
            List<TrafficSignType> trafficSignTypes,
            int version,
            int nwbVersionId,
            Instant trafficSignTimestamp) {
        // todo change event subject type semantics to more generic to incorporate
        //  datasets based on more than one traffic sign
        return NlsEvent.builder()
                .type(NlsEventType.MAP_GEOJSON_PUBLISHED_EVENT)
                .subject(NlsEventSubject.builder()
                        .type(mapNlsEventSubjectType(trafficSignTypes.getFirst()))
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
                        .toUpperCase(Locale.US));
    }
}
