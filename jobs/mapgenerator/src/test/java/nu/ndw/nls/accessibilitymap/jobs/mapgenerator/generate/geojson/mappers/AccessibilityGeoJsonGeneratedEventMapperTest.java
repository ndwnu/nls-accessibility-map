package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.time.Instant;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.commands.model.CmdGenerateGeoJsonType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.properties.GeoJsonProperties;
import nu.ndw.nls.events.NlsEvent;
import nu.ndw.nls.events.NlsEventSubject;
import nu.ndw.nls.events.NlsEventSubjectType;
import nu.ndw.nls.events.NlsEventType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityGeoJsonGeneratedEventMapperTest {

    private static final int NWB_VERSION_ID = 20231001;
    private static final String NWB_VERSION_ID_STRING = "20231001";
    private static final String TRAFFIC_SIGN_TIMESTAMP_STRING = "2023-11-07T15:37:23Z";
    private static final Instant TRAFFIC_SIGN_TIMESTAMP = Instant.parse(TRAFFIC_SIGN_TIMESTAMP_STRING);
    private static final int VERSION = 202301002;
    private static final String VERSION_AS_STRING = "202301002";
    private static final CmdGenerateGeoJsonType GENERATE_GEO_JSON_TYPE = CmdGenerateGeoJsonType.C6;
    private static final NlsEventSubjectType NLS_EVENT_SUBJECT_TYPE =
            NlsEventSubjectType.ACCESSIBILITY_WINDOWS_TIMES_RVV_CODE_C6;

    @Mock
    private GenerateConfiguration generateConfiguration;

    @InjectMocks
    private AccessibilityGeoJsonGeneratedEventMapper mapper;

    @Mock
    private GeoJsonProperties geoJsonProperties;

    @Test
    void map_ok() {
        when(generateConfiguration.getConfiguration(GENERATE_GEO_JSON_TYPE)).thenReturn(geoJsonProperties);
        when(geoJsonProperties.getPublisherEventSubjectType())
                .thenReturn(NLS_EVENT_SUBJECT_TYPE);

        NlsEvent event = mapper.map(GENERATE_GEO_JSON_TYPE, VERSION, NWB_VERSION_ID, TRAFFIC_SIGN_TIMESTAMP);

        assertEquals(NlsEventType.MAP_GEOJSON_PUBLISHED_EVENT, event.getType());
        assertEquals(NlsEventSubject.builder()
                .type(NLS_EVENT_SUBJECT_TYPE)
                .version(VERSION_AS_STRING)
                .nwbVersion(NWB_VERSION_ID_STRING)
                .timestamp(TRAFFIC_SIGN_TIMESTAMP_STRING)
                .build(), event.getSubject());
        assertNull(event.getSourceEvent());
    }
}