package nu.ndw.nls.accessibilitymap.jobs.graphhopper.commands;

import java.io.IOException;
import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.graphhopper.services.AccessibilityNetworkService;
import nu.ndw.nls.events.NlsEvent;
import nu.ndw.nls.events.NlsEventType;
import nu.ndw.nls.springboot.messaging.services.MessageService;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Slf4j
@Component
@Command(name = "createOrUpdateNetwork")
@RequiredArgsConstructor
public class CreateOrUpdateNetworkCommand implements Callable<Integer> {

    private final AccessibilityNetworkService accessibilityNetworkService;

    private final MessageService messageService;

    @Override
    public Integer call() {

        return messageService.receive(NlsEventType.NWB_IMPORTED_EVENT, this::start).getResult();
    }

    private Integer start(NlsEvent nlsEvent) {
        try {
            accessibilityNetworkService.storeLatestNetworkOnDisk();
            return 0;
        } catch (IOException | RuntimeException e) {
            log.error("And error occurred while creating or updating latest network", e);
            return 1;
        }
    }
}
