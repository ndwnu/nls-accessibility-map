package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.model.trafficsign.TrafficSignType;
import nu.ndw.nls.events.NlsEvent;
import nu.ndw.nls.events.NlsEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityGeoJsonGeneratedEventMapperTest {

    private AccessibilityGeoJsonGeneratedEventMapper accessibilityGeoJsonGeneratedEventMapper;

    @BeforeEach
    void setUp() {

        accessibilityGeoJsonGeneratedEventMapper = new AccessibilityGeoJsonGeneratedEventMapper();
    }

    @ParameterizedTest
    @EnumSource(value = TrafficSignType.class)
    void map(TrafficSignType trafficSignType) {

        int version = 1;
        int nwbVersionId = 123;
        Instant trafficSignTimestamp = Instant.now();

        NlsEvent nlsEvent = accessibilityGeoJsonGeneratedEventMapper.map(
                trafficSignType,
                version,
                nwbVersionId,
                trafficSignTimestamp
        );

        assertThat(nlsEvent).isNotNull();
        assertThat(nlsEvent.getType()).isEqualTo(NlsEventType.MAP_GEOJSON_PUBLISHED_EVENT);

        assertThat(nlsEvent.getSubject()).isNotNull();
        assertThat(nlsEvent.getSubject().getType().name())
                .isEqualTo("ACCESSIBILITY_WINDOWS_TIMES_RVV_CODE_" + trafficSignType.name());
        assertThat(nlsEvent.getSubject().getVersion()).isEqualTo(String.valueOf(version));
        assertThat(nlsEvent.getSubject().getNwbVersion()).isEqualTo(String.valueOf(nwbVersionId));
        assertThat(nlsEvent.getSubject().getTimestamp()).isEqualTo(trafficSignTimestamp.toString());
    }
}