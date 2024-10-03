package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.properties.GeoJsonProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.configuration.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.trafficsign.TrafficSignType;
import nu.ndw.nls.events.NlsEvent;
import nu.ndw.nls.events.NlsEventSubject;
import nu.ndw.nls.events.NlsEventType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessibilityGeoJsonGeneratedEventMapper {

    private final GenerateConfiguration generateConfiguration;

    public NlsEvent map(
            TrafficSignType trafficSignType,
            int version,
            int nwbVersionId,
            Instant trafficSignTimestamp) {

        GeoJsonProperties geoJsonProperties = generateConfiguration.getConfiguration(trafficSignType);

        return NlsEvent.builder()
                .type(NlsEventType.MAP_GEOJSON_PUBLISHED_EVENT)
                .subject(NlsEventSubject.builder()
                        .type(geoJsonProperties.getPublisherEventSubjectType())
                        .version(String.valueOf(version))
                        .nwbVersion(String.valueOf(nwbVersionId))
                        .timestamp(trafficSignTimestamp.toString())
                        .build())
                .build();
    }

}
