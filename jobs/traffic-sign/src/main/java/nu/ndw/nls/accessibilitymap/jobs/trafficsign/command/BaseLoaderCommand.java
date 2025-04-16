package nu.ndw.nls.accessibilitymap.jobs.trafficsign.command;

import nu.ndw.nls.springboot.messaging.commands.ConfigureRabbitMQCommand;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

/*
 * The PicoCLI framework needs one base command as entrypoint.
 * That's why there's this empty container class.
 */
@Component
@Command(name = "jobs", subcommands = {AnalyseCommand.class, UpdateCacheCommand.class, ConfigureRabbitMQCommand.class})
public class BaseLoaderCommand {

}
