package nu.ndw.nls.accessibilitymap.jobs.data.analyser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.data.analyser.command.BaseLoaderCommand;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationCommandLineRunner implements CommandLineRunner, ExitCodeGenerator {

    private final BaseLoaderCommand baseLoaderCommand;

    private final CommandLine.IFactory factory;

    private int exitCode;

    @Override
    public void run(String[] args) {

        exitCode = new CommandLine(baseLoaderCommand, factory).execute(args);

        log.info("Finished");
    }

    @Override
    public int getExitCode() {

        return exitCode;
    }
}
