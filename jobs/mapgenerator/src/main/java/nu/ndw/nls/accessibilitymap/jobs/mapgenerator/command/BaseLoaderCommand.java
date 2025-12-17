package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command;

import nu.ndw.nls.springboot.job.annotation.ConditionalOnRunWithCommandLineRunner;
import nu.ndw.nls.springboot.job.command.AbstractCommand;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.IFactory;

/*
 * The PicoCLI framework needs one base command as entrypoint.
 * That's why there's this empty container class.
 */
@Component
@ConditionalOnRunWithCommandLineRunner
@Command(name = "jobs", subcommands = {GenerateCommand.class})
public class BaseLoaderCommand extends AbstractCommand {

    public BaseLoaderCommand(IFactory factory) {
        super(factory);
    }
}
