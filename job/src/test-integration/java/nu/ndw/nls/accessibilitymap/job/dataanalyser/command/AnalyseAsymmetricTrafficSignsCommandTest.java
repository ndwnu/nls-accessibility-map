package nu.ndw.nls.accessibilitymap.job.dataanalyser.command;

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
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.job.dataanalyser.command.dto.AnalyseAsymmetricTrafficSignsConfiguration;
import nu.ndw.nls.accessibilitymap.job.dataanalyser.service.TrafficSignAnalyserService;
import nu.ndw.nls.springboot.core.time.ClockService;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@ExtendWith(MockitoExtension.class)
class AnalyseAsymmetricTrafficSignsCommandTest {

    private AnalyseAsymmetricTrafficSignsCommand analyseAsymmetricTrafficSignsCommand;

    @Mock
    private ClockService clockService;

    @Mock
    private TrafficSignAnalyserService trafficSignAnalyserService;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @BeforeEach
    void setUp() {

        analyseAsymmetricTrafficSignsCommand = new AnalyseAsymmetricTrafficSignsCommand(
                clockService,
                trafficSignAnalyserService);
    }

    @ParameterizedTest
    @EnumSource(value = TrafficSignType.class)
    void call(TrafficSignType trafficSignType) {

        OffsetDateTime startTime = OffsetDateTime.parse("2022-03-11T09:00:00.000-01:00");

        when(clockService.now()).thenReturn(startTime);

        assertThat(new CommandLine(analyseAsymmetricTrafficSignsCommand)
                .execute(
                        "--traffic-signs=%s".formatted(trafficSignType.name()),
                        "--start-location-latitude=2d",
                        "--start-location-longitude=3d",
                        "--search-radius-in-meters=4d",
                        "--report-issues")
        ).isZero();

        verify(trafficSignAnalyserService).analyse(
                AnalyseAsymmetricTrafficSignsConfiguration.builder()
                        .startTime(startTime)
                        .accessibilityRequest(AccessibilityRequest.builder()
                                .timestamp(startTime)
                                .startLocationLatitude(2d)
                                .startLocationLongitude(3d)
                                .trafficSignTypes(Set.of(trafficSignType))
                                .searchRadiusInMeters(4d)
                                .build())
                        .reportIssues(true)
                        .build()
        );
    }

    @Test
    void call_multipleTrafficSignTypes() {

        OffsetDateTime startTime = OffsetDateTime.parse("2022-03-11T09:00:00.000-01:00");

        when(clockService.now()).thenReturn(startTime);

        assertThat(new CommandLine(analyseAsymmetricTrafficSignsCommand)
                .execute(
                        "--traffic-signs=%s,%s".formatted(TrafficSignType.C6.name(), TrafficSignType.C7.name()),
                        "--traffic-signs=%s".formatted(TrafficSignType.C18.name()),
                        "--start-location-latitude=2d",
                        "--start-location-longitude=3d",
                        "--search-radius-in-meters=4d",
                        "--report-issues")
        ).isZero();

        verify(trafficSignAnalyserService).analyse(
                argThat(analyseProperties -> analyseProperties.accessibilityRequest().trafficSignTypes()
                        .containsAll(List.of(TrafficSignType.C6, TrafficSignType.C7))));
        verify(trafficSignAnalyserService).analyse(
                argThat(analyseProperties -> analyseProperties.accessibilityRequest().trafficSignTypes().contains(TrafficSignType.C18)));
    }

    @Test
    void call_error() {

        when(clockService.now()).thenThrow(new RuntimeException("test exception"));

        assertThat(new CommandLine(analyseAsymmetricTrafficSignsCommand)
                .execute(
                        "--traffic-signs=%s".formatted(TrafficSignType.C18.name()),
                        "--start-location-latitude=2d",
                        "--start-location-longitude=3d",
                        "--search-radius-in-meters=4d",
                        "--report-issues")
        ).isOne();

        verify(trafficSignAnalyserService, never()).analyse(any());
        loggerExtension.containsLog(Level.ERROR, "Could not analyse traffic signs because of:", "test exception");
    }

    @Test
    void annotation_class_command() {

        AnnotationUtil.classContainsAnnotation(
                analyseAsymmetricTrafficSignsCommand.getClass(),
                Command.class,
                annotation -> assertThat(annotation.name()).isEqualTo("analyse-asymmetric-traffic-signs")
        );
    }
}
