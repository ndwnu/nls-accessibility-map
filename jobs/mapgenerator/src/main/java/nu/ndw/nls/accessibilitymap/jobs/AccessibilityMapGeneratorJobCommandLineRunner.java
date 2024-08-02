package nu.ndw.nls.accessibilitymap.jobs;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.commands.BaseLoaderCommand;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccessibilityMapGeneratorJobCommandLineRunner implements CommandLineRunner, ExitCodeGenerator {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            .withZone(ZoneId.from(ZoneOffset.UTC));

    private final BuildProperties buildProperties;
    private final BaseLoaderCommand baseLoaderCommand;
    private final CommandLine.IFactory factory;
    private int exitCode;

    @Override
    public void run(String[] args) {
        String buildTime = DATE_TIME_FORMATTER.format(buildProperties.getTime());
        String imageVersion = buildProperties.get("docker-image.version");
        log.info("docker-image-version: {} built at: {} ", imageVersion, buildTime);
        exitCode = new CommandLine(baseLoaderCommand, factory).execute(args);
        log.info("Finished");
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }
}
