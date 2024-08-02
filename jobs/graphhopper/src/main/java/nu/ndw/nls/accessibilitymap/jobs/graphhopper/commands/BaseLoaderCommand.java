package nu.ndw.nls.accessibilitymap.jobs.graphhopper.commands;

import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

/*
 * The PicoCLI framework needs one base command as entrypoint.
 * That's why there's this empty container class.
 */
@Component
@Command(name = "jobs", subcommands = { CreateOrUpdateNetworkCommand.class })
public class BaseLoaderCommand {

}
