package nu.ndw.nls.accessibilitymap.jobs.data.analyser.command;

import nu.ndw.nls.springboot.job.annotation.ConditionalOnRunWithCommandLineRunner;
import nu.ndw.nls.springboot.job.command.AbstractCommand;
import nu.ndw.nls.springboot.messaging.commands.ConfigureRabbitMQCommand;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.IFactory;

/*
 * The PicoCLI framework needs one base command as entrypoint.
 * That's why there's this empty container class.
 */
@Component
@ConditionalOnRunWithCommandLineRunner
@Command(name = "jobs", subcommands = {
        AnalyseAsymmetricTrafficSignsCommand.class,
        AnalyseBaseNetworkCommand.class,
        ConfigureRabbitMQCommand.class})
public class BaseLoaderCommand extends AbstractCommand {

    public BaseLoaderCommand(IFactory factory) {
        super(factory);
    }
}
