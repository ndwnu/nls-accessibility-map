package nu.ndw.nls.accessibilitymap.jobs.mapgenerator;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.MapGeneratorJobTest.TestConfig;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.events.NlsEvent;
import nu.ndw.nls.events.NlsEventSubjectType;
import nu.ndw.nls.events.NlsEventType;
import nu.ndw.nls.springboot.messaging.MessagingConfig;
import nu.ndw.nls.springboot.messaging.dtos.MessageConsumeResult;
import nu.ndw.nls.springboot.messaging.services.MessageReceiveService.ReceiveKey;
import nu.ndw.nls.springboot.messaging.services.MessageService;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.ResourceUtils;

@SpringBootTest
@ContextConfiguration(classes = TestConfig.class)
@ActiveProfiles(profiles = {"integration-test"})
class MapGeneratorJobTest {

    private static final String DESTINATION_PATH = "../../map-generation-destination";

    private static final Map<TrafficSignType, NlsEventSubjectType> TRAFFIC_SIGN_TYPE_NLS_EVENT_SUBJECT_TYPE_MAP = Map.of(
            TrafficSignType.C6, NlsEventSubjectType.ACCESSIBILITY_WINDOWS_TIMES_RVV_CODE_C6,
            TrafficSignType.C7, NlsEventSubjectType.ACCESSIBILITY_WINDOWS_TIMES_RVV_CODE_C7,
            TrafficSignType.C7B, NlsEventSubjectType.ACCESSIBILITY_WINDOWS_TIMES_RVV_CODE_C7B,
            TrafficSignType.C12, NlsEventSubjectType.ACCESSIBILITY_WINDOWS_TIMES_RVV_CODE_C12,
            TrafficSignType.C22C, NlsEventSubjectType.ACCESSIBILITY_WINDOWS_TIMES_RVV_CODE_C22C
    );

    @Autowired
    private MessageService messageService;

    @ParameterizedTest
    @EnumSource(TrafficSignType.class)
    void messageReceived_ok(TrafficSignType trafficSignType) {

        if (!TRAFFIC_SIGN_TYPE_NLS_EVENT_SUBJECT_TYPE_MAP.containsKey(trafficSignType)) {
            fail("Missing a topic for traffic sign type: %s".formatted(trafficSignType.name()));
        }

        verifyMessageAvailable(TRAFFIC_SIGN_TYPE_NLS_EVENT_SUBJECT_TYPE_MAP.get(trafficSignType));
    }

    @ParameterizedTest
    @EnumSource(TrafficSignType.class)
    void geojsonPublished_ok(TrafficSignType trafficSignType) throws IOException {

        verifyGeoJson(trafficSignType.name().toLowerCase(Locale.US).concat("WindowTimeSegments.geojson"));
        verifyGeoJson(trafficSignType.name().toLowerCase(Locale.US).concat("WindowTimeSegments-polygon.geojson"));
    }

    private void verifyGeoJson(String geojsonFileName) throws IOException {
        Path geojsonFilePath = formatGeneratedWindowTimesPath(geojsonFileName);
        assertThat(Files.exists(geojsonFilePath))
                .isTrue()
                .withFailMessage("GeoJson file does not exist");

        assertThat(Files.size(geojsonFilePath))
                .isGreaterThan(0)
                .withFailMessage("GeoJson file must be larger than 0 bytes");

        String actualJson = Files.readString(geojsonFilePath);
        String expectedJson = readTestDataFromFile("expected-it-results/MapGeneratorJobsIT", geojsonFileName);

        assertThatJson(actualJson).isEqualTo(expectedJson);
    }

    private void verifyMessageAvailable(NlsEventSubjectType nlsEventSubjectType) {
        MessageConsumeResult<NlsEvent> messageConsumeResult = messageService.receive(ReceiveKey.builder()
                        .eventType(NlsEventType.MAP_GEOJSON_PUBLISHED_EVENT)
                        .eventSubjectType(nlsEventSubjectType)
                        .build(),
                nlsEvent -> nlsEvent);

        NlsEvent result = messageConsumeResult.getResult();

        assertThat(result.getType()).isEqualTo(NlsEventType.MAP_GEOJSON_PUBLISHED_EVENT);
        assertThat(result.getSubject().getType()).isEqualTo(nlsEventSubjectType);
        assertThat(result.getSubject().getVersion())
                .isEqualTo(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
        assertThat(result.getSubject().getNwbVersion()).isEqualTo("20240701");
    }

    private Path formatGeneratedWindowTimesPath(String geojsonFileName) {

        return Path.of(DESTINATION_PATH + "/v1/windowTimes/" + LocalDate.now()
                .format(DateTimeFormatter.BASIC_ISO_DATE) +
                "/geojson/").resolve(geojsonFileName);
    }

    public String readTestDataFromFile(final String folder, final String file) throws IOException {

        return FileUtils.readFileToString(
                ResourceUtils.getFile("classpath:" + folder + File.separator + file),
                StandardCharsets.UTF_8.toString());
    }

    @Configuration
    @Import(MessagingConfig.class)
    public static class TestConfig {

    }
}

