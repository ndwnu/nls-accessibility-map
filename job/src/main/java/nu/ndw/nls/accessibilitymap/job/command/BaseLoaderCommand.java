package nu.ndw.nls.accessibilitymap.job.command;

import nu.ndw.nls.accessibilitymap.job.cache.command.InitializeCacheCommand;
import nu.ndw.nls.accessibilitymap.job.dataanalyser.command.AnalyseAsymmetricTrafficSignsCommand;
import nu.ndw.nls.accessibilitymap.job.dataanalyser.command.AnalyseBaseNetworkCommand;
import nu.ndw.nls.accessibilitymap.job.mapgenerator.command.GenerateCommand;
import nu.ndw.nls.accessibilitymap.job.network.command.RebuildNetworkCacheCommand;
import nu.ndw.nls.accessibilitymap.job.trafficsign.command.UpdateCacheCommand;
import nu.ndw.nls.springboot.job.annotation.ConditionalOnNotRunningAsService;
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
@ConditionalOnNotRunningAsService
@Command(name = "jobs", subcommands = {
        AnalyseAsymmetricTrafficSignsCommand.class,
        AnalyseBaseNetworkCommand.class,
        ConfigureRabbitMQCommand.class,
        RebuildNetworkCacheCommand.class,
        GenerateCommand.class,
        InitializeCacheCommand.class,
        UpdateCacheCommand.class})
public class BaseLoaderCommand extends AbstractCommand {

    public BaseLoaderCommand(IFactory factory) {
        super(factory);
    }
}
