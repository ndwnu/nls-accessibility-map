package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import java.time.OffsetDateTime;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphhopperConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.network.GraphhopperMetaData;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.time.ClockService;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.dto.ExportProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.ExportType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.services.MapGeneratorService;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignType;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import picocli.CommandLine;

@ExtendWith(MockitoExtension.class)
class GenerateCommandTest {

    private GenerateCommand generateCommand;

    @Mock
    private MapGeneratorService mapGeneratorService;

    @Mock
    private GraphhopperConfiguration graphhopperConfiguration;

    @Mock
    private GenerateConfiguration generateConfiguration;

    @Mock
    private ClockService clockService;

    @Mock
    private GraphhopperMetaData graphhopperMetaData;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @BeforeEach
    void setUp() {

        generateCommand = new GenerateCommand(mapGeneratorService, graphhopperConfiguration, generateConfiguration, clockService);
    }

    @ParameterizedTest
    @EnumSource(value = TrafficSignType.class)
    void call(TrafficSignType trafficSignType) {

        OffsetDateTime startTime = OffsetDateTime.parse("2022-03-11T09:00:00.000-01:00");

        when(graphhopperConfiguration.getMetaData()).thenReturn(graphhopperMetaData);
        when(graphhopperConfiguration.getMetaData()).thenReturn(graphhopperMetaData);
        when(graphhopperMetaData.nwbVersion()).thenReturn(123);
        when(clockService.now()).thenReturn(startTime);
        when(generateConfiguration.startLocationLatitude()).thenReturn(1d);
        when(generateConfiguration.startLocationLongitude()).thenReturn(2d);
        when(generateConfiguration.searchRadiusInMeters()).thenReturn(3d);

        assertThat(new CommandLine(generateCommand)
                .execute("--export-name=%s".formatted(trafficSignType.name()),
                        "--traffic-sign=%s".formatted(trafficSignType.name()),
                        "--export-type=%s".formatted(ExportType.LINE_STRING_GEO_JSON.name()),
                        "--include-only-time-windowed-signs",
                        "--publish-events")
        ).isZero();

        ArgumentCaptor<ExportProperties> exportPropertiesCaptor = ArgumentCaptor.forClass(
                ExportProperties.class);
        verify(mapGeneratorService).generate(exportPropertiesCaptor.capture());

        ExportProperties exportProperties = exportPropertiesCaptor.getValue();

        validateExportPropertiesValid(
                Set.of(trafficSignType),
                exportProperties,
                startTime,
                true,
                true);
    }

    @ParameterizedTest
    @EnumSource(value = TrafficSignType.class)
    void call_withoutTimeWindows(TrafficSignType trafficSignType) {

        OffsetDateTime startTime = OffsetDateTime.parse("2022-03-11T09:00:00.000-01:00");

        when(graphhopperConfiguration.getMetaData()).thenReturn(graphhopperMetaData);
        when(graphhopperConfiguration.getMetaData()).thenReturn(graphhopperMetaData);
        when(graphhopperMetaData.nwbVersion()).thenReturn(123);
        when(clockService.now()).thenReturn(startTime);
        when(generateConfiguration.startLocationLatitude()).thenReturn(1d);
        when(generateConfiguration.startLocationLongitude()).thenReturn(2d);
        when(generateConfiguration.searchRadiusInMeters()).thenReturn(3d);

        assertThat(new CommandLine(generateCommand)
                .execute("--export-name=%s".formatted(trafficSignType.name()),
                        "--traffic-sign=%s".formatted(trafficSignType.name()),
                        "--export-type=%s".formatted(ExportType.LINE_STRING_GEO_JSON.name()),
                        "--publish-events")
        ).isZero();

        ArgumentCaptor<ExportProperties> exportPropertiesCaptor = ArgumentCaptor.forClass(
                ExportProperties.class);
        verify(mapGeneratorService).generate(exportPropertiesCaptor.capture());

        ExportProperties exportProperties = exportPropertiesCaptor.getValue();

        validateExportPropertiesValid(
                Set.of(trafficSignType),
                exportProperties,
                startTime,
                true,
                false);
    }

    @Test
    void call_withoutPublishingEvents() {

        OffsetDateTime startTime = OffsetDateTime.parse("2022-03-11T09:00:00.000-01:00");
        TrafficSignType trafficSignType = TrafficSignType.C1;

        when(graphhopperConfiguration.getMetaData()).thenReturn(graphhopperMetaData);
        when(graphhopperConfiguration.getMetaData()).thenReturn(graphhopperMetaData);
        when(graphhopperMetaData.nwbVersion()).thenReturn(123);
        when(clockService.now()).thenReturn(startTime);
        when(generateConfiguration.startLocationLatitude()).thenReturn(1d);
        when(generateConfiguration.startLocationLongitude()).thenReturn(2d);
        when(generateConfiguration.searchRadiusInMeters()).thenReturn(3d);

        assertThat(new CommandLine(generateCommand)
                .execute("--export-name=%s".formatted(trafficSignType.name()),
                        "--traffic-sign=%s".formatted(trafficSignType.name()),
                        "--export-type=%s".formatted(ExportType.LINE_STRING_GEO_JSON.name()),
                        "--include-only-time-windowed-signs")
        ).isZero();

        ArgumentCaptor<ExportProperties> exportPropertiesCaptor = ArgumentCaptor.forClass(
                ExportProperties.class);
        verify(mapGeneratorService).generate(exportPropertiesCaptor.capture());

        ExportProperties exportProperties = exportPropertiesCaptor.getValue();

        validateExportPropertiesValid(
                Set.of(trafficSignType),
                exportProperties,
                startTime,
                false,
                true);
    }

    @Test
    void call_errorOccurred() {

        when(clockService.now()).thenThrow(new RuntimeException("MyException"));

        assertThat(new CommandLine(generateCommand)
                .execute("--export-name=%s".formatted(TrafficSignType.C6.name()),
                        "--traffic-sign=%s".formatted(TrafficSignType.C6.name()),
                        "--export-type=%s".formatted(ExportType.LINE_STRING_GEO_JSON.name()),
                        "--include-only-time-windowed-signs",
                        "--publish-events")
        ).isOne();

        loggerExtension.containsLog(
                Level.ERROR,
                "Could not generate export because of: ",
                "MyException"
        );
    }

    @Test
    void call_exceptionMultipleTrafficSigns() {

        assertThat(new CommandLine(generateCommand)
                .execute("--export-name=%s".formatted(TrafficSignType.C6.name()),
                        "--traffic-sign=%s".formatted(TrafficSignType.C6.name()),
                        "--traffic-sign=%s".formatted(TrafficSignType.C7.name()),
                        "--export-type=%s".formatted(ExportType.LINE_STRING_GEO_JSON.name()),
                        "--include-only-time-windowed-signs",
                        "--publish-events"
                )
        ).isOne();

        loggerExtension.containsLog(
                Level.ERROR,
                "Could not generate export because of: ",
                "Events are disabled for multiple traffic signs"
        );
    }

    private void validateExportPropertiesValid(
            Set<TrafficSignType> trafficSignTypes,
            ExportProperties exportProperties,
            OffsetDateTime startTime,
            boolean publishEvents,
            boolean includeTimeWindowedSigns) {

        assertThat(exportProperties.startTime()).isEqualTo(startTime);
        assertThat(exportProperties.publishEvents()).isEqualTo(publishEvents);
        assertThat(exportProperties.accessibilityRequest()).isEqualTo(
                AccessibilityRequest.builder()
                        .timestamp(startTime)
                        .trafficSignTypes(trafficSignTypes)
                        .startLocationLatitude(1d)
                        .startLocationLongitude(2d)
                        .searchRadiusInMeters(3d)
                        .trafficSignTextSignTypes(includeTimeWindowedSigns ? Set.of(TextSignType.TIME_PERIOD) : null)
                        .build());
        assertThat(exportProperties.generateConfiguration()).isEqualTo(generateConfiguration);

        loggerExtension.containsLog(
                Level.INFO,
                "Generating export"
        );
    }

}
