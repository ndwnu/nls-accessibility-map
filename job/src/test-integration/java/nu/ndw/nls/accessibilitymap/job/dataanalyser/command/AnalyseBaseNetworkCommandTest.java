package nu.ndw.nls.accessibilitymap.job.dataanalyser.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import nu.ndw.nls.accessibilitymap.job.dataanalyser.command.dto.AnalyseNetworkConfiguration;
import nu.ndw.nls.accessibilitymap.job.dataanalyser.service.NetworkAnalyserService;
import nu.ndw.nls.events.NlsEvent;
import nu.ndw.nls.events.NlsEventType;
import nu.ndw.nls.springboot.messaging.dtos.MessageConsumeResult;
import nu.ndw.nls.springboot.messaging.functions.NlsEventConsumeFunction;
import nu.ndw.nls.springboot.messaging.services.MessageService;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@ExtendWith(MockitoExtension.class)
class AnalyseBaseNetworkCommandTest {

    private AnalyseBaseNetworkCommand analyseBaseNetworkCommand;

    @Mock
    private NetworkAnalyserService networkAnalyserService;

    @Mock
    private MessageService messageService;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @BeforeEach
    void setUp() {

        analyseBaseNetworkCommand = new AnalyseBaseNetworkCommand(
                networkAnalyserService,
                messageService);
    }

    @Test
    void call() {

        when(messageService.receive(eq(NlsEventType.ACCESSIBILITY_ROUTING_NETWORK_UPDATED), any())).thenAnswer(answer -> {
            NlsEventConsumeFunction<Integer> function = answer.getArgument(1);
            return MessageConsumeResult.builder()
                    .result(function.apply(NlsEvent.builder().build()))
                    .build();
        });

        assertThat(new CommandLine(analyseBaseNetworkCommand).execute(
                "--start-location-latitude=2d",
                "--start-location-longitude=3d",
                "--search-radius-in-meters=4d",
                "--report-issues")
        ).isZero();

        verify(networkAnalyserService).analyse(
                AnalyseNetworkConfiguration.builder()
                        .startLocationLatitude(2d)
                        .startLocationLongitude(3d)
                        .searchRadiusInMeters(4d)
                        .reportIssues(true)
                        .build()
        );
    }

    @Test
    void call_error() {

        when(messageService.receive(eq(NlsEventType.ACCESSIBILITY_ROUTING_NETWORK_UPDATED), any())).thenAnswer(answer -> {
            NlsEventConsumeFunction<Integer> function = answer.getArgument(1);
            return MessageConsumeResult.builder()
                    .result(function.apply(NlsEvent.builder().build()))
                    .build();
        });

        doThrow(new RuntimeException("test exception")).when(networkAnalyserService).analyse(any());
        assertThat(new CommandLine(analyseBaseNetworkCommand).execute(
                "--start-location-latitude=2d",
                "--start-location-longitude=3d",
                "--search-radius-in-meters=4d",
                "--report-issues")
        ).isOne();

        loggerExtension.containsLog(Level.ERROR, "Could not analyse base network because of:", "test exception");
    }

    @Test
    void annotation_class_command() {

        AnnotationUtil.classContainsAnnotation(
                analyseBaseNetworkCommand.getClass(),
                Command.class,
                annotation -> assertThat(annotation.name()).isEqualTo("analyse-base-network")
        );
    }
}
