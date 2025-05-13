package nu.ndw.nls.accessibilitymap.jobs.data.analyser.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperService;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphhopperConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.network.GraphhopperMetaData;
import nu.ndw.nls.accessibilitymap.jobs.data.analyser.command.dto.AnalyseNetworkConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.data.analyser.configuration.AnalyserConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.data.analyser.service.NetworkAnalyserService;
import nu.ndw.nls.events.NlsEvent;
import nu.ndw.nls.events.NlsEventType;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
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
    private GraphHopperService graphHopperService;

    @Mock
    private NetworkGraphHopper networkGraphHopper;

    @Mock
    private GraphhopperConfiguration graphhopperConfiguration;

    @Mock
    private AnalyserConfiguration analyserConfiguration;

    @Mock
    private NetworkAnalyserService networkAnalyserService;

    @Mock
    private GraphhopperMetaData graphhopperMetaData;

    @Mock
    private MessageService messageService;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @BeforeEach
    void setUp() {

        analyseBaseNetworkCommand = new AnalyseBaseNetworkCommand(
                graphHopperService,
                graphhopperConfiguration,
                analyserConfiguration,
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


        when(graphhopperConfiguration.getMetaData()).thenReturn(graphhopperMetaData);
        when(graphhopperMetaData.nwbVersion()).thenReturn(123);
        when(analyserConfiguration.startLocationLatitude()).thenReturn(2d);
        when(analyserConfiguration.startLocationLongitude()).thenReturn(3d);
        when(analyserConfiguration.searchRadiusInMeters()).thenReturn(4d);

        when(graphHopperService.getNetworkGraphHopper()).thenReturn(networkGraphHopper);

        assertThat(new CommandLine(analyseBaseNetworkCommand).execute("--report-issues")).isZero();

        verify(networkAnalyserService).analyse(
                networkGraphHopper,
                AnalyseNetworkConfiguration.builder()
                        .startLocationLatitude(2d)
                        .startLocationLongitude(3d)
                        .searchRadiusInMeters(4d)
                        .nwbVersion(123)
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

        when(graphhopperConfiguration.getMetaData()).thenReturn(graphhopperMetaData);
        when(graphhopperMetaData.nwbVersion()).thenReturn(123);
        when(analyserConfiguration.startLocationLatitude()).thenReturn(2d);
        when(analyserConfiguration.startLocationLongitude()).thenReturn(3d);
        when(analyserConfiguration.searchRadiusInMeters()).thenReturn(4d);

        when(graphHopperService.getNetworkGraphHopper()).thenReturn(networkGraphHopper);

        doThrow(new RuntimeException("test exception")).when(networkAnalyserService).analyse(any(), any());
        assertThat(new CommandLine(analyseBaseNetworkCommand).execute("--report-issues")
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
