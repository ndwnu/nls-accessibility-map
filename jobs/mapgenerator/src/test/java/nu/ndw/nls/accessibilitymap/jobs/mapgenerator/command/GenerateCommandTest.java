package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.AccessibilityConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.dto.ExportProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.mapper.VehiclePropertiesMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.time.ClockService;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.ExportType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.services.MapGeneratorService;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.test.utils.LoggerExtension;
import nu.ndw.nls.accessibilitymap.shared.network.dtos.AccessibilityGraphhopperMetaData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
    private AccessibilityConfiguration accessibilityConfiguration;

    @Mock
    private GenerateConfiguration generateConfiguration;

    @Mock
    private VehicleProperties vehicleProperties;

    @Mock
    private VehiclePropertiesMapper vehiclePropertiesMapper;

    @Mock
    private ClockService clockService;

    @Mock
    private AccessibilityGraphhopperMetaData accessibilityGraphhopperMetaData;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @BeforeEach
    void setUp() {

        generateCommand = new GenerateCommand(mapGeneratorService, accessibilityConfiguration,
                generateConfiguration, vehiclePropertiesMapper, clockService);
    }

    @ParameterizedTest
    @EnumSource(value = TrafficSignType.class)
    void call_ok(TrafficSignType trafficSignType) {

        OffsetDateTime startTime = OffsetDateTime.parse("2022-03-11T09:00:00.000-01:00");

        when(accessibilityConfiguration.accessibilityGraphhopperMetaData())
                .thenReturn(accessibilityGraphhopperMetaData);
        when(accessibilityGraphhopperMetaData.nwbVersion()).thenReturn(123);
        when(clockService.now()).thenReturn(startTime);
        when(vehiclePropertiesMapper.map(List.of(trafficSignType), true)).thenReturn(vehicleProperties);

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

        validateexportPropertiesValid(
                List.of(trafficSignType),
                exportProperties,
                startTime,
                true,
                true);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            true, true,
            true, false,
            false, true,
            false, false
            """)
    void call_ok_booleanArguments(boolean includeOnlyTimeWindowedSigns, boolean publishEvents) {

        TrafficSignType trafficSignType = TrafficSignType.C6;
        OffsetDateTime startTime = OffsetDateTime.parse("2022-03-11T09:00:00.000-01:00");

        when(accessibilityConfiguration.accessibilityGraphhopperMetaData())
                .thenReturn(accessibilityGraphhopperMetaData);
        when(accessibilityGraphhopperMetaData.nwbVersion()).thenReturn(123);
        when(clockService.now()).thenReturn(startTime);
        when(vehiclePropertiesMapper.map(List.of(trafficSignType), includeOnlyTimeWindowedSigns))
                .thenReturn(vehicleProperties);

        ArrayList<String> arguments = new ArrayList<>();
        arguments.add("--export-name=%s".formatted(trafficSignType.name()));
        arguments.add("--traffic-sign=%s".formatted(trafficSignType.name()));
        arguments.add("--export-type=%s".formatted(ExportType.LINE_STRING_GEO_JSON.name()));
        if (includeOnlyTimeWindowedSigns) {
            arguments.add("--include-only-time-windowed-signs");
        }
        if (publishEvents) {
            arguments.add("--publish-events");
        }

        assertThat(new CommandLine(generateCommand).execute(arguments.toArray(String[]::new))
        ).isZero();

        ArgumentCaptor<ExportProperties> exportPropertiesCaptor = ArgumentCaptor.forClass(
                ExportProperties.class);
        verify(mapGeneratorService).generate(exportPropertiesCaptor.capture());

        ExportProperties exportProperties = exportPropertiesCaptor.getValue();

        validateexportPropertiesValid(
                List.of(trafficSignType),
                exportProperties,
                startTime,
                includeOnlyTimeWindowedSigns,
                publishEvents);
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

    private void validateexportPropertiesValid(
            List<TrafficSignType> trafficSignTypes,
            ExportProperties exportProperties,
            OffsetDateTime startTime,
            boolean includeOnlyTimeWindowedSigns,
            boolean publishEvents) {

        assertThat(exportProperties.startTime()).isEqualTo(startTime);
        assertThat(exportProperties.trafficSignTypes()).isEqualTo(trafficSignTypes);
        assertThat(exportProperties.includeOnlyTimeWindowedSigns()).isEqualTo(includeOnlyTimeWindowedSigns);
        assertThat(exportProperties.publishEvents()).isEqualTo(publishEvents);
        assertThat(exportProperties.vehicleProperties()).isEqualTo(vehicleProperties);
        assertThat(exportProperties.generateConfiguration()).isEqualTo(generateConfiguration);

        loggerExtension.containsLog(
                Level.INFO,
                "Generating export"
        );
    }

}
