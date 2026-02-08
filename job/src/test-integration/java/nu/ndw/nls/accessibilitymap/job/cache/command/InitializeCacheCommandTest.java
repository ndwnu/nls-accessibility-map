package nu.ndw.nls.accessibilitymap.job.cache.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import lombok.SneakyThrows;
import nu.ndw.nls.accessibilitymap.accessibility.network.NetworkDataService;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.TrafficSignDataService;
import nu.ndw.nls.accessibilitymap.job.trafficsign.command.UpdateCacheCommand;
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
class InitializeCacheCommandTest {

    private InitializeCacheCommand initializeCacheCommand;

    @Mock
    private NetworkDataService networkDataService;

    @Mock
    private UpdateCacheCommand updateCacheCommand;

    @Mock
    private TrafficSignDataService trafficSignDataService;

    @BeforeEach
    void setUp() {
        initializeCacheCommand = new InitializeCacheCommand(networkDataService, updateCacheCommand, trafficSignDataService);
    }

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @SneakyThrows
    @Test
    void call() {

        assertThat(new CommandLine(initializeCacheCommand).execute()).isZero();

        verify(networkDataService).recompileData();
        verify(trafficSignDataService).dataExists();
    }

    @SneakyThrows
    @Test
    void call_networkCache_exists() {
        when(networkDataService.networkExists()).thenReturn(true);

        assertThat(new CommandLine(initializeCacheCommand).execute()).isZero();

        loggerExtension.containsLog(Level.INFO, "Network cache already exists, skipping creation");
        verifyNoMoreInteractions(networkDataService);
    }

    @Test
    void call_networkCache_unableToCreateCache() {
        doThrow(new RuntimeException("error")).when(networkDataService).recompileData();

        assertThat(new CommandLine(initializeCacheCommand).execute()).isOne();

        loggerExtension.containsLog(Level.ERROR, "An error occurred while creating network", "error");
    }

    @SneakyThrows
    @Test
    void call_trafficSignCache_exists() {
        when(trafficSignDataService.dataExists()).thenReturn(true);

        assertThat(new CommandLine(initializeCacheCommand).execute()).isZero();

        loggerExtension.containsLog(Level.INFO, "Traffic sign cache already exists, skipping creation");
        verifyNoMoreInteractions(trafficSignDataService);
    }

    @Test
    void call_trafficSignCache_unableToCreateCache() {
        doThrow(new RuntimeException("error")).when(updateCacheCommand).call();

        assertThat(new CommandLine(initializeCacheCommand).execute()).isOne();

        loggerExtension.containsLog(Level.ERROR, "An error occurred while creating traffic sign cache", "error");
    }

    @Test
    void annotation_class_command() {

        AnnotationUtil.classContainsAnnotation(
                initializeCacheCommand.getClass(),
                Command.class,
                annotation -> assertThat(annotation.name())
                        .isEqualTo("initializeCache")
        );
    }
}
