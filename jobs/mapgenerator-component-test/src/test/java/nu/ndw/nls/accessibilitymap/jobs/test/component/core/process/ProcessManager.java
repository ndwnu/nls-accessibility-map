package nu.ndw.nls.accessibilitymap.jobs.test.component.core.process;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.test.component.core.StateManagement;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProcessManager implements StateManagement {

    private final List<Process> startedProcesses = new ArrayList<>();

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    public Process startProcess(List<String> command) {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(command);

        try {
            Process process = builder.start();
            startedProcesses.add(process);

            return process;
        } catch (IOException ioException) {
            fail(ioException);
        }

        return null;
    }

    public Process startProcessAndLog(List<String> command) {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(command);

        try {
            Process process = builder.start();
            startedProcesses.add(process);

            InputStreamConsumer inputStreamConsumer = new InputStreamConsumer(
                    process.getInputStream(),
                    System.out::println);
            InputStreamConsumer errorStreamConsumer = new InputStreamConsumer(
                    process.getErrorStream(),
                    System.err::println);

            executorService.submit(inputStreamConsumer);
            executorService.submit(errorStreamConsumer);

            return process;
        } catch (IOException ioException) {
            fail(ioException);
        }

        return null;
    }

    public void startProcessAndWaitToBeFinished(List<String> command) {

        Process process = startProcessAndLog(command);
        try {
            int exitCode = process.waitFor();
            assertThat(exitCode)
                    .isZero()
                    .withFailMessage(
                            "Process not successfully finished. Exit code %s. Command: %s. Check console for errors."
                                    .formatted(process.exitValue(), command));
        } catch (InterruptedException interruptedException) {
            fail(interruptedException);
        }
    }

    @Override
    public void clearStateAfterEachScenario() {

        startedProcesses.stream().toList().forEach(Process::destroy);
    }
}
