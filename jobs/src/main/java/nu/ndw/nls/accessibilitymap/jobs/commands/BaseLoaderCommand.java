package nu.ndw.nls.accessibilitymap.jobs.commands;

import nu.ndw.nls.accessibilitymap.jobs.generate.geojson.commands.GenerateGeoJsonCommand;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

/*
 * The PicoCLI framework needs one base command as entrypoint.
 * That's why there's this empty container class.
 */
@Component
@Command(name = "jobs", subcommands = {
        CreateOrUpdateNetworkCommand.class,
        GenerateGeoJsonCommand.class
})
public class BaseLoaderCommand {

}
