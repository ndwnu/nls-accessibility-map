package nu.ndw.nls.accessibilitymap.jobs.graphhopper.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import java.io.IOException;
import nu.ndw.nls.accessibilitymap.accessibility.service.AccessibilityNetworkService;
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
class CreateOrUpdateNetworkCommandTest {

    private CreateOrUpdateNetworkCommand createOrUpdateNetworkCommand;

    @Mock
    private AccessibilityNetworkService accessibilityNetworkService;

    @Mock
    private MessageService messageService;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @BeforeEach
    void setUp() {

        createOrUpdateNetworkCommand = new CreateOrUpdateNetworkCommand(accessibilityNetworkService, messageService);
    }

    @Test
    void call() throws IOException {

        when(messageService.receive(eq(NlsEventType.NWB_IMPORTED_EVENT), any())).thenAnswer(answer -> {
            NlsEventConsumeFunction<Integer> function = answer.getArgument(1);
            return MessageConsumeResult.builder()
                    .result(function.apply(NlsEvent.builder().build()))
                    .build();
        });

        assertThat(new CommandLine(createOrUpdateNetworkCommand).execute()).isZero();

        verify(accessibilityNetworkService).storeLatestNetworkOnDisk();
    }

    @Test
    void call_unableToStoreNetwork() throws IOException {

        when(messageService.receive(eq(NlsEventType.NWB_IMPORTED_EVENT), any())).thenAnswer(answer -> {
            NlsEventConsumeFunction<Integer> function = answer.getArgument(1);
            return MessageConsumeResult.builder()
                    .result(function.apply(NlsEvent.builder().build()))
                    .build();
        });
        doThrow(new RuntimeException("error")).when(accessibilityNetworkService).storeLatestNetworkOnDisk();

        assertThat(new CommandLine(createOrUpdateNetworkCommand).execute()).isOne();

        loggerExtension.containsLog(Level.ERROR, "And error occurred while creating or updating latest network", "error");
    }

    @Test
    void annotation_class_command() {

        AnnotationUtil.classContainsAnnotation(
                createOrUpdateNetworkCommand.getClass(),
                Command.class,
                annotation -> assertThat(annotation.name()).isEqualTo("createOrUpdateNetwork")
        );
    }
}
