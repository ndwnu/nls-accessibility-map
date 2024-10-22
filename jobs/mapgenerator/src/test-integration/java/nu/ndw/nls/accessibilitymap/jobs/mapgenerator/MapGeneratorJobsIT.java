package nu.ndw.nls.accessibilitymap.jobs.mapgenerator;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.MapGeneratorJobsIT.TestConfig;
import nu.ndw.nls.events.NlsEvent;
import nu.ndw.nls.events.NlsEventSubjectType;
import nu.ndw.nls.events.NlsEventType;
import nu.ndw.nls.springboot.messaging.MessagingConfig;
import nu.ndw.nls.springboot.messaging.dtos.MessageConsumeResult;
import nu.ndw.nls.springboot.messaging.services.MessageReceiveService.ReceiveKey;
import nu.ndw.nls.springboot.messaging.services.MessageService;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
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
class MapGeneratorJobsIT {

    private static final String DESTINATION_PATH = "../../map-generation-destination";

    @Configuration
    @Import(MessagingConfig.class)
    public static class TestConfig {

    }

    @Autowired
    private MessageService messageService;

    @Test
    void messageReceived_ok_c6Published() {

        verifyMessageAvailable(NlsEventSubjectType.ACCESSIBILITY_WINDOWS_TIMES_RVV_CODE_C6);
    }

    @Test
    void messageReceived_ok_c7Published() {

        verifyMessageAvailable(NlsEventSubjectType.ACCESSIBILITY_WINDOWS_TIMES_RVV_CODE_C7);
    }

    @Test
    void messageReceived_ok_c7bPublished() {

        verifyMessageAvailable(NlsEventSubjectType.ACCESSIBILITY_WINDOWS_TIMES_RVV_CODE_C7B);
    }

    @Test
    void messageReceived_ok_c12Published() {

        verifyMessageAvailable(NlsEventSubjectType.ACCESSIBILITY_WINDOWS_TIMES_RVV_CODE_C12);
    }

    @Test
    void messageReceived_ok_c22cPublished() {

        verifyMessageAvailable(NlsEventSubjectType.ACCESSIBILITY_WINDOWS_TIMES_RVV_CODE_C22C);
    }

    @Test
    void geojson_ok_c6Published() throws IOException {

        verifyGeoJson("c6WindowTimeSegments.geojson");
        verifyGeoJson("c6WindowTimeSegments-polygon.geojson");
    }
    private void verifyGeoJson(String geojsonFileName) throws IOException {
        Path geojsonFilePath = formatGeneratedWindowTimesPath(geojsonFileName);
        assertThat(Files.exists(geojsonFilePath))
                .isTrue()
                .withFailMessage("GeoJson file does not exist");

        assertTrue(Files.size(geojsonFilePath) > 0, "GeoJson file must not be 0 bytes");

        String actualJson = Files.readString(geojsonFilePath);

        String expectedJson = readTestDataFromFile("expected-it-results/MapGeneratorJobsIT", geojsonFileName);

        assertThatJson(actualJson)
                .isEqualTo(expectedJson);
    }

    private void verifyMessageAvailable(NlsEventSubjectType nlsEventSubjectType) {
        MessageConsumeResult<NlsEvent> messageConsumeResult = messageService.receive(ReceiveKey.builder()
                        .eventType(NlsEventType.MAP_GEOJSON_PUBLISHED_EVENT)
                        .eventSubjectType(nlsEventSubjectType)
                        .build(),
                nlsEvent -> nlsEvent);

        NlsEvent result = messageConsumeResult.getResult();

        assertEquals(NlsEventType.MAP_GEOJSON_PUBLISHED_EVENT, result.getType());
        assertEquals(nlsEventSubjectType, result.getSubject().getType());
        assertEquals(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE), result.getSubject().getVersion());
        assertEquals("20240701", result.getSubject().getNwbVersion());
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

}

