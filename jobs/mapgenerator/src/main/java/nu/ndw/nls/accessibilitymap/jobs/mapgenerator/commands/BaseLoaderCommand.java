package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.commands;

import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.command.GenerateGeoJsonCommand;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

/*
 * The PicoCLI framework needs one base command as entrypoint.
 * That's why there's this empty container class.
 */
@Component
@Command(name = "jobs", subcommands = { GenerateGeoJsonCommand.class })
public class BaseLoaderCommand {

}
