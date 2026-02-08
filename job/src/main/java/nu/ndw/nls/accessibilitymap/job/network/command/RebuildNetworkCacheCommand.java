package nu.ndw.nls.accessibilitymap.job.network.command;

import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.network.NetworkDataService;
import nu.ndw.nls.events.NlsEvent;
import nu.ndw.nls.events.NlsEventType;
import nu.ndw.nls.springboot.messaging.services.MessageService;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Slf4j
@Component
@Command(name = "rebuildNetworkCache")
@RequiredArgsConstructor
public class RebuildNetworkCacheCommand implements Callable<Integer> {

    private final NetworkDataService networkDataService;

    private final MessageService messageService;

    @Override
    public Integer call() {
        log.info("Creating or updating latest network");
        return messageService.receive(NlsEventType.NWB_IMPORTED_EVENT, this::start).getResult();
    }

    private Integer start(NlsEvent nlsEvent) {
        try {
            networkDataService.recompileData();
            return 0;
        } catch (RuntimeException e) {
            log.error("And error occurred while creating or updating latest network", e);
            return 1;
        }
    }
}
