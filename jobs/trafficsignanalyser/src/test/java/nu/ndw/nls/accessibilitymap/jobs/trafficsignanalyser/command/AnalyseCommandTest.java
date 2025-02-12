package nu.ndw.nls.accessibilitymap.jobs.trafficsignanalyser.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import java.time.OffsetDateTime;
import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.AccessibilityConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.core.time.ClockService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.mapper.VehiclePropertiesMapper;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import nu.ndw.nls.accessibilitymap.jobs.trafficsignanalyser.command.dto.AnalyseProperties;
import nu.ndw.nls.accessibilitymap.jobs.trafficsignanalyser.configuration.AnalyserConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.trafficsignanalyser.service.TrafficSignAnalyserService;
import nu.ndw.nls.accessibilitymap.shared.network.dtos.AccessibilityGraphhopperMetaData;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
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
import picocli.CommandLine.Command;

@ExtendWith(MockitoExtension.class)
class AnalyseCommandTest {

    private AnalyseCommand analyseCommand;

    @Mock
    private AccessibilityConfiguration accessibilityConfiguration;

    @Mock
    private AnalyserConfiguration analyserConfiguration;

    @Mock
    private VehiclePropertiesMapper vehiclePropertiesMapper;

    @Mock
    private ClockService clockService;

    @Mock
    private TrafficSignAnalyserService trafficSignAnalyserService;

    @Mock
    private AccessibilityGraphhopperMetaData accessibilityGraphhopperMetaData;

    @Mock
    private VehicleProperties vehicleProperties;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @BeforeEach
    void setUp() {

        analyseCommand = new AnalyseCommand(accessibilityConfiguration, analyserConfiguration, vehiclePropertiesMapper,
                clockService, trafficSignAnalyserService);
    }

    @ParameterizedTest
    @EnumSource(value = TrafficSignType.class)
    void call_ok(TrafficSignType trafficSignType) {

        OffsetDateTime startTime = OffsetDateTime.parse("2022-03-11T09:00:00.000-01:00");

        when(accessibilityConfiguration.accessibilityGraphhopperMetaData()).thenReturn(accessibilityGraphhopperMetaData);
        when(accessibilityGraphhopperMetaData.nwbVersion()).thenReturn(123);
        when(clockService.now()).thenReturn(startTime);
        when(vehiclePropertiesMapper.map(List.of(trafficSignType), false)).thenReturn(vehicleProperties);

        when(analyserConfiguration.startLocationLatitude()).thenReturn(2d);
        when(analyserConfiguration.startLocationLongitude()).thenReturn(3d);
        when(analyserConfiguration.searchRadiusInMeters()).thenReturn(4d);

        assertThat(new CommandLine(analyseCommand)
                .execute("--traffic-signs=%s".formatted(trafficSignType.name()),
                        "--report-issues")
        ).isZero();

        ArgumentCaptor<AnalyseProperties> analysePropertiesCaptor = ArgumentCaptor.forClass(
                AnalyseProperties.class);

        verify(trafficSignAnalyserService).analyse(analysePropertiesCaptor.capture());

        AnalyseProperties analyseProperties = analysePropertiesCaptor.getValue();
        assertThat(analyseProperties.startTime()).isEqualTo(startTime);
        assertThat(analyseProperties.startLocationLatitude()).isEqualTo(2d);
        assertThat(analyseProperties.startLocationLongitude()).isEqualTo(3d);
        assertThat(analyseProperties.trafficSignTypes()).isEqualTo(List.of(trafficSignType));
        assertThat(analyseProperties.vehicleProperties()).isEqualTo(vehicleProperties);
        assertThat(analyseProperties.nwbVersion()).isEqualTo(123);
        assertThat(analyseProperties.searchRadiusInMeters()).isEqualTo(4d);
        assertThat(analyseProperties.reportIssues()).isTrue();

        loggerExtension.containsLog(Level.INFO, "Analysing traffic signs: [%s]".formatted(trafficSignType.name()));
    }

    @Test
    void call_multipleTrafficSignTypes() {

        OffsetDateTime startTime = OffsetDateTime.parse("2022-03-11T09:00:00.000-01:00");

        when(accessibilityConfiguration.accessibilityGraphhopperMetaData()).thenReturn(accessibilityGraphhopperMetaData);
        when(accessibilityGraphhopperMetaData.nwbVersion()).thenReturn(123);
        when(clockService.now()).thenReturn(startTime);
        when(vehiclePropertiesMapper.map(List.of(TrafficSignType.C6, TrafficSignType.C7), false)).thenReturn(vehicleProperties);
        when(vehiclePropertiesMapper.map(List.of(TrafficSignType.C18), false)).thenReturn(vehicleProperties);

        when(analyserConfiguration.startLocationLatitude()).thenReturn(2d);
        when(analyserConfiguration.startLocationLongitude()).thenReturn(3d);
        when(analyserConfiguration.searchRadiusInMeters()).thenReturn(4d);

        assertThat(new CommandLine(analyseCommand)
                .execute("--traffic-signs=%s,%s".formatted(TrafficSignType.C6.name(), TrafficSignType.C7.name()),
                        "--traffic-signs=%s".formatted(TrafficSignType.C18.name()),
                        "--report-issues")
        ).isZero();

        verify(trafficSignAnalyserService).analyse(argThat(
                analyseProperties -> analyseProperties.trafficSignTypes().containsAll(List.of(TrafficSignType.C6, TrafficSignType.C7))));
        verify(trafficSignAnalyserService).analyse(argThat(
                analyseProperties -> analyseProperties.trafficSignTypes().contains(TrafficSignType.C18)));

        loggerExtension.containsLog
                (Level.INFO,
                        "Analysing traffic signs: [%s, %s]".formatted(TrafficSignType.C6.name(), TrafficSignType.C7.name()));
        loggerExtension.containsLog(Level.INFO, "Analysing traffic signs: [%s]".formatted(TrafficSignType.C18.name()));
    }

    @Test
    void call_error() {

        when(clockService.now()).thenThrow(new RuntimeException("test exception"));

        assertThat(new CommandLine(analyseCommand)
                .execute("--traffic-signs=%s".formatted(TrafficSignType.C18.name()),
                        "--report-issues")
        ).isOne();

        verify(trafficSignAnalyserService, never()).analyse(any());
        loggerExtension.containsLog(Level.ERROR, "Could not analyse traffic signs because of:", "test exception");
    }

    @Test
    void annotation_class_command() {

        AnnotationUtil.classContainsAnnotation(
                analyseCommand.getClass(),
                Command.class,
                annotation -> assertThat(annotation.name()).isEqualTo("analyse")
        );
    }
}