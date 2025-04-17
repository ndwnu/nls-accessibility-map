package nu.ndw.nls.accessibilitymap.jobs.trafficsign.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphhopperConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.network.GraphhopperMetaData;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.time.ClockService;
import nu.ndw.nls.accessibilitymap.jobs.trafficsign.command.dto.AnalyseProperties;
import nu.ndw.nls.accessibilitymap.jobs.trafficsign.configuration.AnalyserConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.trafficsign.service.TrafficSignAnalyserService;
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
    private GraphhopperConfiguration graphhopperConfiguration;

    @Mock
    private AnalyserConfiguration analyserConfiguration;

    @Mock
    private ClockService clockService;

    @Mock
    private TrafficSignAnalyserService trafficSignAnalyserService;

    @Mock
    private GraphhopperMetaData graphhopperMetaData;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @BeforeEach
    void setUp() {

        analyseCommand = new AnalyseCommand(graphhopperConfiguration, analyserConfiguration, clockService, trafficSignAnalyserService);
    }

    @ParameterizedTest
    @EnumSource(value = TrafficSignType.class)
    void call(TrafficSignType trafficSignType) {

        OffsetDateTime startTime = OffsetDateTime.parse("2022-03-11T09:00:00.000-01:00");

        when(graphhopperConfiguration.getMetaData()).thenReturn(graphhopperMetaData);
        when(graphhopperMetaData.nwbVersion()).thenReturn(123);
        when(clockService.now()).thenReturn(startTime);

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

        assertThat(analyseProperties.accessibilityRequest()).isEqualTo(AccessibilityRequest.builder()
                .timestamp(startTime)
                .startLocationLatitude(2d)
                .startLocationLongitude(3d)
                .trafficSignTypes(Set.of(trafficSignType))
                .searchRadiusInMeters(4d)
                .build());
        assertThat(analyseProperties.nwbVersion()).isEqualTo(123);
        assertThat(analyseProperties.reportIssues()).isTrue();

        loggerExtension.containsLog(Level.INFO, "Analysing traffic signs: [%s]".formatted(trafficSignType.name()));
    }

    @Test
    void call_multipleTrafficSignTypes() {

        OffsetDateTime startTime = OffsetDateTime.parse("2022-03-11T09:00:00.000-01:00");

        when(graphhopperConfiguration.getMetaData()).thenReturn(graphhopperMetaData);
        when(graphhopperMetaData.nwbVersion()).thenReturn(123);
        when(clockService.now()).thenReturn(startTime);

        when(analyserConfiguration.startLocationLatitude()).thenReturn(2d);
        when(analyserConfiguration.startLocationLongitude()).thenReturn(3d);
        when(analyserConfiguration.searchRadiusInMeters()).thenReturn(4d);

        assertThat(new CommandLine(analyseCommand)
                .execute("--traffic-signs=%s,%s".formatted(TrafficSignType.C6.name(), TrafficSignType.C7.name()),
                        "--traffic-signs=%s".formatted(TrafficSignType.C18.name()),
                        "--report-issues")
        ).isZero();

        verify(trafficSignAnalyserService).analyse(argThat(
                analyseProperties -> analyseProperties.accessibilityRequest().trafficSignTypes()
                        .containsAll(List.of(TrafficSignType.C6, TrafficSignType.C7))));
        verify(trafficSignAnalyserService).analyse(argThat(
                analyseProperties -> analyseProperties.accessibilityRequest().trafficSignTypes().contains(TrafficSignType.C18)));

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
