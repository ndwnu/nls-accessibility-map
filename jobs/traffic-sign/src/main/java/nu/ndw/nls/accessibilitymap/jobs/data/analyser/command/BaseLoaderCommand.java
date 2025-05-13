package nu.ndw.nls.accessibilitymap.jobs.data.analyser.command;

import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

/*
 * The PicoCLI framework needs one base command as entrypoint.
 * That's why there's this empty container class.
 */
@Component
@Command(name = "jobs", subcommands = {UpdateCacheCommand.class})
public class BaseLoaderCommand {

}
