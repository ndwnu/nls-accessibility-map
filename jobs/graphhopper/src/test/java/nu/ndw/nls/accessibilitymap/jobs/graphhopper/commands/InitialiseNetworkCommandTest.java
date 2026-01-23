package nu.ndw.nls.accessibilitymap.jobs.graphhopper.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import ch.qos.logback.classic.Level;
import java.io.IOException;
import lombok.SneakyThrows;
import nu.ndw.nls.accessibilitymap.jobs.graphhopper.services.AccessibilityNetworkService;
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
class InitialiseNetworkCommandTest {

    private InitialiseNetworkCommand initialiseNetworkCommand;

    @Mock
    private AccessibilityNetworkService accessibilityNetworkService;

    @BeforeEach
    void setUp() {
        initialiseNetworkCommand = new InitialiseNetworkCommand(accessibilityNetworkService);
    }

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @SneakyThrows
    @Test
    void call() {
        assertThat(new CommandLine(initialiseNetworkCommand).execute()).isZero();
        verify(accessibilityNetworkService).storeLatestNetworkOnDisk();
    }

    @Test
    void call_unableToStoreNetwork() throws IOException {
        doThrow(new RuntimeException("error")).when(accessibilityNetworkService).storeLatestNetworkOnDisk();

        assertThat(new CommandLine(initialiseNetworkCommand).execute()).isOne();

        loggerExtension.containsLog(Level.ERROR, "And error occurred while creating or updating latest network", "error");
    }

    @Test
    void annotation_class_command() {

        AnnotationUtil.classContainsAnnotation(
                initialiseNetworkCommand.getClass(),
                Command.class,
                annotation -> assertThat(annotation.name())
                        .isEqualTo("initialiseNetwork")
        );
    }
}
